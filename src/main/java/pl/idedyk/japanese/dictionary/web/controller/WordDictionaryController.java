package pl.idedyk.japanese.dictionary.web.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WordDictionaryController {

	@RequestMapping(value = "/wordDictionary", method = RequestMethod.GET)
	public String start(Map<String, Object> model) {
		
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
		
		
		return "wordDictionary";
	}
	
}
