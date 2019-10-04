package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.api.dto.KanjivgEntry;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Button.ButtonType;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Script;
import pl.idedyk.japanese.dictionary.web.html.Text;

public class GenerateDrawStrokeDialog {
	
	public static Button generateDrawStrokeButton(String dialogId, String buttonText) {
		
		Button button = new Button("btn btn-default");
		
		button.setButtonType(ButtonType.BUTTON);
		button.setOnClick("show" + dialogId + "Drawing()");
		
		Text buttonTextText = new Text(buttonText);
		
		button.addHtmlElement(buttonTextText);
		
		return button;		
	}
	
	public static Script generateDrawStrokeButtonScript(String dialogId, boolean mobile) {
		
		Script script = new Script();
		
		StringBuffer scriptBody = new StringBuffer();
		
		scriptBody.append("   function show" + dialogId + "Drawing() {\n");
		scriptBody.append("      $( '#" + dialogId + "Drawing').lazylinepainter('erase');\n");
		
		if (mobile == true) {
			scriptBody.append("      $( '#" + dialogId + "').attr(\"width\", screen.width - 30);\n");
			scriptBody.append("      $($( '#" + dialogId + "').children()[0]).css(\"width\", screen.width - 30);\n");
		}
		
		scriptBody.append("      $( '#" + dialogId + "' ).modal();\n");		
		scriptBody.append("      setTimeout(function() { $('#" + dialogId + "Drawing').lazylinepainter('paint'); }, 700);\n");
		scriptBody.append("   }\n");
		
		Text scriptText = new Text(scriptBody.toString());
		
		script.addHtmlElement(scriptText);

		return script;		
	}

	public static Div generateDrawStrokeDialog(DictionaryManager dictionaryManager, MessageSource messageSource,
			String word, String dialogId) throws IOException, DictionaryException {
		
		return generateDrawStrokeDialog(dictionaryManager, messageSource, word, dialogId, new GenerateDrawStrokeDialogParams());		
	}
	
