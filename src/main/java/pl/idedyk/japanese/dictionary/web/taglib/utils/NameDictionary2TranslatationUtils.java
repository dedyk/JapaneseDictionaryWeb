package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.HtmlElementCommon;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2NameHelperCommon;
import pl.idedyk.japanese.dictionary2.jmnedict.xsd.JMnedict;
import pl.idedyk.japanese.dictionary2.jmnedict.xsd.TranslationalInfo;
import pl.idedyk.japanese.dictionary2.jmnedict.xsd.TranslationalInfoTransDet;
import pl.idedyk.japanese.dictionary2.jmnedict.xsd.TranslationalInfoTransDetAdditionalInfo;

public class NameDictionary2TranslatationUtils extends WordNameDictionary2CommonUtils {
	
	public static void createTranslationHtmlElements(MessageSource messageSource, String servletContextPath, JMnedict.Entry entry, HtmlElementCommon translateTd, String findWord, boolean addTranslationNumber, boolean addDetails) {
		
		// FM_FIXME: sprawdzic, czy to dziala
		
		// !!! INFO: jezeli cos tutaj zmieniasz to byc moze trzeba rowniez zmienic w WordDictionary2SenseUtils !!!
		
        for (int translationIdx = 0; translationIdx < entry.getTranslationInfo().size(); ++translationIdx) {
        	
        	TranslationalInfo translationalInfo = entry.getTranslationInfo().get(translationIdx);
        	
        	if (addTranslationNumber == true) {
				// numer znaczenia
				Div senseNoDiv = new Div("col-md-1");
				
				H senseNoDivH = new H(4, null, "margin-top: 3px; text-align: right");
				
				senseNoDivH.addHtmlElement(new Text("" + (translationIdx + 1)));				
				senseNoDiv.addHtmlElement(senseNoDivH);
									
				translateTd.addHtmlElement(senseNoDiv);
        	}
        	
        	Div singleSenseDiv = new Div("col-md-11");
        	translateTd.addHtmlElement(singleSenseDiv);
        	
			// znaczenie
        	List<TranslationalInfoTransDet> translationalInfoTransDetList = Dictionary2NameHelperCommon.getEnglishOrPolishTranslationalInfoTransDet(translationalInfo.getTransDet());        	
        	TranslationalInfoTransDetAdditionalInfo additionalInfo = Dictionary2NameHelperCommon.getFirstEnglishOrPolishTranslationalInfoTransDetAdditionalInfo(translationalInfo.getAddInfo());
        	
        	//
        	
        	boolean wasAdditionalInfoAllTypes = false;
        	            	
            // lista tlumaczen
			for (int currentTranslationalInfoTransDetIdx = 0; currentTranslationalInfoTransDetIdx < translationalInfoTransDetList.size(); ++currentTranslationalInfoTransDetIdx) {
				
				TranslationalInfoTransDet currentTanslationalInfoTransDet = translationalInfoTransDetList.get(currentTranslationalInfoTransDetIdx);
				    				
				singleSenseDiv.addHtmlElement(new Text(getStringWithMark(
						currentTanslationalInfoTransDet.getValue(), findWord, true) +  
						(currentTranslationalInfoTransDetIdx != translationalInfoTransDetList.size() - 1 ? "<br/>" : "")));
			}
            			
			// informacje dodatkowe												
			if (additionalInfo != null) {
				wasAdditionalInfoAllTypes = true;
				
				Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify; font-size: 90%");
				
    			infoDiv.addHtmlElement(new Text(getStringWithMark(additionalInfo.getValue(), findWord, true)));
    			
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
        											
				// rodzaj nazwy				
				if (translationalInfo.getNameType().size() > 0) { 
					Div polishTranslationalInfoNameTypeDiv = new Div(null, "margin-left: 40px; font-size: 75%; margin-top: " + onetimeBiggerMarginTypGenerator.get() + "; text-align: justify");
										
					// zamiana na przetlumaczona postac
					String translatedToPolishTranslationalInfoNameType = String.join("; ", Dictionary2NameHelperCommon.translateToPolishTranslationalInfoNameTypeList(translationalInfo.getNameType()));
											
					polishTranslationalInfoNameTypeDiv.addHtmlElement(new Text(translatedToPolishTranslationalInfoNameType + "<br/>"));
	    			
					singleSenseDiv.addHtmlElement(polishTranslationalInfoNameTypeDiv);						
				}
        	}
						
			// przerwa
			if (translationIdx != entry.getTranslationInfo().size() - 1) {    					
				Div marginDiv = new Div(null, "margin-bottom: 20px;");
				
				singleSenseDiv.addHtmlElement(marginDiv);						
			}
		}   
	}
}
