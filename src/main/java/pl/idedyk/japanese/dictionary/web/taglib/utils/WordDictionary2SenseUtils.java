package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.HtmlElementCommon;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Gloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.LanguageSource;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Sense;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.SenseAdditionalInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict.Entry;

public class WordDictionary2SenseUtils {
	
	public static void createSenseHtmlElements(MessageSource messageSource, String servletContextPath, Entry entry, HtmlElementCommon translateTd, String findWord, boolean addSenseNumber, boolean addDetails) {
        for (int senseIdx = 0; senseIdx < entry.getSenseList().size(); ++senseIdx) {
        	
        	Sense sense = entry.getSenseList().get(senseIdx);
        	
        	if (addSenseNumber == true) {
				// numer znaczenia
				Div senseNoDiv = new Div("col-md-1");
				
				H senseNoDivH = new H(4, null, "margin-top: 3px; text-align: right");
				
				senseNoDivH.addHtmlElement(new Text("" + (senseIdx + 1)));				
				senseNoDiv.addHtmlElement(senseNoDivH);
									
				translateTd.addHtmlElement(senseNoDiv);
        	}
        	
        	Div singleSenseDiv = new Div("col-md-11");
        	translateTd.addHtmlElement(singleSenseDiv);
        	
			// znaczenie
        	List<Gloss> polishGlossList = Dictionary2HelperCommon.getPolishGlossList(sense.getGlossList());
        	SenseAdditionalInfo polishAdditionalInfo = Dictionary2HelperCommon.findFirstPolishAdditionalInfo(sense.getAdditionalInfoList());
        	
        	//    
        	
            // dynamiczna przerwa
            Supplier<String> onetimeBiggerMarginTypGenerator = new Supplier<String>() {
                    private boolean generatedSpacer = false;

                    @Override
                    public String get() {
                            if (generatedSpacer == true) {
                                    return "3px";
                            }
                            
                            generatedSpacer = true;
                            
                            if (polishAdditionalInfo == null) {
                            	return "3px";
                            }

                            return "10px";
                    }
            };
        	                	
			for (int currentGlossIdx = 0; currentGlossIdx < polishGlossList.size(); ++currentGlossIdx) {
				
				Gloss gloss = polishGlossList.get(currentGlossIdx);
										
				singleSenseDiv.addHtmlElement(new Text(getStringWithMark(
						gloss.getValue(), findWord, true) + 
						(gloss.getGType() != null ? " (" + Dictionary2HelperCommon.translateToPolishGlossType(gloss.getGType()) + ")" : "") + 
						(currentGlossIdx != sense.getGlossList().size() - 1 ? "<br/>" : "")));						
			}
			    				
			// informacje dodatkowe												
			if (polishAdditionalInfo != null) {					
				Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify; font-size: 90%");
				
    			infoDiv.addHtmlElement(new Text(getStringWithMark(polishAdditionalInfo.getValue(), findWord, true)));
    			
    			singleSenseDiv.addHtmlElement(infoDiv);						
			}
        	
        	if (addDetails == true) {
        		        	
				// ograniczone do kanji/kana					
				if (sense.getRestrictedToKanjiList().size() > 0 || sense.getRestrictedToKanaList().size() > 0) {
					List<String> restrictedToKanjiKanaList = new ArrayList<>();
					
					restrictedToKanjiKanaList.addAll(sense.getRestrictedToKanjiList());
					restrictedToKanjiKanaList.addAll(sense.getRestrictedToKanaList());
					
					Div restrictedToKanjiKanaDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
											
					// zamiana na przetlumaczona postac
					String restrictedToKanjiKanaString = messageSource.getMessage("wordDictionary.page.search.table.column.details.restrictedKanjiKana", null, Locale.getDefault()) + " " + String.join("; ", restrictedToKanjiKanaList);
											
					restrictedToKanjiKanaDiv.addHtmlElement(new Text(restrictedToKanjiKanaString + "<br/>"));
	    			
					singleSenseDiv.addHtmlElement(restrictedToKanjiKanaDiv);
				}
									
				// czesci mowy
				if (sense.getPartOfSpeechList().size() > 0) { 
					Div polishPartOfSpeechDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
					
					// zamiana na przetlumaczona postac
					String translatedToPolishPartOfSpeechEnum = String.join("; ", Dictionary2HelperCommon.translateToPolishPartOfSpeechEnum(sense.getPartOfSpeechList()));
											
					polishPartOfSpeechDiv.addHtmlElement(new Text(translatedToPolishPartOfSpeechEnum + "<br/>"));
	    			
					singleSenseDiv.addHtmlElement(polishPartOfSpeechDiv);						
				}
				
				// kategoria slowa
				if (sense.getFieldList().size() > 0) {
					Div fieldsDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
					
					// zamiana na przetlumaczona postac
					String translatedfieldEnum = String.join("; ", Dictionary2HelperCommon.translateToPolishFieldEnumList(sense.getFieldList()));
											
					fieldsDiv.addHtmlElement(new Text(translatedfieldEnum + "<br/>"));
	    			
					singleSenseDiv.addHtmlElement(fieldsDiv);						
				}
				
				// roznosci
				if (sense.getMiscList().size() > 0) {
					Div miscDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
					
					// zamiana na przetlumaczona postac
					String translatedMiscEnum = String.join("; ", Dictionary2HelperCommon.translateToPolishMiscEnumList(sense.getMiscList()));
											
					miscDiv.addHtmlElement(new Text(translatedMiscEnum + "<br/>"));
	    			
					singleSenseDiv.addHtmlElement(miscDiv);						
				}	
				
				// dialekt
				if (sense.getDialectList().size() > 0) {
					Div dialectDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
					
					// zamiana na przetlumaczona postac
					String translatedDialectEnum = String.join("; ", Dictionary2HelperCommon.translateToPolishDialectEnumList(sense.getDialectList()));
											
					dialectDiv.addHtmlElement(new Text(translatedDialectEnum + "<br/>"));
	    			
					singleSenseDiv.addHtmlElement(dialectDiv);						
				}	
				
				// zagraniczne pochodzenie slowa
				if (sense.getLanguageSourceList().size() > 0) {
					Div languageSourceDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
					
					// zamiana na przetlumaczona postac
					List<String> singleLanguageSourceList = new ArrayList<>();
					
					for (LanguageSource languageSource : sense.getLanguageSourceList()) {
													
						StringBuffer singleLanguageSource = new StringBuffer();
						
						String languageCodeInPolish = Dictionary2HelperCommon.translateToPolishLanguageCode(languageSource.getLang());
						String languageValue = languageSource.getValue();
						String languageLsWasei = Dictionary2HelperCommon.translateToPolishLanguageSourceLsWaseiEnum(languageSource.getLsWasei());
						
						if (languageValue != null && languageValue.equals("") == false) {
							singleLanguageSource.append(languageCodeInPolish + ": " + languageValue);
							
						} else {
							singleLanguageSource.append(Dictionary2HelperCommon.translateToPolishLanguageCodeWithoutValue(languageSource.getLang()));
						}
						
						if (languageLsWasei != null && languageLsWasei.equals("") == false) {
							singleLanguageSource.append(", ").append(languageLsWasei);
						}
	
						singleLanguageSourceList.add(singleLanguageSource.toString());							
					}
													
					String joinedLanguageSource = String.join("; ", singleLanguageSourceList);
											
					languageSourceDiv.addHtmlElement(new Text(joinedLanguageSource + "<br/>"));
	    			
					singleSenseDiv.addHtmlElement(languageSourceDiv);						
				}
				
				// odnosnic do innego slowa
				if (sense.getReferenceToAnotherKanjiKanaList().size() > 0) {						
					createReferenceAntonymToAnotherKanjiKanaDiv(messageSource, servletContextPath, singleSenseDiv, sense.getReferenceToAnotherKanjiKanaList(), "wordDictionary.page.search.table.column.details.referenceToAnotherKanjiKana", onetimeBiggerMarginTypGenerator);
				}
				
				// odnosnic do przeciwienstwa
				if (sense.getAntonymList().size() > 0) {						
					createReferenceAntonymToAnotherKanjiKanaDiv(messageSource, servletContextPath, singleSenseDiv, sense.getAntonymList(), "wordDictionary.page.search.table.column.details.referewnceToAntonymKanjiKana", onetimeBiggerMarginTypGenerator);
				}
        	}
						
			// przerwa
			if (senseIdx != entry.getSenseList().size() - 1) {    					
				Div marginDiv = new Div(null, "margin-bottom: 20px;");
				
				singleSenseDiv.addHtmlElement(marginDiv);						
			}
		}   
	}
	
