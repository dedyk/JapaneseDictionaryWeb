package pl.idedyk.japanese.dictionary.web.dictionary.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.api.dictionary.sqlite.Cursor;
import pl.idedyk.japanese.dictionary.api.dictionary.sqlite.SQLiteDatabase;

@Service
public class WebSQLiteDatabase implements SQLiteDatabase {
	
	private Connection connection = null;
	
	@Override
	public void open() {
		
		try {
			if (connection == null || connection.isClosed() == true) {
				// init db driver
				Class.forName("org.sqlite.JDBC");
				
				connection = DriverManager.getConnection("jdbc:sqlite:../webapps/JapaneseDictionaryWeb/WEB-INF/classes/db/dictionary.db");

				connection.setReadOnly(true);
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		
		if (connection != null) {			
			try {
				connection.close();
				
				connection = null;
				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public void beginTransaction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void endTransaction() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setTransactionSuccessful() {
		throw new UnsupportedOperationException();		
	}

	@Override
	public void execSQL(String sql) {
		
		open();
			
		try {			
			connection.nativeSQL(sql);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long insertOrThrow(String arg0, String arg1, Map<String, Object> arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
	}
	
	@Override
	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}
	
	@Override
	public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		
		open();
		
		StringBuffer query = new StringBuffer();
		
		query.append("select ");
		
		if (distinct == true) {
			query.append(" distinct ");
		}
				
		for (int idxColumn = 0; idxColumn < columns.length; ++idxColumn) {
			
			query.append(columns[idxColumn]);
			
			if (idxColumn != columns.length - 1) {
				query.append(" , ");
				
			} else {
				query.append(" ");
			}
		}
		
		query.append(" from ").append(table).append(" ");
		
		if (selection != null) {
			query.append(" where ").append(selection).append(" ");
		}
		
		if (groupBy != null) {
			query.append(" group by ").append(groupBy).append(" ");
		}
		
		if (having != null) {
			query.append(" having ").append(having).append(" ");
		}

		if (orderBy != null) {
			query.append(" order by ").append(orderBy).append(" ");
		}

		if (limit != null) {
			query.append(" limit ").append(limit).append(" ");
		}
		
		return rawQuery(query.toString(), selectionArgs);
	}

	@Override
	public Cursor rawQuery(String sql, String[] params) {
		
		open();
		
		PreparedStatement prepareStatement = null;
		
		try {			
			prepareStatement = connection.prepareStatement(sql);
			
			for (int idx = 0; params != null && idx < params.length; ++idx) {
				prepareStatement.setString(idx + 1, params[idx]);
			}
			
			return new WebSQLiteCursor(prepareStatement, prepareStatement.executeQuery());
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
