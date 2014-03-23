package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.FuriganaEntry;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog;

public class GenerateWordDictionaryDetailsTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private DictionaryEntry dictionaryEntry;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		MessageSource messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		DictionaryManager dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
		
		try {
            JspWriter out = pageContext.getOut();

            if (dictionaryEntry == null) {
            	           	
            	// nie znaleziono strony
            	out.println("<div class=\"alert alert-danger\">");
            	out.println(getMessage(messageSource, "wordDictionaryDetails.page.dictionaryEntry.null"));            	
            	out.println("</div>");
            	
            	return SKIP_BODY;
            }
            
            // tytul strony
            generateTitle(out, messageSource);
            
            // kanji
            generateKanjiSection(out, dictionaryManager, messageSource);
            
            // czytanie
            generateReadingSection(out, dictionaryManager, messageSource);
            
            return SKIP_BODY;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	private void generateTitle(JspWriter out, MessageSource messageSource) throws IOException {
		
        out.println("<h4 class=\"page-header\">");
        out.println(getMessage(messageSource, "wordDictionaryDetails.page.dictionaryEntry.title"));
        out.println("</h4>");		
	}
	
	private void generateKanjiSection(JspWriter out, DictionaryManager dictionaryManager, MessageSource messageSource) throws IOException {
		
		final String kanjiDrawId = "kanjiDrawId";
		        
		String prefixKana = dictionaryEntry.getPrefixKana();

		if (prefixKana != null && prefixKana.length() == 0) {
			prefixKana = null;
		}
		
		final StringBuffer kanjiSb = new StringBuffer();
        
		boolean addKanjiWrite = false;
		
        if (dictionaryEntry.isKanjiExists() == true) {
        	
			if (prefixKana != null) {
				kanjiSb.append("(").append(prefixKana).append(") ");
			}

			kanjiSb.append(dictionaryEntry.getKanji());

			addKanjiWrite = true;
        	
        } else {
			kanjiSb.append("-");

			addKanjiWrite = false;
		}
        
        if (addKanjiWrite == true) {
        	
            out.println("<div class=\"panel panel-default\">");
            
            out.println("   <div class=\"panel-heading\">");
            out.println("      <h3 class=\"panel-title\">" + getMessage(messageSource, "wordDictionaryDetails.page.dictionaryEntry.kanji.title") + "</h3>");
            out.println("   </div>");
            
            out.println("   <div class=\"panel-body\">");
       	
            List<FuriganaEntry> furiganaEntries = dictionaryManager.getFurigana(dictionaryEntry);
        	            
            if (furiganaEntries != null && furiganaEntries.size() > 0 && addKanjiWrite == true) {
            	
            	for (FuriganaEntry currentFuriganaEntry : furiganaEntries) {
            		
					List<String> furiganaKanaParts = currentFuriganaEntry.getKanaPart();
					List<String> furiganaKanjiParts = currentFuriganaEntry.getKanjiPart();
					
					out.println("<table>");
					out.println("<tr style=\"font-size: 123%; text-align:center;\">");
					
					for (int idx = 0; idx < furiganaKanaParts.size(); ++idx) {
						
						String currentKanaPart = furiganaKanaParts.get(idx);
						
						out.println("<td>");
						out.println(currentKanaPart);
						out.println("</td>");
					}
					
					out.println("</tr>");
					
					out.println("<tr style=\"font-size: 300%;\">");
					
					for (int idx = 0; idx < furiganaKanjiParts.size(); ++idx) {
						
						String currentKanjiPart = furiganaKanjiParts.get(idx);
						
						out.println("<td>");
						out.println(currentKanjiPart);
						out.println("</td>");
					}		
					
					out.println("<td><div style=\"margin: 0 0 0 50px\">");
					
					// dodaj guzik pisania znakow kanji
					GenerateDrawStrokeDialog.addDrawStrokeButton(out, kanjiDrawId, getMessage(messageSource, "wordDictionaryDetails.page.dictionaryEntry.kanji.showKanjiDraw"));
					
					out.println("</div></td>");
					
					out.println("</tr>");
					out.println("</table>");
            	}
            	
            } else {
            	
            	out.println("<table>");   	
            	out.println("<tr style=\"font-size: 300%;\">");
            	
				out.println("<td>");
				out.println(kanjiSb.toString());
				out.println("</td>");            	
            	
            	out.println("</tr>");
            	out.println("</table>");
            }
        	            
            out.println("   </div>");
            
            out.println("</div>");        	
        }
                
        // wygeneruj okienko rysowania znaku kanji
        GenerateDrawStrokeDialog.generateDrawStrokeDialog(out, dictionaryManager, messageSource, kanjiSb.toString(), kanjiDrawId);
	}
	
	private void generateReadingSection(JspWriter out, DictionaryManager dictionaryManager, MessageSource messageSource) throws IOException {
		
		String prefixKana = dictionaryEntry.getPrefixKana();
		String prefixRomaji = dictionaryEntry.getPrefixRomaji();
		
		List<String> kanaList = dictionaryEntry.getKanaList();
		List<String> romajiList = dictionaryEntry.getRomajiList();
		
        out.println("<div class=\"panel panel-default\">");
        
        out.println("   <div class=\"panel-heading\">");
        out.println("      <h3 class=\"panel-title\">" + getMessage(messageSource, "wordDictionaryDetails.page.dictionaryEntry.reading.title") + "</h3>");
        out.println("   </div>");
        
        out.println("   <div class=\"panel-body\">");

        out.println("      <table>");
        
        class IdAndText {
        	
        	public String id;
        	
        	public String text;

			public IdAndText(String id, String text) {
				this.id = id;
				this.text = text;
			}        	
        }
        
        List<IdAndText> idAndTextList = new ArrayList<IdAndText>();
                
        for (int idx = 0; idx < kanaList.size(); ++idx) {
        	
        	final String kanaDrawId = "kanaDrawId" + idx;
        	
            out.println("         <tr>");

			final StringBuffer sb = new StringBuffer();

			if (prefixKana != null && prefixKana.equals("") == false) {
				sb.append("(").append(prefixKana).append(") ");
			}

			sb.append(kanaList.get(idx)).append(" - ");

			if (prefixRomaji != null && prefixRomaji.equals("") == false) {
				sb.append("(").append(prefixRomaji).append(") ");
			}

			sb.append(romajiList.get(idx));
			
			out.println("            <td><h3>");
			out.println(sb.toString());
			out.println("            </h3></td>");
			
			out.println("            <td><div style=\"margin: 0 0 0 50px\">");
			
			// dodaj guzik pisania znakow kana
			GenerateDrawStrokeDialog.addDrawStrokeButton(out, kanaDrawId, getMessage(messageSource, "wordDictionaryDetails.page.dictionaryEntry.reading.showKanaDraw"));
			
			idAndTextList.add(new IdAndText(kanaDrawId, sb.toString()));
			
			out.println("            </div></td>");
			
            out.println("         </tr>");
        }
        
        out.println("      </table>");
		
        out.println("   </div>");
        
        out.println("</div>");
        
        for (IdAndText idAndText : idAndTextList) {
        	
            // wygeneruj okienko rysowania znakow kana
            GenerateDrawStrokeDialog.generateDrawStrokeDialog(out, dictionaryManager, messageSource, idAndText.text, idAndText.id);
		}        
	}
	
	/*

		private List<IScreenItem> generateDetails(final DictionaryEntry dictionaryEntry,
				DictionaryEntryType forceDictionaryEntryType, final ScrollView scrollMainLayout) {

			List<IScreenItem> report = new ArrayList<IScreenItem>();

			String prefixKana = dictionaryEntry.getPrefixKana();
			String prefixRomaji = dictionaryEntry.getPrefixRomaji();

			// Reading
			report.add(new TitleItem(getString(R.string.word_dictionary_details_reading_label), 0));
			report.add(new StringValue(getString(R.string.word_dictionary_word_anim), 12.0f, 0));

			List<String> romajiList = dictionaryEntry.getRomajiList();

			for (int idx = 0; idx < kanaList.size(); ++idx) {

				final StringBuffer sb = new StringBuffer();

				if (prefixKana != null) {
					sb.append("(").append(prefixKana).append(") ");
				}

				sb.append(kanaList.get(idx)).append(" - ");

				if (prefixRomaji != null) {
					sb.append("(").append(prefixRomaji).append(") ");
				}

				sb.append(romajiList.get(idx));

				StringValue readingStringValue = new StringValue(sb.toString(), 20.0f, 0);

				readingStringValue.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						List<KanjivgEntry> strokePathsForWord = JapaneseAndroidLearnHelperApplication.getInstance()
								.getDictionaryManager(WordDictionaryDetails.this).getStrokePathsForWord(sb.toString());

						StrokePathInfo strokePathInfo = new StrokePathInfo();

						strokePathInfo.setStrokePaths(strokePathsForWord);

						Intent intent = new Intent(getApplicationContext(), SodActivity.class);

						intent.putExtra("strokePathsInfo", strokePathInfo);
						intent.putExtra("annotateStrokes", false);

						startActivity(intent);
					}
				});

				report.add(readingStringValue);

				TableLayout actionButtons = new TableLayout(TableLayout.LayoutParam.WrapContent_WrapContent, true, null);
				TableRow actionTableRow = new TableRow();

				// speak image		
				Image speakImage = new Image(getResources().getDrawable(android.R.drawable.ic_lock_silent_mode_off), 0);
				speakImage.setOnClickListener(new TTSJapaneseSpeak(null, kanaList.get(idx)));
				actionTableRow.addScreenItem(speakImage);

				// clipboard kana
				Image clipboardKana = new Image(getResources().getDrawable(R.drawable.clipboard_kana), 0);
				clipboardKana.setOnClickListener(new CopyToClipboard(kanaList.get(idx)));
				actionTableRow.addScreenItem(clipboardKana);

				// clipboard romaji
				Image clipboardRomaji = new Image(getResources().getDrawable(R.drawable.clipboard_romaji), 0);
				clipboardRomaji.setOnClickListener(new CopyToClipboard(romajiList.get(idx)));
				actionTableRow.addScreenItem(clipboardRomaji);

				actionButtons.addTableRow(actionTableRow);

				report.add(actionButtons);
			}

			// Translate
			report.add(new TitleItem(getString(R.string.word_dictionary_details_translate_label), 0));

			List<String> translates = dictionaryEntry.getTranslates();

			for (int idx = 0; idx < translates.size(); ++idx) {
				report.add(new StringValue(translates.get(idx), 20.0f, 0));
			}

			// Additional info
			report.add(new TitleItem(getString(R.string.word_dictionary_details_additional_info_label), 0));

			if (isSmTsukiNiKawatteOshiokiYo(kanjiSb.toString()) == true) {
				report.add(createSmTsukiNiKawatteOshiokiYo());
			} else {
				String info = dictionaryEntry.getInfo();

				if (info != null && info.length() > 0) {
					report.add(new StringValue(info, 20.0f, 0));
				} else {
					report.add(new StringValue("-", 20.0f, 0));
				}
			}

			// Word type
			int addableDictionaryEntryTypeInfoCounter = 0;

			List<DictionaryEntryType> dictionaryEntryTypeList = dictionaryEntry.getDictionaryEntryTypeList();

			if (dictionaryEntryTypeList != null) {
				for (DictionaryEntryType currentDictionaryEntryType : dictionaryEntryTypeList) {

					boolean addableDictionaryEntryTypeInfo = DictionaryEntryType
							.isAddableDictionaryEntryTypeInfo(currentDictionaryEntryType);

					if (addableDictionaryEntryTypeInfo == true) {
						addableDictionaryEntryTypeInfoCounter++;
					}
				}
			}

			if (addableDictionaryEntryTypeInfoCounter > 0) {
				report.add(new TitleItem(getString(R.string.word_dictionary_details_part_of_speech), 0));

				if (addableDictionaryEntryTypeInfoCounter > 1) {
					report.add(new StringValue(getString(R.string.word_dictionary_details_part_of_speech_press), 12.0f, 0));
				}

				for (final DictionaryEntryType currentDictionaryEntryType : dictionaryEntryTypeList) {

					StringValue currentDictionaryEntryTypeStringValue = new StringValue(
							currentDictionaryEntryType.getName(), 20.0f, 0);

					if (addableDictionaryEntryTypeInfoCounter > 1) {
						currentDictionaryEntryTypeStringValue.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent(getApplicationContext(), WordDictionaryDetails.class);

								intent.putExtra("item", dictionaryEntry);
								intent.putExtra("forceDictionaryEntryType", currentDictionaryEntryType);

								startActivity(intent);
							}
						});
					}

					report.add(currentDictionaryEntryTypeStringValue);
				}
			}

			List<Attribute> attributeList = dictionaryEntry.getAttributeList().getAttributeList();

			if (attributeList != null && attributeList.size() > 0) {
				report.add(new TitleItem(getString(R.string.word_dictionary_details_attributes), 0));

				for (Attribute currentAttribute : attributeList) {

					AttributeType attributeType = currentAttribute.getAttributeType();

					if (attributeType.isShow() == true) {
						report.add(new StringValue(attributeType.getName(), 15.0f, 0));
					}

					if (attributeType == AttributeType.VERB_TRANSITIVITY_PAIR
							|| attributeType == AttributeType.VERB_INTRANSITIVITY_PAIR) {

						Integer transitivityIntransitivityPairWordId = Integer.parseInt(currentAttribute
								.getAttributeValue().get(0));

						final DictionaryEntry transitivityIntransitivityPairDictionaryEntry = JapaneseAndroidLearnHelperApplication
								.getInstance().getDictionaryManager(WordDictionaryDetails.this)
								.getDictionaryEntryById(transitivityIntransitivityPairWordId);

						if (transitivityIntransitivityPairDictionaryEntry != null) {

							StringValue attributeTypeStringValue = new StringValue(attributeType.getName(), 15.0f, 0);

							OnClickListener goToTransitivityIntransitivityPairDictionaryEntryDetails = new OnClickListener() {

								@Override
								public void onClick(View v) {

									Intent intent = new Intent(getApplicationContext(), WordDictionaryDetails.class);

									intent.putExtra("item", transitivityIntransitivityPairDictionaryEntry);

									startActivity(intent);
								}
							};

							attributeTypeStringValue
									.setOnClickListener(goToTransitivityIntransitivityPairDictionaryEntryDetails);

							report.add(attributeTypeStringValue);

							List<String> transitivityIntransitivityPairDictionaryEntryKanaList = transitivityIntransitivityPairDictionaryEntry
									.getKanaList();
							List<String> transitivityIntransitivityPairDictionaryEntryRomajiList = transitivityIntransitivityPairDictionaryEntry
									.getRomajiList();

							for (int transitivityIntransitivityPairDictionaryEntryKanaListIdx = 0; transitivityIntransitivityPairDictionaryEntryKanaListIdx < transitivityIntransitivityPairDictionaryEntryKanaList
									.size(); transitivityIntransitivityPairDictionaryEntryKanaListIdx++) {

								StringBuffer transitivityIntrasitivitySb = new StringBuffer();

								if (transitivityIntransitivityPairDictionaryEntry.isKanjiExists() == true) {
									transitivityIntrasitivitySb.append(
											transitivityIntransitivityPairDictionaryEntry.getKanji()).append(", ");
								}

								transitivityIntrasitivitySb.append(
										transitivityIntransitivityPairDictionaryEntryKanaList
												.get(transitivityIntransitivityPairDictionaryEntryKanaListIdx))
										.append(", ");
								transitivityIntrasitivitySb.append(transitivityIntransitivityPairDictionaryEntryRomajiList
										.get(transitivityIntransitivityPairDictionaryEntryKanaListIdx));

								StringValue transitivityIntransitivityPairDictionaryEntryKanjiKanaRomajiListStringValue = new StringValue(
										transitivityIntrasitivitySb.toString(), 15.0f, 1);

								transitivityIntransitivityPairDictionaryEntryKanjiKanaRomajiListStringValue
										.setOnClickListener(goToTransitivityIntransitivityPairDictionaryEntryDetails);

								report.add(transitivityIntransitivityPairDictionaryEntryKanjiKanaRomajiListStringValue);
							}
						}
					}
				}
			}

			// dictionary groups
			List<GroupEnum> groups = dictionaryEntry.getGroups();

			report.add(new StringValue("", 15.0f, 2));
			report.add(new TitleItem(getString(R.string.word_dictionary_details_dictionary_groups), 0));

			for (int groupsIdx = 0; groupsIdx < groups.size(); ++groupsIdx) {
				report.add(new StringValue(String.valueOf(groups.get(groupsIdx).getValue()), 20.0f, 0));
			}

			/ *
			// dictionary position
			report.add(new TitleItem(getString(R.string.word_dictionary_details_dictionary_position), 0));

			report.add(new StringValue(String.valueOf(dictionaryEntry.getId()), 20.0f, 0));
			* /

			// known kanji
			List<KanjiEntry> knownKanji = null;

			if (dictionaryEntry.isKanjiExists() == true) {
				knownKanji = JapaneseAndroidLearnHelperApplication.getInstance().getDictionaryManager(this)
						.findKnownKanji(dictionaryEntry.getKanji());
			}

			if (knownKanji != null && knownKanji.size() > 0) {

				report.add(new StringValue("", 15.0f, 2));
				report.add(new TitleItem(getString(R.string.word_dictionary_known_kanji), 0));
				report.add(new StringValue(getString(R.string.word_dictionary_known_kanji_info), 12.0f, 0));

				for (int knownKanjiIdx = 0; knownKanjiIdx < knownKanji.size(); ++knownKanjiIdx) {

					final KanjiEntry kanjiEntry = knownKanji.get(knownKanjiIdx);

					OnClickListener kanjiOnClickListener = new OnClickListener() {

						@Override
						public void onClick(View v) {

							// show kanji details

							Intent intent = new Intent(getApplicationContext(), KanjiDetails.class);

							intent.putExtra("item", kanjiEntry);

							startActivity(intent);
						}
					};

					StringValue knownKanjiStringValue = new StringValue(kanjiEntry.getKanji(), 16.0f, 1);
					StringValue polishTranslateStringValue = new StringValue(kanjiEntry.getPolishTranslates().toString(),
							16.0f, 1);

					knownKanjiStringValue.setOnClickListener(kanjiOnClickListener);
					polishTranslateStringValue.setOnClickListener(kanjiOnClickListener);

					report.add(knownKanjiStringValue);
					report.add(polishTranslateStringValue);

					if (knownKanjiIdx != knownKanji.size() - 1) {
						report.add(new StringValue("", 10.0f, 1));
					}

				}
			}

			// index
			int indexStartPos = report.size();

			Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaCache = new HashMap<GrammaFormConjugateResultType, GrammaFormConjugateResult>();

			// Conjugater
			List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList = GrammaConjugaterManager
					.getGrammaConjufateResult(JapaneseAndroidLearnHelperApplication.getInstance()
							.getDictionaryManager(this).getKeigoHelper(), dictionaryEntry, grammaCache,
							forceDictionaryEntryType);

			if (grammaFormConjugateGroupTypeElementsList != null) {
				report.add(new StringValue("", 15.0f, 2));
				report.add(new TitleItem(getString(R.string.word_dictionary_details_conjugater_label), 0));

				for (GrammaFormConjugateGroupTypeElements currentGrammaFormConjugateGroupTypeElements : grammaFormConjugateGroupTypeElementsList) {

					report.add(new TitleItem(currentGrammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType()
							.getName(), 1));

					List<GrammaFormConjugateResult> grammaFormConjugateResults = currentGrammaFormConjugateGroupTypeElements
							.getGrammaFormConjugateResults();

					for (GrammaFormConjugateResult currentGrammaFormConjugateResult : grammaFormConjugateResults) {

						if (currentGrammaFormConjugateResult.getResultType().isShow() == true) {
							report.add(new TitleItem(currentGrammaFormConjugateResult.getResultType().getName(), 2));
						}

						addGrammaFormConjugateResult(report, currentGrammaFormConjugateResult);
					}

					report.add(new StringValue("", 15.0f, 1));
				}
			}

			// Exampler
			List<ExampleGroupTypeElements> exampleGroupTypeElementsList = ExampleManager.getExamples(
					JapaneseAndroidLearnHelperApplication.getInstance().getDictionaryManager(this).getKeigoHelper(),
					dictionaryEntry, grammaCache, forceDictionaryEntryType);

			if (exampleGroupTypeElementsList != null) {

				if (grammaFormConjugateGroupTypeElementsList == null) {
					report.add(new StringValue("", 15.0f, 2));
				}

				report.add(new TitleItem(getString(R.string.word_dictionary_details_exampler_label), 0));

				for (ExampleGroupTypeElements currentExampleGroupTypeElements : exampleGroupTypeElementsList) {

					report.add(new TitleItem(currentExampleGroupTypeElements.getExampleGroupType().getName(), 1));

					String exampleGroupInfo = currentExampleGroupTypeElements.getExampleGroupType().getInfo();

					if (exampleGroupInfo != null) {
						report.add(new StringValue(exampleGroupInfo, 12.0f, 1));
					}

					List<ExampleResult> exampleResults = currentExampleGroupTypeElements.getExampleResults();

					for (ExampleResult currentExampleResult : exampleResults) {
						addExampleResult(report, currentExampleResult);
					}

					report.add(new StringValue("", 15.0f, 1));
				}
			}

			// add index
			if (indexStartPos < report.size()) {

				int indexStopPos = report.size();

				List<IScreenItem> indexList = new ArrayList<IScreenItem>();

				indexList.add(new StringValue("", 15.0f, 2));
				indexList.add(new TitleItem(getString(R.string.word_dictionary_details_report_counters_index), 0));
				indexList.add(new StringValue(getString(R.string.word_dictionary_details_index_go), 12.0f, 1));

				for (int reportIdx = indexStartPos; reportIdx < indexStopPos; ++reportIdx) {

					IScreenItem currentReportScreenItem = report.get(reportIdx);

					if (currentReportScreenItem instanceof TitleItem == false) {
						continue;
					}

					final TitleItem currentReportScreenItemAsTitle = (TitleItem) currentReportScreenItem;

					final StringValue titleStringValue = new StringValue(currentReportScreenItemAsTitle.getTitle(), 15.0f,
							currentReportScreenItemAsTitle.getLevel() + 2);

					titleStringValue.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							backScreenPositionStack.push(scrollMainLayout.getScrollY());

							int counterPos = currentReportScreenItemAsTitle.getY();
							scrollMainLayout.scrollTo(0, counterPos - 3);
						}
					});

					indexList.add(titleStringValue);
				}

				for (int indexListIdx = 0, reportStartPos = indexStartPos; indexListIdx < indexList.size(); ++indexListIdx) {
					report.add(reportStartPos, indexList.get(indexListIdx));

					reportStartPos++;
				}
			}

			return report;
		}

		private void fillDetailsMainLayout(List<IScreenItem> generatedDetails, LinearLayout detailsMainLayout) {

			for (IScreenItem currentDetailsReportItem : generatedDetails) {
				currentDetailsReportItem.generate(this, getResources(), detailsMainLayout);
			}
		}

		private void addGrammaFormConjugateResult(List<IScreenItem> report,
				GrammaFormConjugateResult grammaFormConjugateResult) {

			TableLayout actionButtons = new TableLayout(TableLayout.LayoutParam.WrapContent_WrapContent, true, null);
			TableRow actionTableRow = new TableRow();

			String grammaFormKanji = grammaFormConjugateResult.getKanji();

			String prefixKana = grammaFormConjugateResult.getPrefixKana();
			String prefixRomaji = grammaFormConjugateResult.getPrefixRomaji();

			StringBuffer grammaFormKanjiSb = new StringBuffer();

			if (grammaFormKanji != null) {
				if (prefixKana != null && prefixKana.equals("") == false) {
					grammaFormKanjiSb.append("(").append(prefixKana).append(") ");
				}

				grammaFormKanjiSb.append(grammaFormKanji);

				report.add(new StringValue(grammaFormKanjiSb.toString(), 15.0f, 2));
			}

			List<String> grammaFormKanaList = grammaFormConjugateResult.getKanaList();
			List<String> grammaFormRomajiList = grammaFormConjugateResult.getRomajiList();

			for (int idx = 0; idx < grammaFormKanaList.size(); ++idx) {

				StringBuffer sb = new StringBuffer();

				if (prefixKana != null && prefixKana.equals("") == false) {
					sb.append("(").append(prefixKana).append(") ");
				}

				sb.append(grammaFormKanaList.get(idx));

				report.add(new StringValue(sb.toString(), 15.0f, 2));

				StringBuffer grammaFormRomajiSb = new StringBuffer();

				if (prefixRomaji != null && prefixRomaji.equals("") == false) {
					grammaFormRomajiSb.append("(").append(prefixRomaji).append(") ");
				}

				grammaFormRomajiSb.append(grammaFormRomajiList.get(idx));

				report.add(new StringValue(grammaFormRomajiSb.toString(), 15.0f, 2));

				// speak image
				Image speakImage = new Image(getResources().getDrawable(android.R.drawable.ic_lock_silent_mode_off), 2);
				speakImage.setOnClickListener(new TTSJapaneseSpeak(null, grammaFormKanaList.get(idx)));
				actionTableRow.addScreenItem(speakImage);

				// clipboard kanji
				if (grammaFormKanji != null) {
					Image clipboardKanji = new Image(getResources().getDrawable(R.drawable.clipboard_kanji), 0);
					clipboardKanji.setOnClickListener(new CopyToClipboard(grammaFormKanji));
					actionTableRow.addScreenItem(clipboardKanji);
				}

				// clipboard kana
				Image clipboardKana = new Image(getResources().getDrawable(R.drawable.clipboard_kana), 0);
				clipboardKana.setOnClickListener(new CopyToClipboard(grammaFormKanaList.get(idx)));
				actionTableRow.addScreenItem(clipboardKana);

				// clipboard romaji
				Image clipboardRomaji = new Image(getResources().getDrawable(R.drawable.clipboard_romaji), 0);
				clipboardRomaji.setOnClickListener(new CopyToClipboard(grammaFormRomajiList.get(idx)));
				actionTableRow.addScreenItem(clipboardRomaji);

				actionButtons.addTableRow(actionTableRow);

				report.add(actionButtons);
			}

			GrammaFormConjugateResult alternative = grammaFormConjugateResult.getAlternative();

			if (alternative != null) {
				report.add(new StringValue("", 5.0f, 1));

				addGrammaFormConjugateResult(report, alternative);
			}
		}

		private void addExampleResult(List<IScreenItem> report, ExampleResult exampleResult) {

			TableLayout actionButtons = new TableLayout(TableLayout.LayoutParam.WrapContent_WrapContent, true, null);
			TableRow actionTableRow = new TableRow();

			String exampleKanji = exampleResult.getKanji();
			String prefixKana = exampleResult.getPrefixKana();
			String prefixRomaji = exampleResult.getPrefixRomaji();

			StringBuffer exampleKanjiSb = new StringBuffer();

			if (exampleKanji != null) {
				if (prefixKana != null && prefixKana.equals("") == false) {
					exampleKanjiSb.append("(").append(prefixKana).append(") ");
				}

				exampleKanjiSb.append(exampleKanji);

				report.add(new StringValue(exampleKanjiSb.toString(), 15.0f, 2));
			}

			List<String> exampleKanaList = exampleResult.getKanaList();
			List<String> exampleRomajiList = exampleResult.getRomajiList();

			for (int idx = 0; idx < exampleKanaList.size(); ++idx) {

				StringBuffer sb = new StringBuffer();

				if (prefixKana != null && prefixKana.equals("") == false) {
					sb.append("(").append(prefixKana).append(") ");
				}

				sb.append(exampleKanaList.get(idx));

				report.add(new StringValue(sb.toString(), 15.0f, 2));

				StringBuffer exampleRomajiSb = new StringBuffer();

				if (prefixRomaji != null && prefixRomaji.equals("") == false) {
					exampleRomajiSb.append("(").append(prefixRomaji).append(") ");
				}

				exampleRomajiSb.append(exampleRomajiList.get(idx));

				report.add(new StringValue(exampleRomajiSb.toString(), 15.0f, 2));

				String exampleResultInfo = exampleResult.getInfo();

				if (exampleResultInfo != null) {
					report.add(new StringValue(exampleResultInfo, 12.0f, 2));
				}

				// speak image
				Image speakImage = new Image(getResources().getDrawable(android.R.drawable.ic_lock_silent_mode_off), 2);
				speakImage.setOnClickListener(new TTSJapaneseSpeak(null, exampleKanaList.get(idx)));
				actionTableRow.addScreenItem(speakImage);

				// clipboard kanji
				if (exampleKanji != null) {
					Image clipboardKanji = new Image(getResources().getDrawable(R.drawable.clipboard_kanji), 0);
					clipboardKanji.setOnClickListener(new CopyToClipboard(exampleKanji));
					actionTableRow.addScreenItem(clipboardKanji);
				}

				// clipboard kana
				Image clipboardKana = new Image(getResources().getDrawable(R.drawable.clipboard_kana), 0);
				clipboardKana.setOnClickListener(new CopyToClipboard(exampleKanaList.get(idx)));
				actionTableRow.addScreenItem(clipboardKana);

				// clipboard romaji
				Image clipboardRomaji = new Image(getResources().getDrawable(R.drawable.clipboard_romaji), 0);
				clipboardRomaji.setOnClickListener(new CopyToClipboard(exampleRomajiList.get(idx)));
				actionTableRow.addScreenItem(clipboardRomaji);

				actionButtons.addTableRow(actionTableRow);

				report.add(actionButtons);
			}

			ExampleResult alternative = exampleResult.getAlternative();

			if (alternative != null) {
				report.add(new StringValue("", 5.0f, 1));

				addExampleResult(report, alternative);
			}
		}

		// special
		private boolean isSmTsukiNiKawatteOshiokiYo(String value) {

			if (value == null) {
				return false;
			}

			if (value.equals("月に代わって、お仕置きよ!") == true) {
				return true;
			}

			return false;
		}

		private StringValue createSmTsukiNiKawatteOshiokiYo() {

			StringValue smStringValue = new StringValue(getString(R.string.sm_tsuki_ni_kawatte_oshioki_yo), 2.8f, 0);

			smStringValue.setTypeface(Typeface.MONOSPACE);
			smStringValue.setTextColor(Color.BLACK);
			smStringValue.setBackgroundColor(Color.WHITE);
			smStringValue.setGravity(Gravity.CENTER);

			return smStringValue;
		}

		private class TTSJapaneseSpeak implements OnClickListener {

			private final String prefix;

			private final String kanjiKana;

			public TTSJapaneseSpeak(String prefix, String kanjiKana) {
				this.prefix = prefix;
				this.kanjiKana = kanjiKana;
			}

			@Override
			public void onClick(View v) {

				StringBuffer text = new StringBuffer();

				if (prefix != null) {
					text.append(prefix);
				}

				if (kanjiKana != null) {
					text.append(kanjiKana);
				}

				if (ttsConnector != null && ttsConnector.getOnInitResult() != null
						&& ttsConnector.getOnInitResult().booleanValue() == true) {
					ttsConnector.speak(text.toString());
				} else {
					AlertDialog alertDialog = new AlertDialog.Builder(WordDictionaryDetails.this).create();

					alertDialog.setMessage(getString(R.string.tts_japanese_error));
					alertDialog.setCancelable(false);

					alertDialog.setButton(getString(R.string.tts_error_ok), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// noop
						}
					});

					alertDialog.setButton2(getString(R.string.tts_google_play_go), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							Uri marketUri = Uri.parse(getString(R.string.tts_svox_market_url));

							Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);

							startActivity(marketIntent);
						}
					});

					alertDialog.show();
				}
			}
		}

		private class CopyToClipboard implements OnClickListener {

			private final String text;

			public CopyToClipboard(String text) {
				this.text = text;
			}

			@Override
			public void onClick(View v) {

				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

				clipboardManager.setText(text);

				Toast.makeText(WordDictionaryDetails.this,
						getString(R.string.word_dictionary_details_clipboard_copy, text), Toast.LENGTH_SHORT).show();
			}
		}
	}

	*/
	
	private String getMessage(MessageSource messageSource, String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}

	/*
	private String getMessage(MessageSource messageSource, String code, String[] args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
	*/
	
	public DictionaryEntry getDictionaryEntry() {
		return dictionaryEntry;
	}

	public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}
}
