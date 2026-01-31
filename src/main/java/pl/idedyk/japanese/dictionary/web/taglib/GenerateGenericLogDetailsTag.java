package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.HtmlUtils;

import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.common.Utils.ThemeType;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionaryDrawStroke;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionaryDrawStroke.Point;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.B;
import pl.idedyk.japanese.dictionary.web.html.Canvas;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.IHtmlElement;
import pl.idedyk.japanese.dictionary.web.html.Label;
import pl.idedyk.japanese.dictionary.web.html.Pre;
import pl.idedyk.japanese.dictionary.web.html.Script;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.AdminRequestLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidGetSpellCheckerSuggestionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidQueueEventLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidSendMissingWordLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GeneralExceptionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetectLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryRadicalsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.SuggestionSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchLog;

public class GenerateGenericLogDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
	private static final long serialVersionUID = 1L;
	
	private GenericLog genericLog;
	
	private MySQLConnector mySQLConnector;
			
	private MessageSource messageSource;
		
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		ServletRequest servletRequest = pageContext.getRequest();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
				
		HttpServletRequest httpServletRequest = null;
				
		if (servletRequest instanceof HttpServletRequest) {			
			httpServletRequest = (HttpServletRequest)servletRequest;
		}
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.mySQLConnector = webApplicationContext.getBean(MySQLConnector.class);
		
		try {
            JspWriter out = pageContext.getOut();

            if (genericLog == null) {
            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.null"), true));
            	
            	errorDiv.render(out);
            	
            	return SKIP_BODY;
            }
            
            // pobranie listy rekordow (jesli wystepuja)
            HttpSession session = pageContext.getSession();
            
            Div mainContentDiv = new Div();
            
            @SuppressWarnings("unchecked")
			List<GenericLog> genericLogList = (List<GenericLog>)session.getAttribute("genericLogList");
            
            if (genericLogList != null) {
            	Integer pos = null;
            	
            	for (int idx = 0; idx < genericLogList.size(); ++idx) {
					if (genericLogList.get(idx).getId().equals(genericLog.getId()) == true) {
						pos = idx;
					}
				}
            	
            	if (pos != null && genericLogList.size() > 1) {            		
            		Div buttonDiv = new Div("col-md-12", "text-align: right; margin: 0px 0px 20px 0px");
            		
            		if (pos != 0 && pos <= genericLogList.size() - 1) {
            			A previousButton = new A("btn btn-default");
            			
            			previousButton.setHref(LinkGenerator.generateShowGenericLog(pageContext.getServletContext().getContextPath(), genericLogList.get(pos - 1)));
            			previousButton.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.previousRecord")));
            			
            			buttonDiv.addHtmlElement(previousButton);
            		}
            		
            		if (pos != genericLogList.size() - 1) {
                		A nextButton = new A("btn btn-default");
                		
                		nextButton.setHref(LinkGenerator.generateShowGenericLog(pageContext.getServletContext().getContextPath(), genericLogList.get(pos + 1)));
                		nextButton.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.nextRecord")));
                		
                		buttonDiv.addHtmlElement(nextButton);
            		}
            		
            		mainContentDiv.addHtmlElement(buttonDiv);
            	}
            }            
            
            // tytul strony
            mainContentDiv.addHtmlElement(generateTitle());
                        
            Div contentDiv = new Div("col-md-12");
            mainContentDiv.addHtmlElement(contentDiv);
            
            // generowanie informacji podstawowych
            contentDiv.addHtmlElement(generateMainInfo());
            
            // generowanie informacji dodatkowych
            IHtmlElement generateAdditionalInfo = generateAdditionalInfo(httpServletRequest);
            
            if (generateAdditionalInfo != null) {
            	contentDiv.addHtmlElement(generateAdditionalInfo);
            }            
            
            // renderowanie
            mainContentDiv.render(out);
            
            return SKIP_BODY;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private H generateTitle() throws IOException {
		
		H pageHeader = new H(4);
				
		pageHeader.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.title"), true));
				
		return pageHeader;
	}
	
	private Div generateMainInfo() throws IOException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("mainInfoId");
		
		h3Title.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo"), true));
				
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		Table table = new Table();
		
		// id
		addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.id"), String.valueOf(genericLog.getId()));
		
		// timestamp
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.timestamp"), String.valueOf(simpleDateFormat.format(genericLog.getTimestamp())));

        // operation
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.operation"), genericLog.getOperation().toString());
        
        // session id
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.sessionId"), genericLog.getSessionId());
        
        // user agent
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.userAgent"), genericLog.getUserAgent());
        
        // request url
        String requestURL = genericLog.getRequestURL();
        
        if (requestURL == null) {
        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.requestUrl"), requestURL);
        	
        } else {
        	requestURL = URLDecoder.decode(requestURL, "UTF8");            	
        	        	
        	A requestUrlLink = new A();
        	
        	requestUrlLink.setHref(requestURL);
        	requestUrlLink.setEscapeHref(true);
        	requestUrlLink.addHtmlElement(new Text(requestURL, true));            	

        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.requestUrl"), requestUrlLink);
        }        

        // referer url
        String refererURL = genericLog.getRefererURL();
        
        if (refererURL == null) {
        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.refererUrl"), refererURL);
        	
        } else {
        	refererURL = URLDecoder.decode(refererURL, "UTF8");            	
        	        	
        	A refererUrlLink = new A();
        	
        	refererUrlLink.setHref(refererURL);
        	refererUrlLink.setEscapeHref(true);
        	refererUrlLink.addHtmlElement(new Text(refererURL, true));            	

        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.refererUrl"), refererUrlLink);
        }        
        
        // remote ip
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.remoteIp"), genericLog.getRemoteIp());
        
        // remote host
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.remoteHost"), genericLog.getRemoteHost());
				
		panelBody.addHtmlElement(table);
        		
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private IHtmlElement generateAdditionalInfo(HttpServletRequest httpServletRequest) throws SQLException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("additionalInfoId");
		
		h3Title.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo"), true));
				
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		GenericLogOperationEnum operation = genericLog.getOperation();
				
		if (operation == GenericLogOperationEnum.ADMIN_REQUEST) {
			
			AdminRequestLog adminRequestLog = mySQLConnector.getAdminRequestLogByGenericId(genericLog.getId());
			
			if (adminRequestLog == null) {
				return null;
			}
			
			Table table = new Table();

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.adminRequestLog.type"), String.valueOf(adminRequestLog.getType()));
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.adminRequestLog.result"), String.valueOf(adminRequestLog.getResult()));
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.adminRequestLog.params"), String.valueOf(adminRequestLog.getParams()));			
			
			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.ANDROID_SEND_MISSING_WORD) {
			
			AndroidSendMissingWordLog androidSendMissingWordLog = mySQLConnector.getAndroidSendMissingWordLogByGenericId(genericLog.getId());
			
			if (androidSendMissingWordLog == null) {
				return null;
			}
			
			Table table = new Table();
						
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidSendMissingWordLog.word"), androidSendMissingWordLog.getWord());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidSendMissingWordLog.wordPlaceSearch"), androidSendMissingWordLog.getWordPlaceSearch());
			
			panelBody.addHtmlElement(table);			
			
		} else if (operation == GenericLogOperationEnum.ANDROID_GET_SPELL_CHECKER_SUGGESTION) { 
			
			AndroidGetSpellCheckerSuggestionLog androidGetSpellCheckerSuggestionLog = mySQLConnector.getAndroidGetSpellCheckerSuggestionLogByGenericId(genericLog.getId());
			
			if (androidGetSpellCheckerSuggestionLog == null) {
				return null;
			}
			
			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidGetSpellCheckerSuggestionLog.word"), androidGetSpellCheckerSuggestionLog.getWord());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidGetSpellCheckerSuggestionLog.type"), androidGetSpellCheckerSuggestionLog.getType());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidGetSpellCheckerSuggestionLog.spellCheckerSuggestionList"), androidGetSpellCheckerSuggestionLog.getSpellCheckerSuggestionList());
			
			panelBody.addHtmlElement(table);			
		
		} else if (operation == GenericLogOperationEnum.ANDROID_QUEUE_EVENT) {
			
			AndroidQueueEventLog androidQueueEventLog = mySQLConnector.getAndroidQueueEventLogByGenericId(genericLog.getId());
			
			if (androidQueueEventLog == null) {
				return null;
			}
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidQueueEventLog.userId"), androidQueueEventLog.getUserId());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidQueueEventLog.operation"), androidQueueEventLog.getOperation().toString());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidQueueEventLog.createDate"), String.valueOf(simpleDateFormat.format(androidQueueEventLog.getCreateDate())));
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.androidQueueEventLog.params"), androidQueueEventLog.getParamsAsMap().toString());

			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.DAILY_REPORT) {
			
			DailyReportSendLog dailyReportSendLog = mySQLConnector.getDailyReportSendLogByGenericId(genericLog.getId());
			
			if (dailyReportSendLog == null) {
				return null;
			}
			
			B titleB = new B();
			titleB.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.dailyReportSendLog.title")));
			panelBody.addHtmlElement(titleB);
						
			panelBody.addHtmlElement(new Text(dailyReportSendLog.getTitle()));
			
			panelBody.addHtmlElement(new Hr());			

			panelBody.addHtmlElement(new Text(dailyReportSendLog.getReport()));
			
		} else if (operation == GenericLogOperationEnum.GENERAL_EXCEPTION) {
			
			GeneralExceptionLog generalExceptionLog = mySQLConnector.getGeneralExceptionLogByGenericId(genericLog.getId());
			
			if (generalExceptionLog == null) {
				return null;
			}
			
			Table table = new Table();

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.generalExceptionLog.statusCode"), String.valueOf(generalExceptionLog.getStatusCode()));
			
			Pre exceptionPre = new Pre();
			exceptionPre.addHtmlElement(new Text(generalExceptionLog.getException()));
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.generalExceptionLog.exception"), exceptionPre);			
			
			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_AUTOCOMPLETE) {
			
			KanjiDictionaryAutocompleteLog kanjiDictionaryAutocompleteLog = mySQLConnector.getKanjiDictionaryAutocompleteLogByGenericId(genericLog.getId());
			
			if (kanjiDictionaryAutocompleteLog == null) {
				return null;
			}
			
			Table table = new Table();

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryAutocompleteLog.term"), kanjiDictionaryAutocompleteLog.getTerm());
						
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryAutocompleteLog.foundElements"), String.valueOf(kanjiDictionaryAutocompleteLog.getFoundElements()));			
			
			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_CATALOG) {
			
			KanjiDictionaryCatalogLog kanjiDictionaryCatalogLog = mySQLConnector.getKanjiDictionaryCatalogLogByGenericId(genericLog.getId());
			
			if (kanjiDictionaryCatalogLog == null) {
				return null;
			}
			
			Table table = new Table();
						
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryCatalogLog.pageNo"), String.valueOf(kanjiDictionaryCatalogLog.getPageNo()));			
			
			panelBody.addHtmlElement(table);			
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_DETAILS) {
			
			KanjiDictionaryDetailsLog kanjiDictionaryDetailsLog = mySQLConnector.getKanjiDictionaryDetailsLogByGenericId(genericLog.getId());
			
			if (kanjiDictionaryDetailsLog == null) {
				return null;
			}
			
			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryDetailsLog.kanjiEntryId"), String.valueOf(kanjiDictionaryDetailsLog.getKanjiEntryId()));
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryDetailsLog.kanjiEntryKanji"), kanjiDictionaryDetailsLog.getKanjiEntryKanji());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryDetailsLog.kanjiEntryTranslateList"), kanjiDictionaryDetailsLog.getKanjiEntryTranslateList());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryDetailsLog.kanjiEntryInfo"), kanjiDictionaryDetailsLog.getKanjiEntryInfo());
			
			panelBody.addHtmlElement(table);			
		
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_DETECT) {
			
			KanjiDictionaryDetectLog kanjiDictionaryDetectLog = mySQLConnector.getKanjiDictionaryDetectLogByGenericId(genericLog.getId());
			
			if (kanjiDictionaryDetectLog == null) {
				return null;
			}
			
			Utils.ThemeType theme = Utils.getTheme(httpServletRequest);
			
			Div canvasDiv = new Div("col-md-6");
			
			Label strokeLabel = new Label();
			strokeLabel.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryDetectLog.stroke.title")));
			
			canvasDiv.addHtmlElement(strokeLabel);
			
			// canvas
			Canvas canvas = new Canvas(null, "border: 1px solid black;");
			
			canvas.setId("detectCanvas");
			canvas.setWidth(500);
			canvas.setHeight(500);
			
			canvasDiv.addHtmlElement(canvas);
			panelBody.addHtmlElement(canvasDiv);
			
			// parse stroke 
			KanjiDictionaryDrawStroke kanjiDictionaryDrawStroke = new KanjiDictionaryDrawStroke();
			
			String[] strokesSplited = kanjiDictionaryDetectLog.getStrokes().split("\n");

			for (int strokePathNo = 0; strokePathNo < strokesSplited.length; ++strokePathNo) {

				kanjiDictionaryDrawStroke.newStroke();

				String currentStrokePath = strokesSplited[strokePathNo];

				if (currentStrokePath.equals("") == true) {
					break;
				}

				String[] points = currentStrokePath.split(";");

				for (String currentPoint : points) {
					
					currentPoint = currentPoint.trim();
					
					if (currentPoint.equals("") == true) {
						continue;
					}

					String[] currentPointSplited = currentPoint.split(",");

					int pointIdx = currentPointSplited[0].indexOf(".");
					
					if (pointIdx != -1) {
						currentPointSplited[0] = currentPointSplited[0].substring(0, pointIdx);
					}

					pointIdx = currentPointSplited[1].indexOf(".");
					
					if (pointIdx != -1) {
						currentPointSplited[1] = currentPointSplited[1].substring(0, pointIdx);
					}
					
					try {				
						Integer currentPointX = Integer.parseInt(currentPointSplited[0]);
						Integer currentPointY = Integer.parseInt(currentPointSplited[1]);
						
						kanjiDictionaryDrawStroke.addPoint(currentPointX, currentPointY);

					} catch (NumberFormatException e) {
						throw new RuntimeException(e);
					}				
				}			
			}
			
			// script
			Script script = new Script();
			
			StringBuffer scriptSb = new StringBuffer();
			
			scriptSb.append("var canvas, stage;\n");
			scriptSb.append("var drawingCanvas;\n");
			scriptSb.append("var oldPt;\n");
			scriptSb.append("var oldMidPt;\n");
			scriptSb.append("var color;\n");
			scriptSb.append("var stroke;\n");
			scriptSb.append("\n");
			scriptSb.append("var strokePaths = [];\n");
			scriptSb.append("var currentPath = [];\n");
			
			scriptSb.append("\n");
			
			scriptSb.append("$(document).ready(function() {\n");
			scriptSb.append("\n");
			scriptSb.append("   // rysowanie\n");				
			scriptSb.append("   canvas = document.getElementById(\"detectCanvas\");\n");
			scriptSb.append("\n");
			scriptSb.append("   stage = new createjs.Stage(canvas);\n");
			scriptSb.append("   stage.autoClear = false;\n");
			scriptSb.append("   stage.enableDOMEvents(true);\n");
			scriptSb.append("\n");
			scriptSb.append("   createjs.Touch.enable(stage);\n");
			scriptSb.append("   createjs.Ticker.setFPS(24);\n");
			scriptSb.append("\n");
			scriptSb.append("   drawingCanvas = new createjs.Shape();\n");
			scriptSb.append("\n");
			scriptSb.append("   stage.addChild(drawingCanvas);\n");
			scriptSb.append("   stage.update();\n");
			
			scriptSb.append("   strokePaths = [];\n");
			scriptSb.append("\n");
			
			List<List<Point>> parsedStrokes = kanjiDictionaryDrawStroke.getStrokes();

			for (List<Point> currentParsedStroke : parsedStrokes) {
				
				scriptSb.append("  currentPath = [];\n");
				scriptSb.append("\n");
				
				for (Point point : currentParsedStroke) {
					scriptSb.append("  currentPath.push([" + point.getX() + ", " + point.getY() + "]);\n");
				}
				
				scriptSb.append("  strokePaths.push(currentPath);\n");
				scriptSb.append("\n");
				scriptSb.append("  currentPath = [];\n");
			}
			
			scriptSb.append("\n");
			scriptSb.append("   reDrawDetect();\n");
			scriptSb.append("\n");
			
			scriptSb.append("});\n");
									
			scriptSb.append("\n");
		    
			scriptSb.append("function reDrawDetect() {\n");
			scriptSb.append("\n");
			
			scriptSb.append("   stage.clear();\n");
			scriptSb.append("\n");
			
			if (theme == ThemeType.LIGHT) {
				scriptSb.append("   color = \"#000000\";\n");	
			} else if (theme == ThemeType.DARK) {
				scriptSb.append("   color = \"white\";\n");
			} else {
				throw new RuntimeException(); // to nigdy nie powinno zdarzyc sie
			}			
			
			scriptSb.append("   stroke = 10;\n");
			scriptSb.append("\n");
			scriptSb.append("   for (var idx = 0; idx < strokePaths.length; ++idx) {\n");
			scriptSb.append("      var currentStrokePath = strokePaths[idx];\n");
			scriptSb.append("\n");
			scriptSb.append("      oldPt = new createjs.Point(currentStrokePath[0][0], currentStrokePath[0][1]);\n");
			scriptSb.append("      oldMidPt = oldPt;\n");
			scriptSb.append("\n");
			scriptSb.append("      for (var currentStrokePathIdx = 0; currentStrokePathIdx < currentStrokePath.length; ++currentStrokePathIdx) {\n");
			scriptSb.append("\n");
			scriptSb.append("         var midPt = new createjs.Point(oldPt.x + currentStrokePath[currentStrokePathIdx][0]>>1, oldPt.y + currentStrokePath[currentStrokePathIdx][1]>>1);\n");
			scriptSb.append("\n");
			scriptSb.append("         drawingCanvas.graphics.clear().setStrokeStyle(stroke, 'round', 'round').beginStroke(color).moveTo(midPt.x, midPt.y).curveTo(oldPt.x, oldPt.y, oldMidPt.x, oldMidPt.y);\n");
			scriptSb.append("\n");
			scriptSb.append("         oldPt.x = currentStrokePath[currentStrokePathIdx][0];\n");
			scriptSb.append("         oldPt.y = currentStrokePath[currentStrokePathIdx][1];\n");
			scriptSb.append("\n");
			scriptSb.append("         oldMidPt.x = midPt.x;\n");
			scriptSb.append("         oldMidPt.y = midPt.y;\n");
			scriptSb.append("\n");
			scriptSb.append("         stage.update();\n");
			scriptSb.append("      }\n");
			scriptSb.append("   }\n");
			scriptSb.append("}\n");
			
			script.addHtmlElement(new Text(scriptSb.toString()));
			
			panelBody.addHtmlElement(script);
			
			Div detectResultDiv = new Div("col-md-6");
			
			Label detectResultLabel = new Label();
			detectResultLabel.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryDetectLog.detectResult.title")));
			
			detectResultDiv.addHtmlElement(detectResultLabel);
			
			Pre detectResultPre = new Pre();			
			detectResultPre.addHtmlElement(new Text(kanjiDictionaryDetectLog.getDetectKanjiResult()));
			
			detectResultDiv.addHtmlElement(detectResultPre);
			
			panelBody.addHtmlElement(detectResultDiv);
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_RADICALS) {
			
			KanjiDictionaryRadicalsLog kanjiDictionaryRadicalsLog = mySQLConnector.getKanjiDictionaryRadicalsLogByGenericId(genericLog.getId());
			
			if (kanjiDictionaryRadicalsLog == null) {
				return null;
			}
			
			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryRadicalsLog.radicals"), kanjiDictionaryRadicalsLog.getRadicals());
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionaryRadicalsLog.foundElements"), String.valueOf(kanjiDictionaryRadicalsLog.getFoundElements()));			

			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH) {
			
			KanjiDictionarySearchLog kanjiDictionarySearchLog = mySQLConnector.getKanjiDictionarySearchLogByGenericId(genericLog.getId());
			
			if (kanjiDictionarySearchLog == null) {
				return null;
			}

			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionarySearchLog.findKanjiRequestWord"), kanjiDictionarySearchLog.getFindKanjiRequestWord());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionarySearchLog.findKanjiRequestWordPlace"), kanjiDictionarySearchLog.getFindKanjiRequestWordPlace());
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionarySearchLog.findKanjiRequestStrokeCountFrom"), kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountFrom() != null ? 
					String.valueOf(kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountFrom()) : null);
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionarySearchLog.findKanjiRequestStrokeCountTo"), kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountTo() != null ? 
					String.valueOf(kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountTo()) : null);
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.kanjiDictionarySearchLog.findKanjiResultResultSize"), String.valueOf(kanjiDictionarySearchLog.getFindKanjiResultResultSize()));

			panelBody.addHtmlElement(table);
						
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_AUTOCOMPLETE) {
			
			WordDictionaryAutocompleteLog wordDictionaryAutocompleteLog = mySQLConnector.getWordDictionaryAutocompleteLogByGenericId(genericLog.getId());
			
			if (wordDictionaryAutocompleteLog == null) {
				return null;
			}
			
			Table table = new Table();

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryAutocompleteLog.term"), wordDictionaryAutocompleteLog.getTerm());
						
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryAutocompleteLog.foundElements"), String.valueOf(wordDictionaryAutocompleteLog.getFoundElements()));			
			
			panelBody.addHtmlElement(table);

		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_CATALOG) {
			
			WordDictionaryCatalogLog wordDictionaryCatalogLog = mySQLConnector.getWordDictionaryCatalogLogByGenericId(genericLog.getId());
			
			if (wordDictionaryCatalogLog == null) {
				return null;
			}
			
			Table table = new Table();
						
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryCatalogLog.pageNo"), String.valueOf(wordDictionaryCatalogLog.getPageNo()));			
			
			panelBody.addHtmlElement(table);			
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_DETAILS) {
			
			WordDictionaryDetailsLog wordDictionaryDetailsLog = mySQLConnector.getWordDictionaryDetailsLogByGenericId(genericLog.getId());
			
			if (wordDictionaryDetailsLog == null) {
				return null;
			}
			
			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryDetailsLog.dictionaryEntryId"), String.valueOf(wordDictionaryDetailsLog.getDictionaryEntryId()));
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryDetailsLog.dictionaryEntryKanji"), wordDictionaryDetailsLog.getDictionaryEntryKanji());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryDetailsLog.dictionaryEntryKanaList"), wordDictionaryDetailsLog.getDictionaryEntryKanaList());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryDetailsLog.dictionaryEntryRomajiList"), wordDictionaryDetailsLog.getDictionaryEntryRomajiList());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryDetailsLog.dictionaryEntryTranslateList"), wordDictionaryDetailsLog.getDictionaryEntryTranslateList());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryDetailsLog.dictionaryEntryInfo"), wordDictionaryDetailsLog.getDictionaryEntryInfo());
			
			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_SEARCH) {
			
			WordDictionarySearchLog wordDictionarySearchLog = mySQLConnector.getWordDictionarySearchLogByGenericId(genericLog.getId());
			
			if (wordDictionarySearchLog == null) {
				return null;
			}
			
			Table table = new Table();

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestWord"), wordDictionarySearchLog.getFindWordRequestWord());

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestKanji"), wordDictionarySearchLog.getFindWordRequestKanji() != null ? 
					String.valueOf(wordDictionarySearchLog.getFindWordRequestKanji()) : null);
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestKana"), wordDictionarySearchLog.getFindWordRequestKana() != null ? 
					String.valueOf(wordDictionarySearchLog.getFindWordRequestKana()) : null);

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestRomaji"), wordDictionarySearchLog.getFindWordRequestRomaji() != null ? 
					String.valueOf(wordDictionarySearchLog.getFindWordRequestRomaji()) : null);

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestTranslate"), wordDictionarySearchLog.getFindWordRequestTranslate() != null ? 
					String.valueOf(wordDictionarySearchLog.getFindWordRequestTranslate()) : null);

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestInfo"), wordDictionarySearchLog.getFindWordRequestInfo() != null ? 
					String.valueOf(wordDictionarySearchLog.getFindWordRequestInfo()) : null);

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestOnlyCommonWords"), wordDictionarySearchLog.getFindWordRequestOnlyCommonWords() != null ? 
					String.valueOf(wordDictionarySearchLog.getFindWordRequestOnlyCommonWords()) : null);

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestWordPlace"), wordDictionarySearchLog.getFindWordRequestWordPlace());

			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordRequestDictionaryEntryTypeList"), wordDictionarySearchLog.getFindWordRequestDictionaryEntryTypeList());			
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.findWordResultResultSize"), String.valueOf(wordDictionarySearchLog.getFindWordResultResultSize()));
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionarySearchLog.priority"), String.valueOf(wordDictionarySearchLog.getPriority()));
			
			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_NAME_CATALOG) {
			
			WordDictionaryNameCatalogLog wordDictionaryNameCatalogLog = mySQLConnector.getWordDictionaryNameCatalogLogByGenericId(genericLog.getId());
			
			if (wordDictionaryNameCatalogLog == null) {
				return null;
			}
			
			Table table = new Table();
						
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryNameCatalogLog.pageNo"), String.valueOf(wordDictionaryNameCatalogLog.getPageNo()));			
			
			panelBody.addHtmlElement(table);			
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_NAME_DETAILS) {
			
			WordDictionaryNameDetailsLog wordDictionaryNameDetailsLog = mySQLConnector.getWordDictionaryNameDetailsLogByGenericId(genericLog.getId());
			
			if (wordDictionaryNameDetailsLog == null) {
				return null;
			}
			
			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryNameDetailsLog.dictionaryEntryId"), String.valueOf(wordDictionaryNameDetailsLog.getDictionaryEntryId()));
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryNameDetailsLog.dictionaryEntryKanji"), wordDictionaryNameDetailsLog.getDictionaryEntryKanji());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryNameDetailsLog.dictionaryEntryKanaList"), wordDictionaryNameDetailsLog.getDictionaryEntryKanaList());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryNameDetailsLog.dictionaryEntryRomajiList"), wordDictionaryNameDetailsLog.getDictionaryEntryRomajiList());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryNameDetailsLog.dictionaryEntryTranslateList"), wordDictionaryNameDetailsLog.getDictionaryEntryTranslateList());
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.wordDictionaryNameDetailsLog.dictionaryEntryInfo"), wordDictionaryNameDetailsLog.getDictionaryEntryInfo());
			
			panelBody.addHtmlElement(table);
			
		} else if (operation == GenericLogOperationEnum.SUGGESTION_SEND) {
			
			SuggestionSendLog suggestionSendLog = mySQLConnector.getSuggestionSendLogByGenericId(genericLog.getId());
			
			if (suggestionSendLog == null) {
				return null;
			}
			
			Table table = new Table();
			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.suggestionSendLog.title"), HtmlUtils.htmlEscape(suggestionSendLog.getTitle()));
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.suggestionSendLog.sender"), HtmlUtils.htmlEscape(suggestionSendLog.getSender()));			
			addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo.suggestionSendLog.body"), HtmlUtils.htmlEscape(suggestionSendLog.getBody()));			
			
			panelBody.addHtmlElement(table);
			
		} else {
			return null;
		}		
				
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private void addRowToTable(Table table, String label, String value) {

		if (value == null) {
			value = "";
		}		
		
		addRowToTable(table, label, new Text(value, true));
	}
	
	private void addRowToTable(Table table, String label, IHtmlElement valueHtmlElement) {
		
		Tr tr = new Tr();
		
		Td td1 = new Td(null, "padding: 0px 10px 0px 0px");
		
		Label td1Label = new Label();
		
		td1Label.addHtmlElement(new Text(label, true));
		
		td1.addHtmlElement(td1Label);
		tr.addHtmlElement(td1);
		
		Td td2 = new Td();		
		
		Div td2Div = new Div(null, "margin-bottom: 5px");
		td2Div.addHtmlElement(valueHtmlElement);
		
		td2.addHtmlElement(td2Div);
		
		tr.addHtmlElement(td2);
		
		table.addHtmlElement(tr);
	}
	
	private String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}

	public GenericLog getGenericLog() {
		return genericLog;
	}

	public void setGenericLog(GenericLog genericLog) {
		this.genericLog = genericLog;
	}
}
