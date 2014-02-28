package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult.ResultItem;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

@Controller
public class WordDictionaryController extends CommonController {
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@RequestMapping(value = "/wordDictionary", method = RequestMethod.GET)
	public String start(Map<String, Object> model) {
		
		/*
		Connection conn = null;
		try {
		    Context ctx = new InitialContext();
		    
		    Context envContext  = (Context) ctx.lookup("java:/comp/env");
		    DataSource ds = (DataSource) envContext.lookup("jdbc/sqlite");
		    
		    conn = ds.getConnection();
		    
		    System.out.println("RRRRRRR: " + conn.isReadOnly());
		    
		    PreparedStatement prepareStatement = conn.prepareStatement("select * from DictionaryEntries");

		    ResultSet executeQuery = prepareStatement.executeQuery();
		    
		    // TESTY !!!!!!!!!!
		    
		    while(executeQuery.next()) {
		    	
		    	String a = executeQuery.getString(1);
		    	String b = executeQuery.getString(3);
		    	
		    	System.out.println(a + " - " + b);
		    	
		    }
		    
		    executeQuery.close();
		    prepareStatement.close();
		} catch (Exception e) { 
		     throw new RuntimeException(e);
		}
		*/
		
		
		//webSQLiteDatabase.execSQL("aaaaa");
		
		//Cursor cursor = webSQLiteDatabase.rawQuery("select * from DictionaryEntries where info like ?", new String[] { "%ko%" });
		
		/*
		Cursor cursor = webSQLiteDatabase.query(false, "KOTY", new String[] { "col1", "col2", "col3" }, 
				"col1 < ? and col2 > ?", new String[] { "666", "777" }, "col4", "count(*) > 5 ", "col1, col2", "333");
		
		/*
		System.out.println("AAA: " + cursor.moveToFirst());

		while (!cursor.isAfterLast()) {
			
	    	String a = cursor.getString(1);
	    	String b = cursor.getString(3);
	    	String c = cursor.getString(5);
	    	
	    	System.out.println(a + " - " + b + " - " + c);
	    	
	    	System.out.println("BBB: " + cursor.moveToNext());
	    }
		
		cursor.close();
		*/
		
		long start = System.currentTimeMillis();
		
		try {			
			FindWordRequest findWordRequest = new FindWordRequest();
			
			findWordRequest.word = "kot";
			findWordRequest.wordPlaceSearch = WordPlaceSearch.START_WITH;
			
			long findStart = System.currentTimeMillis();
			FindWordResult findDictionaryEntries = dictionaryManager.findWord(findWordRequest);
			long findStop = System.currentTimeMillis();
			
			System.out.println("Czas(find): " + (findStop - findStart));
			
			for (ResultItem resultItem : findDictionaryEntries.result) {
				
				System.out.println(resultItem.getKanji() + " - " + resultItem.getKanaList() + " - " + 
				resultItem.getRomajiList() + " - " + resultItem.getTranslates() + " - " + resultItem.getInfo());
				
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			//sqliteConnector.close();
		}
		
		long stop = System.currentTimeMillis();
		
		System.out.println("Czas: " + (stop - start));
		
		return "wordDictionary";
	}
	
}