	private static void createReferenceAntonymToAnotherKanjiKanaDiv(MessageSource messageSource, String servletContextPath, HtmlElementCommon translateTd, List<String> wordReference, String messageCode, Supplier<String> onetimeBiggerMarginTypGenerator) {
		List<String> wordsToCreateLinkList = new ArrayList<>();
		
		for (String referenceToAnotherKanjiKana : wordReference) {							
			// wartosc tutaj znajduja sie moze byc w trzech wariantach: kanji, kanji i kana oraz kanji, kana i numer pozycji w tlumaczeniu
			String[] referenceToAnotherKanjiKanaSplited = referenceToAnotherKanjiKana.split("・");
										
			if (referenceToAnotherKanjiKanaSplited.length == 1 || referenceToAnotherKanjiKanaSplited.length == 2) {
				wordsToCreateLinkList.add(referenceToAnotherKanjiKanaSplited[0]);
				
			} else if (referenceToAnotherKanjiKanaSplited.length == 3) {
				wordsToCreateLinkList.add(referenceToAnotherKanjiKanaSplited[0]);
				wordsToCreateLinkList.add(referenceToAnotherKanjiKanaSplited[1]);								
			}							
		}
		
		if (wordsToCreateLinkList.size() > 0) {
			Div referenceToAnotherKanjiKanaDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
			
			referenceToAnotherKanjiKanaDiv.addHtmlElement(new Text(messageSource.getMessage(messageCode, null, Locale.getDefault()) + " "));
			
			for (int wordsToCreateLinkListIdx = 0; wordsToCreateLinkListIdx < wordsToCreateLinkList.size(); ++wordsToCreateLinkListIdx) {
				String currentWordsToCreateLink = wordsToCreateLinkList.get(wordsToCreateLinkListIdx);
				
				// tworzymy link-i
        		WordDictionarySearchModel searchModel = new WordDictionarySearchModel();
        		
        		searchModel.setWord(currentWordsToCreateLink);
        		searchModel.setWordPlace(WordPlaceSearch.EXACT.toString());
        		
        		List<String> searchIn = new ArrayList<String>();
        		
        		searchIn.add("KANJI");
        		searchIn.add("KANA");
        		searchIn.add("ROMAJI");
        		searchIn.add("TRANSLATE");
        		searchIn.add("INFO");
        		searchIn.add("GRAMMA_FORM_AND_EXAMPLES");
        		searchIn.add("NAMES");
        				
        		searchModel.setSearchIn(searchIn);
        		
        		List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();
        		
        		for (DictionaryEntryType dictionaryEntryType : addableDictionaryEntryList) {
        			searchModel.addDictionaryType(dictionaryEntryType);
        		}
        		
        		A currentWordsToCreateLinkLink = new A();
        		
        		currentWordsToCreateLinkLink.setHref(LinkGenerator.generateWordSearchLink(servletContextPath, searchModel));
        		currentWordsToCreateLinkLink.addHtmlElement(new Text(currentWordsToCreateLink));
        		
        		referenceToAnotherKanjiKanaDiv.addHtmlElement(currentWordsToCreateLinkLink);
        		
        		if (wordsToCreateLinkListIdx != wordsToCreateLinkList.size() - 1) {
        			referenceToAnotherKanjiKanaDiv.addHtmlElement(new Text(", "));
        		}
			}
																	
    		translateTd.addHtmlElement(referenceToAnotherKanjiKanaDiv);	
		}
	}
	
    public static String getStringWithMark(String text, String findWord, boolean mark) {
    	
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

}