	public static Div generateDrawStrokeDialog(DictionaryManager dictionaryManager, MessageSource messageSource,
			String word, String dialogId, GenerateDrawStrokeDialogParams params) throws IOException, DictionaryException {
		
		String dialogTitle = messageSource.getMessage("common.generateDrawStrokeDiv.dialog.title", new String[] { word }, null);
		
		String drawAgainButtonTitle = messageSource.getMessage("common.generateDrawStrokeDiv.dialog.drawAgain", null, Locale.getDefault());
		String closeButtonTitle = messageSource.getMessage("common.generateDrawStrokeDiv.dialog.close", null, Locale.getDefault());
		
		List<KanjivgEntry> strokePathsForWord = dictionaryManager.getStrokePathsForWord(word);
		
		// glowny div
		Div drawStrokeDialogDiv = new Div("modal fade");
		
		drawStrokeDialogDiv.setId(dialogId);
		drawStrokeDialogDiv.setWidth(params.width);
		drawStrokeDialogDiv.setHeight(params.height);
		
		Div modalDialog = new Div("modal-dialog");

		modalDialog.setStyle("width: " + params.width + "px");
		drawStrokeDialogDiv.addHtmlElement(modalDialog);
		
		Div modalContent = new Div("modal-content");
		modalDialog.addHtmlElement(modalContent);
		
		Div moldalHeader = new Div("modal-header");
		modalContent.addHtmlElement(moldalHeader);
		
		// przycisk zamkniecia
		Button closeButton = new Button("close");
		
		closeButton.setButtonType(ButtonType.BUTTON);
		closeButton.setDataDismiss("modal");
		closeButton.setAriaHidden("true");
		
		closeButton.addHtmlElement(new Text("&times;"));
		
		moldalHeader.addHtmlElement(closeButton);
		
		// tytul okienka
		H dialogTitleH4 = new H(4, "modal-title");
		dialogTitleH4.addHtmlElement(new Text(dialogTitle));
		
		moldalHeader.addHtmlElement(dialogTitleH4);
		
		// zawartosc glowna okienka
		Div modalBody = new Div("modal-body");
		modalContent.addHtmlElement(modalBody);
		
		Div drawingDiv = new Div();
		
		drawingDiv.setId(dialogId + "Drawing");
		modalBody.addHtmlElement(drawingDiv);
				
		// naglowek okienka
		Div modalFooter = new Div("modal-footer");
		modalContent.addHtmlElement(modalFooter);
		
		// przycisk zamkniecia 2
		Button closeButton2 = new Button("btn btn-default");
		
		closeButton2.setButtonType(ButtonType.BUTTON);
		closeButton2.setDataDismiss("modal");
		
		closeButton2.addHtmlElement(new Text(closeButtonTitle));
		
		modalFooter.addHtmlElement(closeButton2);
		
		// przycisk rysowania ponownego
		Button drawAgainButton = new Button("btn btn-primary");
		
		drawAgainButton.setButtonType(ButtonType.BUTTON);
		drawAgainButton.setOnClick("draw" + dialogId + "Again()");
		
		drawAgainButton.addHtmlElement(new Text(drawAgainButtonTitle));
		
		modalFooter.addHtmlElement(drawAgainButton);
						
		// skrypt
		StringBuffer scriptDrawingSb = new StringBuffer();
		
		scriptDrawingSb.append("	var pathObj_" + dialogId + " = {\n");
		scriptDrawingSb.append("	    \"" + dialogId + "Drawing\": {\n");
		scriptDrawingSb.append("	        \"strokepath\": [\n");
				
		for (int currentStrokePathsIdx = 0; currentStrokePathsIdx < strokePathsForWord.size(); ++currentStrokePathsIdx) {
			
			KanjivgEntry kanjivgEntry = strokePathsForWord.get(currentStrokePathsIdx);
			
			List<String> strokePaths = kanjivgEntry.getStrokePaths();
			
			for (int strokePathIdx = 0; strokePathIdx < strokePaths.size(); ++strokePathIdx) {
				
				String currentStrokePath = strokePaths.get(strokePathIdx);

				scriptDrawingSb.append("				{\n");
				scriptDrawingSb.append("					\"path\": \"" + currentStrokePath + "\",\n");
				scriptDrawingSb.append("					\"duration\": " + params.duration + ",\n");
				scriptDrawingSb.append("					\"translateX\": " + (currentStrokePathsIdx * 100) + ",\n");
				scriptDrawingSb.append("					\"addPathNum\": " + params.addPathNum + "\n");
		        
		        if (strokePathIdx != strokePaths.size() - 1 || currentStrokePathsIdx < strokePathsForWord.size()) {
		        	scriptDrawingSb.append("				},\n");
		       	
		        } else {
		        	scriptDrawingSb.append("				}\n");
		        }								
			}
		}

		scriptDrawingSb.append("	        ],\n");
		scriptDrawingSb.append("	        \"dimensions\": {\n");
		scriptDrawingSb.append("	            \"width\": " + (params.width - 80) + ",\n");
		scriptDrawingSb.append("	            \"height\": " + (params.height - 80) + "\n");
		scriptDrawingSb.append("	        }\n");
		scriptDrawingSb.append("	    }\n");
		scriptDrawingSb.append("	};\n");

		scriptDrawingSb.append("	$(document).ready(function() {\n");
		
		scriptDrawingSb.append("		$('#" + dialogId + "Drawing').lazylinepainter({\n");
		scriptDrawingSb.append("			\"svgData\": pathObj_" + dialogId + ",\n");
		scriptDrawingSb.append("			\"strokeWidth\": 5,\n");
		scriptDrawingSb.append("			\"strokeColor\": \"#262213\",\n");
		scriptDrawingSb.append("	        \"viewBoxX\": 0,\n");
		scriptDrawingSb.append("	        \"viewBoxY\": 0,\n");
		scriptDrawingSb.append("	        \"viewBoxWidth\": " + (params.zoomFactory * (params.width - 80)) + ",\n");
		scriptDrawingSb.append("	        \"viewBoxHeight\": " + (params.zoomFactory * (params.height - 80)) + ",\n");
		scriptDrawingSb.append("	        \"viewBoxFit\": false\n");
		scriptDrawingSb.append("		});\n");
				
		scriptDrawingSb.append("	});\n");

		scriptDrawingSb.append("   function draw" + dialogId + "Again() {\n");
		scriptDrawingSb.append("      $( '#" + dialogId + "Drawing').lazylinepainter('erase');\n");
		scriptDrawingSb.append("      setTimeout(function() { $( '#" + dialogId + "Drawing').lazylinepainter('erase'); $('#" + dialogId + "Drawing').lazylinepainter('paint'); }, 700);\n");
		scriptDrawingSb.append("   }\n");
		
		Script scriptDrawing = new Script();
		
		scriptDrawing.addHtmlElement(new Text(scriptDrawingSb.toString()));
		drawStrokeDialogDiv.addHtmlElement(scriptDrawing);
		
		return drawStrokeDialogDiv;
	}
	
	public static class GenerateDrawStrokeDialogParams {
		
		public int width = 900;
		
		public int height = 200;
		
		public float zoomFactory = 1.5f;
		
		public int duration = 400;
		
		public boolean addPathNum = false;
	}
}
