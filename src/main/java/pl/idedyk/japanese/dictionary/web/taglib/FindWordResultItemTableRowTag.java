package pl.idedyk.japanese.dictionary.web.taglib;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Span;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.KanjiKanaPair;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Gloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict.Entry;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiAdditionalInfoEnum;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.LanguageSource;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingAdditionalInfoEnum;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Sense;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.SenseAdditionalInfo;

public class FindWordResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindWordRequest findWordRequest;
	
	private FindWordResult.ResultItem resultItem;
	
	private MessageSource messageSource;
		
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
		
        // FM_FIXME: zaznaczanie, ktore znaczenia sa dla pewnych elementow
        
		// FM_FIXME: wersja na telefon
		
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		
		try {
            JspWriter out = pageContext.getOut();
            
            Tr tr = new Tr();            
            
            // pobranie danych
            //
            String findWord = findWordRequest.word;
            
            // tylko jeden z nich bedzie wypelniony
            Entry entry = resultItem.getEntry();
            DictionaryEntry oldDictionaryEntry = resultItem.getOldDictionaryEntry();

            if (entry != null) {
            	// wygenerowanie wszystkich kombinacji
            	List<KanjiKanaPair> kanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(entry, true);
            	            	
            	// slowo
    	    	Td wordTd = new Td();    	    	
    	    	tr.addHtmlElement(wordTd);
    	    	
    	    	Div wordDiv = new Div(null, "width: 100%");
    	    	wordTd.addHtmlElement(wordDiv);
    	    	
    	    	for (int kanjiKanaPairIdx = 0; kanjiKanaPairIdx < kanjiKanaPairList.size(); ++kanjiKanaPairIdx) {
    	    		
    	    		KanjiKanaPair kanjiKanaPair = kanjiKanaPairList.get(kanjiKanaPairIdx);
                	
    	    		KanjiInfo kanjiInfo = kanjiKanaPair.getKanjiInfo();
    	    		ReadingInfo readingInfo = kanjiKanaPair.getReadingInfo();
    	    		
    	    		boolean isKanjiSearchOnly = kanjiInfo != null && kanjiInfo.getKanjiAdditionalInfoList().contains(KanjiAdditionalInfoEnum.SEARCH_ONLY_KANJI_FORM) == true;
    	    		boolean isKanaSearchOnly = readingInfo.getReadingAdditionalInfoList().contains(ReadingAdditionalInfoEnum.SEARCH_ONLY_KANA_FORM) == true;
    	    		
    	    		// czy ten element zawiera kanji i kana tylko do wyszukiwania
    	    		if (isKanjiSearchOnly == true && isKanaSearchOnly == true) {
    	    			continue;
    	    		}
    	    		
    	    		// gdy kana jest tylko do wyszukiwania to nie pokazuj
    	    		if (isKanaSearchOnly == true) {
    	    			continue;
    	    		}    	    		
    	    		
    	    		// pobieramy wszystkie skladniki slowa
    	    		String kanji = null;
    	    		String kana = null;
    	        	String romaji = null;
    	    		
    	    		if (isKanjiSearchOnly == false) {
    	    			kanji = kanjiKanaPair.getKanji();	
    	    		}
    	    		
    	    		if (isKanaSearchOnly == false) {
        	        	kana = kanjiKanaPair.getKana();
        	        	romaji = kanjiKanaPair.getRomaji();    	    			
    	    		}    	        	
    	        	
            		Div singleWordDiv = createWordColumn(findWordRequest, findWord, kanji, kana, romaji);
            		                	
                	wordDiv.addHtmlElement(singleWordDiv);
				}
            	            	
            	// znaczenie
    	    	Td translateTd = new Td(null, "padding-top: 10px");
    	    	tr.addHtmlElement(translateTd);
            	
                for (int senseIdx = 0; senseIdx < entry.getSenseList().size(); ++senseIdx) {
                	
                	Sense sense = entry.getSenseList().get(senseIdx);
                	
					// ograniczone do kanji/kana					
					if (sense.getRestrictedToKanjiList().size() > 0 || sense.getRestrictedToKanaList().size() > 0) {
						List<String> restrictedToKanjiKanaList = new ArrayList<>();
						
						restrictedToKanjiKanaList.addAll(sense.getRestrictedToKanjiList());
						restrictedToKanjiKanaList.addAll(sense.getRestrictedToKanaList());
						
						Div restrictedToKanjiKanaDiv = new Div(null, "font-size: 75%; margin-top: 3px; text-align: justify");
												
						// zamiana na przetlumaczona postac
						String restrictedToKanjiKanaString = "・" + messageSource.getMessage("wordDictionary.page.search.table.column.details.restrictedKanjiKana", null, Locale.getDefault()) + " " + String.join("; ", restrictedToKanjiKanaList);
												
						restrictedToKanjiKanaDiv.addHtmlElement(new Text(restrictedToKanjiKanaString + "<br/>"));
		    			
		    			translateTd.addHtmlElement(restrictedToKanjiKanaDiv);
					}
										
					// czesci mowy
					if (sense.getPartOfSpeechList().size() > 0) { 
						Div polishPartOfSpeechDiv = new Div(null, "font-size: 75%; margin-top: 3px; text-align: justify");
						
						// zamiana na przetlumaczona postac
						String translatedToPolishPartOfSpeechEnum = "・" + String.join("; ", Dictionary2HelperCommon.translateToPolishPartOfSpeechEnum(sense.getPartOfSpeechList()));
												
						polishPartOfSpeechDiv.addHtmlElement(new Text(translatedToPolishPartOfSpeechEnum + "<br/>"));
		    			
		    			translateTd.addHtmlElement(polishPartOfSpeechDiv);						
					}
					
					// kategoria slowa
					if (sense.getFieldList().size() > 0) {
						Div fieldsDiv = new Div(null, "font-size: 75%; margin-top: 3px; text-align: justify");
						
						// zamiana na przetlumaczona postac
						String translatedfieldEnum = "・" + String.join("; ", Dictionary2HelperCommon.translateToPolishFieldEnumList(sense.getFieldList()));
												
						fieldsDiv.addHtmlElement(new Text(translatedfieldEnum + "<br/>"));
		    			
		    			translateTd.addHtmlElement(fieldsDiv);						
					}
					
					// roznosci
					if (sense.getMiscList().size() > 0) {
						Div miscDiv = new Div(null, "font-size: 75%; margin-top: 3px; text-align: justify");
						
						// zamiana na przetlumaczona postac
						String translatedMiscEnum = "・" + String.join("; ", Dictionary2HelperCommon.translateToPolishMiscEnumList(sense.getMiscList()));
												
						miscDiv.addHtmlElement(new Text(translatedMiscEnum + "<br/>"));
		    			
		    			translateTd.addHtmlElement(miscDiv);						
					}	
					
					// dialekt
					if (sense.getDialectList().size() > 0) {
						Div dialectDiv = new Div(null, "font-size: 75%; margin-top: 3px; text-align: justify");
						
						// zamiana na przetlumaczona postac
						String translatedDialectEnum = "・" + String.join("; ", Dictionary2HelperCommon.translateToPolishDialectEnumList(sense.getDialectList()));
												
						dialectDiv.addHtmlElement(new Text(translatedDialectEnum + "<br/>"));
		    			
		    			translateTd.addHtmlElement(dialectDiv);						
					}	
					
					// zagraniczne pochodzenie slowa
					if (sense.getLanguageSourceList().size() > 0) {
						Div languageSourceDiv = new Div(null, "font-size: 75%; margin-top: 3px; text-align: justify");
						
						// zamiana na przetlumaczona postac
						List<String> singleLanguageSourceList = new ArrayList<>();
						
						for (LanguageSource languageSource : sense.getLanguageSourceList()) {
														
							StringBuffer singleLanguageSource = new StringBuffer();
							
							String languageCodeInPolish = Dictionary2HelperCommon.translateToPolishLanguageCode(languageSource.getLang());
							String languageValue = languageSource.getValue();
							String languageLsWasei = Dictionary2HelperCommon.translateToPolishLanguageSourceLsWaseiEnum(languageSource.getLsWasei());
							
							if (languageValue != null) {
								singleLanguageSource.append(languageCodeInPolish + ": " + languageValue);
								
							} else {
								singleLanguageSource.append(Dictionary2HelperCommon.translateToPolishLanguageCodeWithoutValue(languageSource.getLang()));
							}
							
							if (languageLsWasei != null) {
								singleLanguageSource.append(", ").append(languageLsWasei);
							}

							singleLanguageSourceList.add(singleLanguageSource.toString());							
						}
														
						String joinedLanguageSource = "・" + String.join("; ", singleLanguageSourceList);
												
						languageSourceDiv.addHtmlElement(new Text(joinedLanguageSource + "<br/>"));
		    			
		    			translateTd.addHtmlElement(languageSourceDiv);						
					}
					
					// znaczenie
                	List<Gloss> polishGlossList = Dictionary2HelperCommon.getPolishGlossList(sense.getGlossList());
                	SenseAdditionalInfo polishAdditionalInfo = Dictionary2HelperCommon.findFirstPolishAdditionalInfo(sense.getAdditionalInfoList());
                	
                	//                	
                	                	
    				for (int currentGlossIdx = 0; currentGlossIdx < polishGlossList.size(); ++currentGlossIdx) {
    					
    					Gloss gloss = polishGlossList.get(currentGlossIdx);
    											
    					translateTd.addHtmlElement(new Text(getStringWithMark(
    							gloss.getValue(), findWord, true) + 
    							(gloss.getGType() != null ? " (" + Dictionary2HelperCommon.translateToPolishGlossType(gloss.getGType()) + ")" : "") + 
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
    					
    					Div marginDiv = new Div(null, "margin-bottom: 17px;");
    					
    					translateTd.addHtmlElement(marginDiv);						
    				}
				}   
                                
            } else if (oldDictionaryEntry != null) { // obsluga starego formatu
            	
            	// slowo
    	    	Td wordTd = new Td();    	    	
    	    	tr.addHtmlElement(wordTd);
    	    	
    	    	Div wordDiv = new Div(null, "width: 100%");
    	    	wordTd.addHtmlElement(wordDiv);
    	    	
    	    	String kanji = oldDictionaryEntry.getKanji();
    	    	String kana = oldDictionaryEntry.getKana();
	        	String romaji = oldDictionaryEntry.getRomaji();
	        	
        		Div singleWordDiv = createWordColumn(findWordRequest, findWord, kanji, kana, romaji);
        		                	
            	wordDiv.addHtmlElement(singleWordDiv);
            	
            	// znaczenie
    	    	Td translateTd = new Td(null, "padding-top: 10px");
    	    	tr.addHtmlElement(translateTd);
            	
            	List<String> translates = oldDictionaryEntry.getTranslates();
            	String info = oldDictionaryEntry.getInfo();
            	
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
		    	/*
		    	if (userAgent == null || Utils.isMobile(userAgent) == false) {	    	
		    		
			    	Td infoTd = new Td();
			    	tr.addHtmlElement(infoTd);
			    	
			    	if (info != null && info.equals("") == false) {
			    		infoTd.addHtmlElement(new Text(getStringWithMark(info, findWord, findWordRequest.searchInfo)));
			    	}
		    	}
		    	*/
		    	
            } else { // to nigdy nie powinno wydarzyc sie
            	throw new RuntimeException();
            }
            
            // details link
	    	Td detailsLinkTd = new Td();
	    	tr.addHtmlElement(detailsLinkTd);
	    	
            String link = ""; // FM_FIXME: do poprawy: LinkGenerator.generateDictionaryEntryDetailsLink(pageContext.getServletContext().getContextPath(), resultItem.getDictionaryEntry(), null);
            A linkButton = new A();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage(
            		"wordDictionary.page.search.table.column.details.value", null, Locale.getDefault())));
                                    
            tr.render(out);
            
            return SKIP_BODY;
 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private Div createWordColumn(FindWordRequest findWordRequest, String findWord, String kanji, String kana, String romaji) {
		Div singleWordDiv = new Div(null, "display: flex; width: 100%; ");
		    	                	
    	// kanji
    	Span singleWordKanjiSpan = new Span(null, "width: 33%; padding: 5px 15px 10px 0px; overflow-wrap: break-word;");
    	
    	if (kanji != null) {
    		singleWordKanjiSpan.addHtmlElement(new Text(getStringWithMark(kanji, findWord, findWordRequest.searchKanji)));
    	}
    	
    	// kana
    	Span singleWordKanaSpan = new Span(null, "width: 33%; padding: 5px 15px 10px 0px; overflow-wrap: break-word;");
    	singleWordKanaSpan.addHtmlElement(new Text(getStringWithMark(kana, findWord, findWordRequest.searchKana)));
    	
    	// romaji
    	Span singleWordRomajiSpan = new Span(null, "width: 33%; padding: 5px 0px 10px 0px; overflow-wrap: break-word;");
    	singleWordRomajiSpan.addHtmlElement(new Text(getStringWithMark(romaji, findWord, findWordRequest.searchRomaji)));
    	
    	// dodanie elementow                    	
    	singleWordDiv.addHtmlElement(singleWordKanjiSpan);
    	singleWordDiv.addHtmlElement(singleWordKanaSpan);
    	singleWordDiv.addHtmlElement(singleWordRomajiSpan);
    	
    	return singleWordDiv;		
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
}
