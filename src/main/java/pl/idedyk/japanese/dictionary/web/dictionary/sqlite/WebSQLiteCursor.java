package pl.idedyk.japanese.dictionary.web.dictionary.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pl.idedyk.japanese.dictionary.api.dictionary.sqlite.Cursor;

public class WebSQLiteCursor implements Cursor {
	
	private PreparedStatement prepareStatement;
	
	private ResultSet resultSet;
		
	public WebSQLiteCursor(PreparedStatement prepareStatement, ResultSet resultSet) {
		this.prepareStatement = prepareStatement;
		this.resultSet = resultSet;
	}

	@Override
	public void close() {
		
		try {
			resultSet.close();
			prepareStatement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
	}

	@Override
	public int getInt(int idx) {
		
		try {
			return resultSet.getInt(idx + 1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}			
	}

	@Override
	public String getString(int idx) {
		try {
			return resultSet.getString(idx + 1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isAfterLast() {
		try {
			return resultSet.isAfterLast();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean moveToFirst() {
		try {
			if (resultSet.isBeforeFirst()) {
				return resultSet.next();
				
			} else {
				return resultSet.first();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean moveToNext() {
		try {
			return resultSet.next();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
