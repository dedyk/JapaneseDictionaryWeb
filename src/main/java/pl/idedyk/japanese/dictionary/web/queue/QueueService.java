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
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItemStatus;

@Service
public class QueueService {
	
	private static final Logger logger = LogManager.getLogger(QueueService.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Value("${local.dir.job.queue}")
	private String localDirJobQueueDir;
	
	private File localDirJobQueueDirFile;
	
	private File localDirJobQueryArchiveDirFile;
	
	private File newlocalDirJobQueueDirFile;
	
	//
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
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
		
		//
		
		localDirJobQueryArchiveDirFile = new File(localDirJobQueueDirFile, "archive");
		
		if (localDirJobQueryArchiveDirFile.exists() == false) {
			
			logger.info("Tworzę katalog " + localDirJobQueryArchiveDirFile.getPath() + " do archiwum lokalnej kolejki");
			
			localDirJobQueryArchiveDirFile.mkdirs();
		}
		
		if (localDirJobQueryArchiveDirFile.exists() == false || localDirJobQueryArchiveDirFile.canWrite() == false) {
			
			logger.error("Nie mogę zainicjalizować katalogu " + localDirJobQueryArchiveDirFile.getPath() + " do archiwum lokalnej kolejki");
			
			throw new RuntimeException();
		}
		
		// tymczasowe
		newlocalDirJobQueueDirFile = new File(localDirJobQueueDirFile.getParentFile(), localDirJobQueueDirFile.getName() + "_NEW");

		if (newlocalDirJobQueueDirFile.exists() == false) {
			newlocalDirJobQueueDirFile.mkdirs();
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		String dateString = sdf.format(queueItem.getSendTimestamp());
		
		String randomFileName = UUID.randomUUID().toString();
		
		// tymczasowe		
		File queueItemFileBody = new File(newlocalDirJobQueueDirFile, queueItem.getName() + "_" + dateString + "_" + randomFileName);
		File queueItemBodyReady = new File(newlocalDirJobQueueDirFile, queueItem.getName() + "_" + dateString + "_" + randomFileName + ".ready");
		
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
				
		Arrays.sort(queueItemsFileReadyList, new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				
				if (f1.lastModified() < f2.lastModified()) {
					return -1;
					
				} else if (f1.lastModified() > f2.lastModified()) {
					return 1;
					
				} else {
					return 0;
				}
			}
		});
				
