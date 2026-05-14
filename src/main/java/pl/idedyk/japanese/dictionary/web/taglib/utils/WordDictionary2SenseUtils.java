package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.HtmlElementCommon;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Gloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Info;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.LanguageSource;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Sense;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.SenseAdditionalInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Xref;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict.Entry;

public class WordDictionary2SenseUtils extends WordNameDictionary2CommonUtils {
	
	public static void createSenseHtmlElements(DictionaryManager dictionaryManager, MessageSource messageSource, String servletContextPath, 
			Entry entry, HtmlElementCommon translateTd, String findWord, boolean addSenseNumber, boolean addDetails, boolean mobile) throws DictionaryException {
		
		// !!! INFO: jezeli cos tutaj zmieniasz to byc moze trzeba rowniez zmienic w NameDictionary2TranslatationUtils !!!
		
        for (int senseIdx = 0; senseIdx < entry.getSenseList().size(); ++senseIdx) {
        	
        	Sense sense = entry.getSenseList().get(senseIdx);
        	
        	if (addSenseNumber == true) {
        		// INFO: podobny kod jest przy info, jezeli cos zmieniasz tutaj, zmien i tam
        		
				// numer znaczenia
				Div senseNoDiv = new Div("col-md-1");
				
				H senseNoDivH = null;
				
				if (mobile == false) {
					senseNoDivH = new H(4, null, "margin-top: 3px; text-align: right");
				} else {
					senseNoDivH = new H(4, null, "margin-top: 3px; text-align: left");
				}
				
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
        	
        	boolean wasAdditionalInfoAllTypes = false;        	        	
            boolean existsGtypeNull = polishGlossList.stream().filter(f -> f.getGType() == null).count() > 0;
            
            if (existsGtypeNull == true) { // jezeli istnieje gType rowne null, wiec pokazujemy tlumaczenia i wszelkie wyjasnienia osobno
            	
                // lista tlumaczen - gtype = null
    			for (int currentGlossIdx = 0; currentGlossIdx < polishGlossList.size(); ++currentGlossIdx) {
    				
    				Gloss gloss = polishGlossList.get(currentGlossIdx);
    				
    				if (gloss.getGType() == null) {
    					singleSenseDiv.addHtmlElement(new Text(getStringWithMark(
    							gloss.getValue(), findWord, true) + 
    							(gloss.getGType() != null ? " (" + Dictionary2HelperCommon.translateToPolishGlossType(gloss.getGType()) + ")" : "") + 
    							(currentGlossIdx != sense.getGlossList().size() - 1 ? "<br/>" : "")));
    				}				
    			}            	
            	
                // lista tlumaczen - gtype != null jako informacje dodatkowe
    			for (int currentGlossIdx = 0; currentGlossIdx < polishGlossList.size(); ++currentGlossIdx) {
    				
    				Gloss gloss = polishGlossList.get(currentGlossIdx);
    				
    				if (gloss.getGType() != null) {
    					wasAdditionalInfoAllTypes = true;
    					
    					Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify; font-size: 90%");
    					
    	    			infoDiv.addHtmlElement(new Text(getStringWithMark(gloss.getValue(), findWord, true) + 
    	    					" (" + Dictionary2HelperCommon.translateToPolishGlossType(gloss.getGType()) + ")"));
    	    			
    	    			singleSenseDiv.addHtmlElement(infoDiv);						
    				}				
    			}
            	
            } else { // jezeli nie ma gType = null, wiec pokazujemy po staremu
            	
                // lista tlumaczen
    			for (int currentGlossIdx = 0; currentGlossIdx < polishGlossList.size(); ++currentGlossIdx) {
    				
    				Gloss gloss = polishGlossList.get(currentGlossIdx);
    				    				
					singleSenseDiv.addHtmlElement(new Text(getStringWithMark(
							gloss.getValue(), findWord, true) + 
							(gloss.getGType() != null ? " (" + Dictionary2HelperCommon.translateToPolishGlossType(gloss.getGType()) + ")" : "") + 
							(currentGlossIdx != sense.getGlossList().size() - 1 ? "<br/>" : "")));
    			}
            }
            			
			// informacje dodatkowe												
			if (polishAdditionalInfo != null) {
				wasAdditionalInfoAllTypes = true;
				
				Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify; font-size: 90%");
				
    			infoDiv.addHtmlElement(new Text(getStringWithMark(polishAdditionalInfo.getValue(), findWord, true)));
    			
    			singleSenseDiv.addHtmlElement(infoDiv);						
			}
        	
        	if (addDetails == true) {
        		final boolean wasAdditionalInfoAllTypesAsFinal = wasAdditionalInfoAllTypes;
        		
                // dynamiczna przerwa
                Supplier<String> onetimeBiggerMarginTypGenerator = new Supplier<String>() {
                        private boolean generatedSpacer = false;

                        @Override
                        public String get() {
                                if (generatedSpacer == true) {
                                        return "3px";
                                }
                                
                                generatedSpacer = true;
                                
                                if (wasAdditionalInfoAllTypesAsFinal == false) {
                                	return "3px";
                                }

                                return "10px";
                        }
                };
        		        	
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
				if (entry.getLanguageSourceList().size() > 0) {
					Div languageSourceDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
					
					// zamiana na przetlumaczona postac
					List<String> singleLanguageSourceList = new ArrayList<>();
					
					for (LanguageSource languageSource : entry.getLanguageSourceList()) {
													
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
				
				/*
				// odnosnic do innego slowa
				if (sense.getReferenceToAnotherKanjiKanaList().size() > 0) {						
					createReferenceAntonymToAnotherKanjiKanaDiv(messageSource, servletContextPath, singleSenseDiv, sense.getReferenceToAnotherKanjiKanaList(), "wordDictionary.page.search.table.column.details.referenceToAnotherKanjiKana", onetimeBiggerMarginTypGenerator);
				}
				
				// odnosnic do przeciwienstwa
				if (sense.getAntonymList().size() > 0) {						
					createReferenceAntonymToAnotherKanjiKanaDiv(messageSource, servletContextPath, singleSenseDiv, sense.getAntonymList(), "wordDictionary.page.search.table.column.details.referewnceToAntonymKanjiKana", onetimeBiggerMarginTypGenerator);
				}
				*/
				
				// odnosniki do innych slow (przejmuje obsluge dwoch elementow powyzej zakomentowanych)
				List<Xref> referenceToAnotherKanjiKanaList = sense.getReferenceToAnotherKanjiKanaList();
				
				if (referenceToAnotherKanjiKanaList.size() > 0) {
					createReferenceToAnotherKanjiKanaDiv(dictionaryManager, messageSource, servletContextPath, singleSenseDiv, referenceToAnotherKanjiKanaList, onetimeBiggerMarginTypGenerator);
				}				
        	}
						
			// przerwa
			if (senseIdx != entry.getSenseList().size() - 1) {    					
				Div marginDiv = new Div(null, "margin-bottom: 20px;");
				
				singleSenseDiv.addHtmlElement(marginDiv);						
			}
		}
        
        // dodanie info
        List<Info> polishInfoList = Dictionary2HelperCommon.getPolishInfoList(entry.getInfoList());
        
        if (polishInfoList.size() > 0) {
        	boolean wasBiggerTopMargin = false;
        	
        	for (Info info : polishInfoList) {
        		
            	if (addSenseNumber == true) {
            		// INFO: podobny kod jest przy sense, jezeli cos zmieniasz tutaj, zmien i tam
            		
    				// numer znaczenia
    				Div senseNoDiv = new Div("col-md-1");
    				
    				H senseNoDivH = null;
    				
    				if (mobile == false) {
    					senseNoDivH = new H(4, null, "margin-top: 3px; text-align: right");
    				} else {
    					senseNoDivH = new H(4, null, "margin-top: 3px; text-align: left");
    				}
    								
    				senseNoDiv.addHtmlElement(senseNoDivH);				
    				translateTd.addHtmlElement(senseNoDiv);
            	}
        		
            	Div singleInfoDiv = new Div("col-md-11", "margin-left: 0px; font-size: 100%; margin-top: " + (wasBiggerTopMargin == false ? "25px" : "5px") + "; text-align: justify");      	
            	            	
            	singleInfoDiv.addHtmlElement(new Text(getStringWithMark(info.getValue(), findWord, true)));
            	translateTd.addHtmlElement(singleInfoDiv);

            	wasBiggerTopMargin = true;
			}
        }
	}
	
	private static void createReferenceToAnotherKanjiKanaDiv(DictionaryManager dictionaryManager, MessageSource messageSource, String servletContextPath, HtmlElementCommon translateTd, 
			List<Xref> referenceToAnotherKanjiKanaList, Supplier<String> onetimeBiggerMarginTypGenerator) throws DictionaryException {
		/*
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
		*/
		
		if (referenceToAnotherKanjiKanaList.size() > 0) {
						
			for (int referenceToAnotherKanjiKanaListIdx = 0; referenceToAnotherKanjiKanaListIdx < referenceToAnotherKanjiKanaList.size(); ++referenceToAnotherKanjiKanaListIdx) {
				Div referenceToAnotherKanjiKanaDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
				
				Xref currentXRef = referenceToAnotherKanjiKanaList.get(referenceToAnotherKanjiKanaListIdx);
								
				referenceToAnotherKanjiKanaDiv.addHtmlElement(new Text(Dictionary2HelperCommon.translateXrefType(currentXRef.getType()) + " "));
								
				if (currentXRef.getDict() != null) {
					referenceToAnotherKanjiKanaDiv.addHtmlElement(new Text(currentXRef.getDict() + ": "));
				}
				
				StringBuffer xrefLinkValue = new StringBuffer();
				
				if (currentXRef.getXKanji() != null) {
					xrefLinkValue.append(currentXRef.getXKanji());
				}
				
				if (currentXRef.getXKana() != null) {
					if (xrefLinkValue.length() > 0) {
						xrefLinkValue.append(" / ");
					}
					xrefLinkValue.append(currentXRef.getXKana());
				}
				
				// pobieramy slowo
				Entry referencedDictionaryEntry2 = null;
				
				if (currentXRef.getSeq() != null) {
					referencedDictionaryEntry2 = dictionaryManager.getDictionaryEntry2ById(currentXRef.getSeq());
				}
				
				// tworzymy link
				if (referencedDictionaryEntry2 != null) {
	        		A referencedDictionaryEntry2Link = new A();
	        		
	        		referencedDictionaryEntry2Link.setHref(LinkGenerator.generateDictionaryEntryDetailsLink(servletContextPath, referencedDictionaryEntry2));
	        		referencedDictionaryEntry2Link.addHtmlElement(new Text(xrefLinkValue.toString()));
	        		
	        		referenceToAnotherKanjiKanaDiv.addHtmlElement(referencedDictionaryEntry2Link);
	        		
				} else {
					referenceToAnotherKanjiKanaDiv.addHtmlElement(new Text(xrefLinkValue.toString()));
				}
				
	    		translateTd.addHtmlElement(referenceToAnotherKanjiKanaDiv);	
			}
		}
	}
}
