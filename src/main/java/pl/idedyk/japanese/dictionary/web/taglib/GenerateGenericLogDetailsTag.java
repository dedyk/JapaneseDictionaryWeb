package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionaryDrawStroke;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionaryDrawStroke.Point;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Canvas;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.IHtmlElement;
import pl.idedyk.japanese.dictionary.web.html.Label;
import pl.idedyk.japanese.dictionary.web.html.Pre;
import pl.idedyk.japanese.dictionary.web.html.Script;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetectLog;

public class GenerateGenericLogDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
	private static final long serialVersionUID = 1L;
	
	private GenericLog genericLog;
	
	private MySQLConnector mySQLConnector;
			
	private MessageSource messageSource;
		
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.mySQLConnector = webApplicationContext.getBean(MySQLConnector.class);
		
		try {
            JspWriter out = pageContext.getOut();

            if (genericLog == null) {
            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.null")));
            	
            	errorDiv.render(out);
            	
            	return SKIP_BODY;
            }
            
            Div mainContentDiv = new Div();
            
            // tytul strony
            mainContentDiv.addHtmlElement(generateTitle());
                        
            Div contentDiv = new Div("col-md-12");
            mainContentDiv.addHtmlElement(contentDiv);
            
            // generowanie informacji podstawowych
            contentDiv.addHtmlElement(generateMainInfo());
            
            // generowanie informacji dodatkowych
            IHtmlElement generateAdditionalInfo = generateAdditionalInfo();
            
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
				
		pageHeader.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.title")));
				
		return pageHeader;
	}
	
	private Div generateMainInfo() throws IOException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("mainInfoId");
		
		h3Title.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo")));
				
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
        	requestUrlLink.addHtmlElement(new Text(requestURL));            	

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
        	refererUrlLink.addHtmlElement(new Text(refererURL));            	

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
	
	private IHtmlElement generateAdditionalInfo() throws SQLException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("additionalInfoId");
		
		h3Title.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.additionalInfo")));
				
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		GenericLogOperationEnum operation = genericLog.getOperation();
		
		if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_DETECT) {
			
			KanjiDictionaryDetectLog kanjiDictionaryDetectLog = mySQLConnector.getKanjiDictionaryDetectLogByGenericId(genericLog.getId());
			
			if (kanjiDictionaryDetectLog == null) {
				return null;
			}
			
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
			scriptSb.append("   color = \"#000000\";\n");
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
		
		addRowToTable(table, label, new Text(value));
	}
	
	private void addRowToTable(Table table, String label, IHtmlElement valueHtmlElement) {
		
		Tr tr = new Tr();
		
		Td td1 = new Td(null, "padding: 0px 10px 0px 0px");
		
		Label td1Label = new Label();
		
		td1Label.addHtmlElement(new Text(label));
		
		td1.addHtmlElement(td1Label);
		tr.addHtmlElement(td1);
		
		Td td2 = new Td();		
		td2.addHtmlElement(valueHtmlElement);
		
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
