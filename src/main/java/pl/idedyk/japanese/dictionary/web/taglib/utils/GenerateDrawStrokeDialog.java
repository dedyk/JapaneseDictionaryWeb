package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.api.dto.KanjivgEntry;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

public class GenerateDrawStrokeDialog {
	
	public static void addDrawStrokeButton(JspWriter out, String dialogId, String buttonText) throws IOException {
		
		out.println("<button type=\"button\" class=\"btn btn-default\" onclick=\"show" + dialogId + "Drawing()\">" + buttonText + "</button>\n");
		
		out.println("<script>");
		
		out.println("   function show" + dialogId + "Drawing() {");
		out.println("      $( '#" + dialogId + "Drawing').lazylinepainter('erase');");
		out.println("      $( '#" + dialogId + "' ).modal();");		
		out.println("      setTimeout(function() { $('#" + dialogId + "Drawing').lazylinepainter('paint'); }, 700);");

		out.println("      ");
		
		out.println("   }");
		out.println("</script>");
	}
	
	public static void generateDrawStrokeDialog(JspWriter out, DictionaryManager dictionaryManager, MessageSource messageSource,
			String word, String dialogId) throws IOException {
		
		String dialogTitle = messageSource.getMessage("common.generateDrawStrokeDiv.dialog.title", new String[] { word }, null);
		
		String drawAgainButtonTitle = messageSource.getMessage("common.generateDrawStrokeDiv.dialog.drawAgain", null, Locale.getDefault());
		String closeButtonTitle = messageSource.getMessage("common.generateDrawStrokeDiv.dialog.close", null, Locale.getDefault());
		
		List<KanjivgEntry> strokePathsForWord = dictionaryManager.getStrokePathsForWord(word);

		final int width = 800;
		final int height = 200;
		
		out.println("<div id=\"" + dialogId + "\" width=\"" + width + "\" height=\"" + height + "\" class=\"modal fade\">\n");
		out.println("  <div class=\"modal-dialog\" style=\"width: " + width + "px\">\n");
		out.println("    <div class=\"modal-content\">\n");
		out.println("      <div class=\"modal-header\">\n");
		out.println("        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>\n");
		out.println("        <h4 class=\"modal-title\">" + dialogTitle + "</h4>\n");
		out.println("      </div>\n");
		out.println("      <div class=\"modal-body\">\n");
		out.println("        <div id=\"" + dialogId + "Drawing\" />");
				
		out.println("<script>");
		out.println("	var pathObj = {");
		out.println("	    \"" + dialogId + "Drawing\": {");
		out.println("	        \"strokepath\": [");
				
		for (int currentStrokePathsIdx = 0; currentStrokePathsIdx < strokePathsForWord.size(); ++currentStrokePathsIdx) {
			
			KanjivgEntry kanjivgEntry = strokePathsForWord.get(currentStrokePathsIdx);
			
			List<String> strokePaths = kanjivgEntry.getStrokePaths();
			
			for (int strokePathIdx = 0; strokePathIdx < strokePaths.size(); ++strokePathIdx) {
				
				String currentStrokePath = strokePaths.get(strokePathIdx);

				out.println("				{");
		        out.println("					\"path\": \"" + currentStrokePath + "\",");
		        out.println("					\"duration\": 400,");
		        out.println("					\"translateX\": " + (currentStrokePathsIdx * 100));
		        
		        if (strokePathIdx != strokePaths.size() - 1 || currentStrokePathsIdx < strokePathsForWord.size()) {
		        	out.println("				},");
		       	
		        } else {
		        	out.println("				}");
		        }								
			}
		}

		out.println("	        ],");
		out.println("	        \"dimensions\": {");
		out.println("	            \"width\": " + (width - 80) + ",");
		out.println("	            \"height\": " + (height - 80));
		out.println("	        }");
		out.println("	    }");
		out.println("	};");

		out.println("	$(document).ready(function() {");
		
		out.println("		$('#" + dialogId + "Drawing').lazylinepainter({");
		out.println("			\"svgData\": pathObj,");
		out.println("			\"strokeWidth\": 5,");
		out.println("			\"strokeColor\": \"#262213\",");
		out.println("	        \"viewBoxX\": 0,");
		out.println("	        \"viewBoxY\": 0,");
		out.println("	        \"viewBoxWidth\": " + (1.5 * (width - 80)) + ",");
		out.println("	        \"viewBoxHeight\": " + (1.5 * (height - 80)) + ",");
		out.println("	        \"viewBoxFit\": false");
		out.println("		});");
				
		out.println("	});");

		out.println("</script>");
		
		out.println("      </div>\n");
		out.println("      <div class=\"modal-footer\">\n");
		out.println("        <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">" + closeButtonTitle + "</button>\n");
		out.println("        <button type=\"button\" class=\"btn btn-primary\" onclick=\"draw" + dialogId + "Again()\">" + drawAgainButtonTitle + "</button>\n");
		out.println("      </div>\n");
		out.println("    </div><!-- /.modal-content -->\n");
		out.println("  </div><!-- /.modal-dialog -->\n");
		out.println("</div><!-- /.modal -->\n");
		
		out.println("<script>");
		
		out.println("   function draw" + dialogId + "Again() {");
		out.println("      $( '#" + dialogId + "Drawing').lazylinepainter('erase');");
		out.println("      setTimeout(function() { $( '#" + dialogId + "Drawing').lazylinepainter('erase'); $('#" + dialogId + "Drawing').lazylinepainter('paint'); }, 700);");
		out.println("   }");
		
		out.println("</script>");
	}
}
