package pl.idedyk.japanese.dictionary.web.taglib;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Span;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.KanjiKanaPair;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSense;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSenseEntry;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSenseEntryGloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Gloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict.Entry;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiAdditionalInfoEnum;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingAdditionalInfoEnum;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfoKanaType;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Sense;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.SenseAdditionalInfo;

public class FindWordResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindWordRequest findWordRequest;
	
	private FindWordResult.ResultItem resultItem;
	private int resultItemIndex;
	
	private MessageSource messageSource;
	
	private DictionaryManager dictionaryManager;
		
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		/*
		ServletRequest servletRequest = pageContext.getRequest();
		
		String userAgent = null;
		
		if (servletRequest instanceof HttpServletRequest) {			
			HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
			
			userAgent = httpServletRequest.getHeader("User-Agent");			
		}
		*/
				
		//
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
		
		try {
            JspWriter out = pageContext.getOut();
            
            // FM_FIXME: przerobic, jedno pod drugim !!!
            
            // pobranie danych
            String findWord = findWordRequest.word;
                        
            // tylko jeden z nich bedzie wypelniony
            Entry entry = resultItem.getEntry();
            DictionaryEntry oldDictionaryEntry = resultItem.getOldDictionaryEntry(); // tylko dla slowek ze slownika nazw, FM_FIXME: obsluga tego elementu
            
            if (entry != null) {
            	// wygenerowanie wszystkich kombinacji
            	List<KanjiKanaPair> kanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(entry);
            	
            	// przefiltrowanie, aby nie pokazywac elementow, ktore sluza jedynie do wyszukiwania
            	kanjiKanaPairList = kanjiKanaPairList.stream().filter(kanjiKanaPair -> {
            		KanjiInfo kanjiInfo = kanjiKanaPair.getKanjiInfo();
            		
            		if (kanjiInfo != null && kanjiInfo.getKanjiAdditionalInfoList().contains(KanjiAdditionalInfoEnum.SEARCH_ONLY_KANJI_FORM) == true) {
            			return false;
            		}
            		
            		ReadingInfo readingInfo = kanjiKanaPair.getReadingInfo();
            		
            		if (readingInfo.getReadingAdditionalInfoList().contains(ReadingAdditionalInfoEnum.SEARCH_ONLY_KANA_FORM) == true) {
            			return false;
            		}            		
            		
            		return true;
            		
            	}).collect(Collectors.toList());

            	// wiersz ze slowami
                Tr tr = new Tr();
                tr.setStyle("padding-bottom: 50px; " + (resultItemIndex % 2 == 0 ? "background-color: #f9f9f9" : ""));
            	            	
            	// pokazanie wszystkich slow
                {    			
	    			// tytul
	    			Td readingTitleTd = new Td();    	    			
	    			readingTitleTd.setStyle("padding-right: 30px; font-weight:bold; font-size: 120%; vertical-align: top; width: auto");
	    	    	     	    	    	
	    			readingTitleTd.addHtmlElement(new Text(messageSource.getMessage("wordDictionary.page.search.table.column.word", new Object[] { }, Locale.getDefault())));
                	
	    			tr.addHtmlElement(readingTitleTd);
	    			
	    			//                	
                	
                	Td readingTd = new Td(null, "font-size: 125%; padding-right: 30px; vertical-align: top; width: 100%");
                	
                	// Table readingTdTable = new Table(null, "width: 50%");
                	
                	for (KanjiKanaPair kanjiKanaPair : kanjiKanaPairList) {
                    	
                    	// pobieramy wszystkie skladniki slowa
                    	String kanji = kanjiKanaPair.getKanji();
                    	String kana = kanjiKanaPair.getKana();
                    	String romaji = kanjiKanaPair.getRomaji();
                    	
                    	// Tr readingTdTableTr = new Tr();
                    	
                    	// kanji
                    	/*
                    	Td readingTdTableTrKanjiTd = new Td(null, "font-size: 125%; width: 20%; padding-right: 45px; padding-bottom: 15px");
                    	
                    	if (kanji != null) {
                    		readingTdTableTrKanjiTd.addHtmlElement(new Text(getStringWithMark(kanji, findWord, findWordRequest.searchKanji)));
                    	}
                    	
                    	// kana
                    	Td readingTdTableTrKanaTd = new Td(null, "font-size: 125%; width: 20%; padding-right: 45px; padding-bottom: 15px");
                    	readingTdTableTrKanaTd.addHtmlElement(new Text(getStringWithMark(kana, findWord, findWordRequest.searchKana)));
                    	
                    	// romaji
                    	Td readingTdTableTrRomajiTd = new Td(null, "font-size: 125%; width: 20%; padding-right: 45px; padding-bottom: 15px");
                    	readingTdTableTrRomajiTd.addHtmlElement(new Text(getStringWithMark(romaji, findWord, findWordRequest.searchRomaji)));
                    	
                    	// dodanie elementow                    	
                    	readingTdTableTr.addHtmlElement(readingTdTableTrKanjiTd);
                    	readingTdTableTr.addHtmlElement(readingTdTableTrKanaTd);
                    	readingTdTableTr.addHtmlElement(readingTdTableTrRomajiTd);
                    	
                    	readingTdTable.addHtmlElement(readingTdTableTr);
                    	*/
                    	
                    	Div singleReadingDiv = new Div(null, "padding-bottom: 10px");
                    	                   
                    	// kanji
                    	if (kanji != null) {
                    		Span singleReadingKanjiSpan = new Span(null, "padding-right: 40px");
                    		
                    		singleReadingKanjiSpan.addHtmlElement(new Text(getStringWithMark(kanji, findWord, findWordRequest.searchKanji)));                    		
                    		singleReadingDiv.addHtmlElement(singleReadingKanjiSpan);
                    	}
                    	
                    	// kana
                    	Span singleReadingKanaSpan = new Span(null, "padding-right: 40px");
                    	
                    	singleReadingKanaSpan.addHtmlElement(new Text(getStringWithMark(kana, findWord, findWordRequest.searchKana)));                    		
                		singleReadingDiv.addHtmlElement(singleReadingKanaSpan);

                		// romaji
                		Span singleReadingRomajiSpan = new Span(null, "padding-right: 40px");
                    	
                    	singleReadingRomajiSpan.addHtmlElement(new Text(getStringWithMark(romaji, findWord, findWordRequest.searchRomaji)));                    		
                		singleReadingDiv.addHtmlElement(singleReadingRomajiSpan);
                    	                    	
                    	// fullWord.append(kana).append(" (").append(romaji).append(")");
                    	                        
                    	// singleReadingTd.addHtmlElement(new Text(getStringWithMark(fullWord.toString(), findWord, true)));
            	    	
            	    	readingTd.addHtmlElement(singleReadingDiv);
					}
                	
                	// readingTd.addHtmlElement(readingTdTable);
                	                	
                	tr.addHtmlElement(readingTd);
                }
                
                // renderowanie slow
                tr.render(out);
                
            	// wiersz ze znaczeniem
                tr = new Tr();
                tr.setStyle("padding-bottom: 50px; " + (resultItemIndex % 2 == 0 ? "background-color: #f9f9f9" : ""));
                
                // znaczenie
                {
	    			// tytul
	    			Td translateTitleTd = new Td();    	    			
	    			translateTitleTd.setStyle("padding-right: 30px; font-weight:bold; font-size: 120%; vertical-align: top; width: auto");
	    	    	     	    	    	
	    			translateTitleTd.addHtmlElement(new Text(messageSource.getMessage("wordDictionary.page.search.table.column.translate", new Object[] { }, Locale.getDefault())));
                	
	    			tr.addHtmlElement(translateTitleTd);
	    			
	    			//
	    			
	    			Td translateTd = new Td(null, "font-size: 125%; width: 50%");  
	    			tr.addHtmlElement(translateTd);
	    				    			
	                for (int senseIdx = 0; senseIdx < entry.getSenseList().size(); ++senseIdx) {
	                	
	                	Sense sense = entry.getSenseList().get(senseIdx);
	                	                
	                	// znaczenia
	                	List<Gloss> polishGlossList = Dictionary2HelperCommon.getPolishGlossList(sense.getGlossList());
	                	SenseAdditionalInfo polishAdditionalInfo = Dictionary2HelperCommon.findFirstPolishAdditionalInfo(sense.getAdditionalInfoList());
	                	
	                	//                	
	                	                	
	    				for (int currentGlossIdx = 0; currentGlossIdx < polishGlossList.size(); ++currentGlossIdx) {
	    					
	    					Gloss gloss = polishGlossList.get(currentGlossIdx);
	    											
	    					translateTd.addHtmlElement(new Text(getStringWithMark(
	    							gloss.getValue(), findWord, true) + 
	    							(gloss.getGType() != null ? " (" + gloss.getGType() + ")" : "") + 
	    							(currentGlossIdx != sense.getGlossList().size() - 1 ? "<br/>" : "")));						
	    				}
	    				    				
	    				// informacje dodatkowe												
	    				if (polishAdditionalInfo != null) {					
	    					Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify");
	    					
	    	    			infoDiv.addHtmlElement(new Text(getStringWithMark(polishAdditionalInfo.getValue(), findWord, true)));
	    	    			
	    	    			translateTd.addHtmlElement(infoDiv);						
	    				}
	    				
	    				// przerwa
	    				if (senseIdx != entry.getSenseList().size() - 1) {
	    					
	    					Div marginDiv = new Div(null, "margin-bottom: 12px;");
	    					
	    					translateTd.addHtmlElement(marginDiv);						
	    				}
					}
                	
                }
                
                // renderowanie znaczenia
                tr.render(out);

                
                
                /* 
                // pokazywanie znaczen
    	    	  	    	

                
                // FM_FIXME: zaznaczanie, ktore znaczenia sa dla pewnych elementow
                
                
                
                
                
                / *
                 * FM_FIXME: obsluga tych pol
                 * 
                "restrictedToKanjiList",
                "restrictedToKanaList",
                "partOfSpeechList",
                "referenceToAnotherKanjiKanaList",
                "antonymList",
                "fieldList",
                "miscList",
                "additionalInfoList",
                "languageSourceList",
                "dialectList",
                "glossList"	
                * /
                                

                            	
                tr.addHtmlElement(translateTd);
                */
            }
            
            // renderuj glowny wiersz
            
            
            // dodanie rozpychacza
            Tr tr = new Tr();
            tr.setStyle((resultItemIndex % 2 == 0 ? "background-color: #f9f9f9" : ""));
            
            Td spacerTd = new Td();
            
            spacerTd.setStyle("padding-bottom: 20px");            
            tr.addHtmlElement(spacerTd);
                
            tr.render(out);
            
            //            
	    	
	    	
	    	
	    	// stary kod
	    	//////////////////
	    	//////////////////
	    	//////////////////
	    	
            
            // FM_FIXME: do poprawy, zakomentowan
            // Tr tr = new Tr();
            
            // pobranie danych
            //
            /*
            
            
	    	String kanji = resultItem.getKanji();
	    	String prefixKana = resultItem.getPrefixKana();
	    	List<String> kanaList = resultItem.getKanaList();
	    	String prefixRomaji = resultItem.getPrefixRomaji();
	    	List<String> romajiList = resultItem.getRomajiList();
	    	List<String> translates = resultItem.getTranslates();
	    	String info = resultItem.getInfo();

	    	String tempPrefixKana = prefixKana != null && prefixKana.equals("") == false ? prefixKana : null;
	    	String tempPrefixRomaji = prefixRomaji != null && prefixRomaji.equals("") == false ? prefixRomaji : null;
            	    	        
			/*
	        // kanji
	    	Td kanjiTd = new Td();
	    	tr.addHtmlElement(kanjiTd);
	        
	    	if (resultItem.isKanjiExists() == true) {
	
	    		if (tempPrefixKana != null) {
	    			kanjiTd.addHtmlElement(new Text("(" + getStringWithMark(tempPrefixKana, findWord, false) + ") "));
	    		}
	
	    		kanjiTd.addHtmlElement(new Text(getStringWithMark(kanji, findWord, findWordRequest.searchKanji)));
	    	}
	
            	    	            
	    	// kana
	    	Td kanaTd = new Td();
	    	tr.addHtmlElement(kanaTd);
	    	
	    	if (kanaList != null && kanaList.size() > 0) {
	    		kanaTd.addHtmlElement(new Text(getStringWithMark(toString(kanaList, tempPrefixKana), findWord, findWordRequest.searchKana)));
	    	}
	    	
	    	// romaji
	    	Td romajiTd = new Td();
	    	tr.addHtmlElement(romajiTd);
	    	
	    	if (romajiList != null && romajiList.size() > 0) {
	    		romajiTd.addHtmlElement(new Text(getStringWithMark(toString(romajiList, tempPrefixRomaji), findWord, findWordRequest.searchRomaji)));
	    	}
	    	
	    	// translates i informacje dodatkowe
	    	
	    	// sprawdzenie czy wystepuja dane w nowym formacie
	    	JMdict.Entry dictionaryEntry2 = null;
	    	
	    	if (resultItem.getDictionaryEntry() != null && resultItem.getDictionaryEntry().isName() == false) {
	    		
				// pobieramy entry id
				Integer entryId = resultItem.getDictionaryEntry().getJmdictEntryId();

				if (entryId != null) {
					// pobieramy z bazy danych
					dictionaryEntry2 = dictionaryManager.getDictionaryEntry2ById(entryId);				
				}	    		
	    	}
	    	
	    	// nie ma danych w nowym formacie, generujemy po staremu
	    	if (dictionaryEntry2 == null) { 
	    		
		    	if (translates != null && translates.size() > 0) {
		    		translateTd.addHtmlElement(new Text(getStringWithMark(toString(translates, null), findWord, findWordRequest.searchTranslate)));
					
		    		// informcje dodatkowe
		    		if (info != null && info.equals("") == false) {
		    			
		    			Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify");
		    			
		    			infoDiv.addHtmlElement(new Text(getStringWithMark(info, findWord, findWordRequest.searchInfo)));
		    			
		    			translateTd.addHtmlElement(infoDiv);		    			
		    		}
		    	}
		    	
		    	// info
		    	/ *
		    	if (userAgent == null || Utils.isMobile(userAgent) == false) {	    	
		    		
			    	Td infoTd = new Td();
			    	tr.addHtmlElement(infoTd);
			    	
			    	if (info != null && info.equals("") == false) {
			    		infoTd.addHtmlElement(new Text(getStringWithMark(info, findWord, findWordRequest.searchInfo)));
			    	}
		    	}
		    	* /

	    	} else { // sa dane w nowym formacie
	    		
				List<KanjiKanaPair> kanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(dictionaryEntry2);
				
				// szukamy konkretnego znaczenia dla naszego slowa
				KanjiKanaPair dictionaryEntry2KanjiKanaPair = Dictionary2HelperCommon.findKanjiKanaPair(kanjiKanaPairList, resultItem.getDictionaryEntry());

				//
				
		    	PrintableSense printableSense = Dictionary2HelperCommon.getPrintableSense(dictionaryEntry2KanjiKanaPair);
				
				// mamy znaczenia
				for (int senseIdx = 0; senseIdx < printableSense.getSenseEntryList().size(); ++senseIdx) {
					
					PrintableSenseEntry printableSenseEntry = printableSense.getSenseEntryList().get(senseIdx);
										
					// czesci mowy
					/ *
					if (printableSenseEntry.getPolishPartOfSpeechValue() != null) {		
						
						Div polishPartOfSpeechDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify");
						
						polishPartOfSpeechDiv.addHtmlElement(new Text(printableSenseEntry.getPolishPartOfSpeechValue() + "<br/>"));
		    			
		    			translateTd.addHtmlElement(polishPartOfSpeechDiv);						
					}
					* /
					

				}
	    	}	    	
            
            // details link
	    	Td detailsLinkTd = new Td();
	    	tr.addHtmlElement(detailsLinkTd);
            
            String link = LinkGenerator.generateDictionaryEntryDetailsLink(pageContext.getServletContext().getContextPath(), resultItem.getDictionaryEntry(), null);
            A linkButton = new A();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage(
            		"wordDictionary.page.search.table.column.details.value", null, Locale.getDefault())));
            
            tr.render(out);
            // FM_FIXME: do poprawy - end
            */
            
            return SKIP_BODY;
 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
    private String getStringWithMark(String text, String findWord, boolean mark) {
    	
    	if (mark == false || findWord == null) {
    		return text;
    	}
    	
    	String findWordLowerCase = findWord.toLowerCase(Locale.getDefault());
    	
		StringBuffer texStringBuffer = new StringBuffer(text);								
		StringBuffer textLowerCaseStringBuffer = new StringBuffer(text.toLowerCase(Locale.getDefault()));
										
		int idxStart = 0;
		
		final String fontBegin = "<font color='red'>";
		final String fontEnd = "</font>";
		
		while(true) {
			
			int idx1 = textLowerCaseStringBuffer.indexOf(findWordLowerCase, idxStart);
			
			if (idx1 == -1) {
				break;
			}
			
			texStringBuffer.insert(idx1, fontBegin);
			textLowerCaseStringBuffer.insert(idx1, fontBegin);
			
			texStringBuffer.insert(idx1 + findWordLowerCase.length() + fontBegin.length(), fontEnd);
			textLowerCaseStringBuffer.insert(idx1 + findWordLowerCase.length() + fontBegin.length(), fontEnd);

			idxStart = idx1 + findWordLowerCase.length() + fontBegin.length() + fontEnd.length();
		}
		
		return texStringBuffer.toString();
    }
    
	private String toString(List<String> listString, String prefix) {
		
		StringBuffer sb = new StringBuffer();
				
		for (int idx = 0; idx < listString.size(); ++idx) {
			if (prefix != null) {
				sb.append("(").append(prefix).append(") ");
			}
			
			sb.append(listString.get(idx));
			
			if (idx != listString.size() - 1) {
				sb.append("<br/>");
			}
		}
				
		return sb.toString();
	}

	public FindWordResult.ResultItem getResultItem() {
		return resultItem;
	}

	public void setResultItem(FindWordResult.ResultItem resultItem) {
		this.resultItem = resultItem;
	}

	public FindWordRequest getFindWordRequest() {
		return findWordRequest;
	}

	public void setFindWordRequest(FindWordRequest findWordRequest) {
		this.findWordRequest = findWordRequest;
	}
	
	public void setResultItemIndex(int resultItemIndex) {
		this.resultItemIndex = resultItemIndex;
	}
}
