package pl.idedyk.japanese.dictionary.web.queue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItemStatus;

@Service
public class QueueService {
	
	private static final Logger logger = Logger.getLogger(QueueService.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Value("${local.dir.job.queue}")
	private String localDirJobQueueDir;
	
	private File localDirJobQueueDirFile;
	
	@PostConstruct
	public void init() {
		
		logger.info("Inicjalizowanie QueueService");
		
		localDirJobQueueDirFile = new File(localDirJobQueueDir);
		
		if (localDirJobQueueDirFile.exists() == false) {
			
			logger.info("Tworzę katalog " + localDirJobQueueDir + " do ewentualnej lokalnej kolejki");
			
			localDirJobQueueDirFile.mkdirs();
		}
		
		if (localDirJobQueueDirFile.exists() == false || localDirJobQueueDirFile.canWrite() == false) {
			
			logger.error("Nie mogę zainicjalizować katalogu " + localDirJobQueueDir + " do ewentualnej lokalnej kolejki");
			
			throw new RuntimeException();
		}
	}

	public void sendToQueue(String queueName, byte[] object) throws SQLException {
		
		// stworzenie nowego elementu do kolejki
		
		QueueItem queueItem = new QueueItem();
		
		queueItem.setName(queueName);
		queueItem.setStatus(QueueItemStatus.WAITING);
		queueItem.setHostName(getHostName());
		queueItem.setSendTimestamp(new Timestamp(new Date().getTime()));
		queueItem.setDeliveryCount(0);
		queueItem.setNextAttempt(queueItem.getSendTimestamp());
		queueItem.setObject(object);
		
		// zapisanie do lokalnego katalogu z kolejka
		saveToLocalDir(queueItem);
		
		/*
		try {
			// wstawienie do kolejki
			mySQLConnector.insertQueueItem(queueItem);
			
		} catch (SQLException e) {
			
			
			throw e;
		}
		*/
	}
	
	private String getHostName() {
		
		try {
			return InetAddress.getLocalHost().getHostName();
			
		} catch (UnknownHostException e) {
			return "localhost";			
		}
	}
	
	private void saveToLocalDir(QueueItem queueItem) {
		
		// logger.info("Zapisanie do lokalnego katalogu kolejki");
		
		String randomFileName = UUID.randomUUID().toString();
		
		File queueItemFileBody = new File(localDirJobQueueDirFile, queueItem.getName() + "_" + randomFileName);
		File queueItemBodyReady = new File(localDirJobQueueDirFile, queueItem.getName() + "_" + randomFileName + ".ready");
		
		ByteArrayOutputStream bos = null;
		ObjectOutput objectOutput = null;
		
		FileOutputStream fos = null;
		
		try {
			// serializacja obiektu
			bos = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bos);
			
			objectOutput.writeObject(queueItem);
			
			objectOutput.close();
			bos.close();
			
			byte[] queueItemByteArray = bos.toByteArray();			
			
			fos = new FileOutputStream(queueItemFileBody);
			
			fos.write(queueItemByteArray);
			
			queueItemBodyReady.createNewFile();
			
		} catch (IOException e) {
			
			logger.error("Błąd zapisu do lokalnego katalogu kolejki", e);
						
		} finally {
			
			if (fos != null) {
				
				try {
					fos.close();
					
				} catch (Exception e) {
					// noop
				}
			}			
		}
	}
	
	public List<QueueItem> getNextItemQueueItem(String queueName) throws SQLException {
		
		List<QueueItem> result = mySQLConnector.getNextQueueItem(queueName, getHostName());
		
		if (result != null) {
			
			for (QueueItem queueItem : result) {
				
				// zachowanie zgodnosci
				if (queueItem.getHostName() == null) {
					queueItem.setHostName(getHostName());
				}				
			}
			
		}
		
		return result;
	}
	
	public void setQueueItemDone(QueueItem queueItem) throws SQLException {
		
		queueItem.setStatus(QueueItemStatus.DONE);
		
		// uaktualnie wpisu
		mySQLConnector.updateQueueItem(queueItem);	
	}

	public void setQueueItemError(QueueItem queueItem) throws SQLException {
		
		queueItem.setStatus(QueueItemStatus.ERROR);
		
		// uaktualnie wpisu
		mySQLConnector.updateQueueItem(queueItem);	
	}
	
	public void delayQueueItem(QueueItem queueItem) throws SQLException {
		
		int deliveryCount = queueItem.getDeliveryCount() + 1;
		Timestamp nextAttempt = queueItem.getNextAttempt();
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(nextAttempt);
		
		calendar.add(Calendar.SECOND, 10 * deliveryCount * 2);
		
		nextAttempt = new Timestamp(calendar.getTime().getTime());
		
		queueItem.setDeliveryCount(deliveryCount);
		queueItem.setNextAttempt(nextAttempt);
		
		// uaktualnie wpisu
		mySQLConnector.updateQueueItem(queueItem);		
	}

	public void processLocalDirQueueItems() {
		
		// proba znalezienia plikow z lokalnego katalogu kolejki
		
		File[] queueItemsFileReadyList = localDirJobQueueDirFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				
				if (pathname.isFile() == false) {
					return false;
				}
				
				String fileName = pathname.getName();
				
				if (fileName.endsWith(".ready") == true) {
					return true;
				}
				
				return false;
			}
		});
		
		if (queueItemsFileReadyList != null && queueItemsFileReadyList.length > 0) {
			
			// logger.info("Znaleziono pliki z lokalnej kolejki");
			
			for (File currentReadyQueueItemFile : queueItemsFileReadyList) {
				
				File queueItemFile = new File(currentReadyQueueItemFile.getParent(), currentReadyQueueItemFile.getName().substring(0, currentReadyQueueItemFile.getName().length() - ".ready".length()));
				
				logger.info("Przetwarzam plik " + queueItemFile.getName());
				
				ObjectInputStream ois = null;
				
				QueueItem queueItem = null;
				
				try {					
					
					ois = new ObjectInputStream(new FileInputStream(queueItemFile));
					
					queueItem = (QueueItem)ois.readObject();
					
				} catch (Exception e) {
					
					logger.error("Błąd podczas odczytywania pliku z lokalnej kolejki: " + queueItemFile.getName(), e);
										
					continue;
					
				} finally {
					
					if (ois != null) {
						
						try {
							ois.close();
							
						} catch (IOException e) {
							// noop
						}
					}					
				}
				
				// zachowanie zgodnosci
				if (queueItem.getHostName() == null) {
					queueItem.setHostName(getHostName());
				}
				
				try {
					// proba wstawienia do bazy danych		
					mySQLConnector.insertQueueItem(queueItem);	
					
				} catch (SQLException e) {					
					logger.error("Błąd wstawienia do bazy danych z lokalnej kolejki: " + e.getMessage());
					
					continue;
				}
				
				// skasowanie plikow z lokalnej kolejki
				currentReadyQueueItemFile.delete();
				queueItemFile.delete();
			}
		}
	}
}
