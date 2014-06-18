package pl.idedyk.japanese.dictionary.web.queue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItemStatus;

@Service
public class QueueService {

	@Autowired
	private MySQLConnector mySQLConnector;

	public void sendToQueue(String queueName, byte[] object) throws SQLException {
		
		// stworzenie nowego elementu do kolejki
		
		QueueItem queueItem = new QueueItem();
		
		queueItem.setName(queueName);
		queueItem.setStatus(QueueItemStatus.WAITING);
		queueItem.setSendTimestamp(new Timestamp(new Date().getTime()));
		queueItem.setDeliveryCount(0);
		queueItem.setNextAttempt(queueItem.getSendTimestamp());
		queueItem.setObject(object);
		
		// wstawienie do kolejki
		mySQLConnector.insertQueueItem(queueItem);		
	}
}