		if (queueItemsFileReadyList != null && queueItemsFileReadyList.length > 0) {
			
			// logger.info("Znaleziono pliki z lokalnej kolejki");
						
			for (File currentReadyQueueItemFile : queueItemsFileReadyList) {
				
				File queueItemFile = new File(currentReadyQueueItemFile.getParent(), currentReadyQueueItemFile.getName().substring(0, currentReadyQueueItemFile.getName().length() - ".ready".length()));
				
				// logger.info("Przetwarzam plik " + queueItemFile.getName());
				
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
				
				// udalo sie, kasujemy plik ready
				currentReadyQueueItemFile.delete();
								
				// przenosimy plik do archiwum
								
				// sprawdzenie i ewentualne utworzenie katalogu z data				
				File localDirJobQueryArchiveDirWithDateFile = new File(localDirJobQueryArchiveDirFile, dateFormat.format(queueItem.getSendTimestamp()));
				
				if (localDirJobQueryArchiveDirWithDateFile.exists() == false && localDirJobQueryArchiveDirWithDateFile.isDirectory() == false) { // tworzymy katalog
					localDirJobQueryArchiveDirWithDateFile.mkdir();
				}
				
				// przenosimy plik do wspolnego archiwum
				FileSystem archiveFileSystem = null;
				
				try {
					/*
					// utworzenie nazwy pliku z archiwum					
					Calendar querySendTimestampCalendar = Calendar.getInstance();
					
					querySendTimestampCalendar.setTime(queueItem.getSendTimestamp());
					
					int sendTimestampHourOfDay = querySendTimestampCalendar.get(Calendar.HOUR_OF_DAY);
										
					String archivePartFileName = dateFormat.format(querySendTimestampCalendar.getTime()) + "_" + (sendTimestampHourOfDay < 10 ? "0" + sendTimestampHourOfDay : sendTimestampHourOfDay) 
							+ "_" + (querySendTimestampCalendar.get(Calendar.MINUTE) / 10) + "0";		
					
					//
					
					File archiveFile = new File(localDirJobQueryArchiveDirWithDateFile, archivePartFileName + ".zip");
									
					URI archiveFileUri = URI.create("jar:file:" + archiveFile.getAbsolutePath());
					
					// utworzenie archiwum												
					Map<String, String> archiveEnv = new HashMap<>();
					
					archiveEnv.put("create", String.valueOf(archiveFile.exists() == false));

					archiveFileSystem = FileSystems.newFileSystem(archiveFileUri, archiveEnv);
					
					// przenoszenie pliku do archiwum						
		            Path queueItemFilePathInArchiveFile = archiveFileSystem.getPath(queueItemFile.getName());
		            
		            Files.copy(queueItemFile.toPath(), queueItemFilePathInArchiveFile, StandardCopyOption.REPLACE_EXISTING); 					
					*/
					
				} catch (Exception e) {
					logger.error("Błąd podczas przenoszenia pliku do archiwum: " + e.getMessage());
					
				} finally {
					
					if (archiveFileSystem != null) {
						
						try {
							archiveFileSystem.close();
							
						} catch (IOException e) {
							logger.error("Błąd podczas przenoszenia pliku do archiwum: " + e.getMessage());
						}
					}						
				}
				
				// kasujemy plik				
				queueItemFile.delete();
			}
		}
	}
	
	/*
	private void copyAndGzipFile(File source, File destination) throws IOException {
		
		byte[] buffer = new byte[1024];
		
		FileInputStream sourceInputStream = null;
		GZIPOutputStream destinationOutputStream = null;
		
		try {

			sourceInputStream = new FileInputStream(source);
			
			destinationOutputStream = new GZIPOutputStream(new FileOutputStream(destination));

	        int len;
	        
	        while ((len = sourceInputStream.read(buffer)) > 0) {
	        	destinationOutputStream.write(buffer, 0, len);
	        }
	        
	    } finally {
	    	
	    	if (sourceInputStream != null) {
	    		sourceInputStream.close();
	    	}
	    	
	    	if (destinationOutputStream != null) {
	    		destinationOutputStream.finish();
	    		destinationOutputStream.close();
	    	}	    	
	    }		
	}
	*/

	public void deleteLocalDirArchiveOldQueueItems(final int olderThanDays) {
		
		// pobieramy liste plikow do skasowania
		File[] oldQueueItemsDirListFiles = localDirJobQueryArchiveDirFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				
				if (pathname.isFile() == true) {
					return false;
				}
				
				if (pathname.isDirectory() == false) {
					return false;
				}

				Date pathnameDate = null;
				
				try {
					pathnameDate = dateFormat.parse(pathname.getName());
					
				} catch (ParseException e) { // zly format nazwy katalogu
					return false;
				}
				
				// nazwa katalogu jest w odpowiednim formacie, wiec sprawdzamy liczbe dni
				
				Calendar calendarNowMinusDays = Calendar.getInstance();
				
				calendarNowMinusDays.add(Calendar.DAY_OF_YEAR, -olderThanDays);
				
				if (calendarNowMinusDays.getTime().getTime() > pathnameDate.getTime()) { // kasujemy
					return true;
					
				} else {
					return false;
				}				
			}
		});
		
		// kasujemy pliki
		for (File directoryToDelete : oldQueueItemsDirListFiles) {
			
			logger.info("Kasuje katalog archiwum: " + directoryToDelete.getName());
			
			// najpierw kasujemy pliki z tego katalogu
			File[] directoryToDeleteListFiles = directoryToDelete.listFiles();
			
			for (File fileToDelete : directoryToDeleteListFiles) {				
				fileToDelete.delete();
			}
			
			// a pozniej sam katalog (powinien byc) pusty
			directoryToDelete.delete();
		}
	}
	
	//
	
	/*
	public static void main(String[] args) {
		
		org.apache.log4j.BasicConfigurator.configure();
		
		QueueService queueService = new QueueService();
		
		queueService.localDirJobQueueDir = "/opt/apache-tomcat-8.0.8/local-job-queue";
		queueService.init(); 
		
		queueService.deleteLocalDirArchiveOldQueueItems(10);
	}
	*/
}
