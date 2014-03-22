package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import pl.idedyk.japanese.dictionary.api.dto.KanjivgEntry;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

public class GenerateDrawStrokeDialog {
	
	public static void generateDrawStrokeDialog(JspWriter out, DictionaryManager dictionaryManager, 
			String word, String dialogId, String dialogTitle) throws IOException {
		
		List<KanjivgEntry> strokePathsForWord = dictionaryManager.getStrokePathsForWord(word);

		final int width = 800;
		final int height = 200;
		
		
		out.println("<div id=\"" + dialogId + "\" width=\"" + width + "\" height=\"" + height + "\" class=\"modal fade\">\n");
		out.println("  <div class=\"modal-dialog\">\n");
		out.println("    <div class=\"modal-content\">\n");
		out.println("      <div class=\"modal-header\">\n");
		out.println("        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>\n");
		out.println("        <h4 class=\"modal-title\">" + dialogTitle + "</h4>\n");
		out.println("      </div>\n");
		out.println("      <div class=\"modal-body\">\n");
		out.println("        <div id=\"" + dialogId + "Drawing\" />");
				
		out.println("<script>");
		out.println("	var pathObj = {");
		out.println("	    \"" + dialogId + "\": {");
		out.println("	        \"strokepath\": [");
		
		int fixme = 1;
		// scalowanie, aby zmiescilo sie
		// szybkosc pisania od dlugosci pisania (mniej wiecej)
		// ponowne pisanie
		
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
		
		out.println("		$('#" + dialogId + "').lazylinepainter({");
		out.println("			\"svgData\": pathObj,");
		out.println("			\"strokeWidth\": 5,");
		out.println("			\"strokeColor\": \"#262213\",");
		out.println("	        \"viewBoxX\": 0,");
		out.println("	        \"viewBoxY\": 0,");
		out.println("	        \"viewBoxWidth\": " + (1.65 * (width - 80)) + ",");
		out.println("	        \"viewBoxHeight\": " + (1.65 * (height - 80)) + ",");
		out.println("	        \"viewBoxFit\": false");

		out.println("		}).lazylinepainter('paint');");
		
		/*
		out.println("$( \"#" + dialogId + "\" ).dialog({minWidth: " + width + 
				", minHeight: " + height + ", maxWidth: " + width + ", maxHeight: " + height + "});");
		*/
		
		out.println("$( \"#" + dialogId + "\" ).modal();"); 
		out.println("	});");

		out.println("</script>");
		
		out.println("      </div>\n");
		out.println("      <div class=\"modal-footer\">\n");
		out.println("        <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">FFFF Close</button>\n");
		out.println("        <button type=\"button\" class=\"btn btn-primary\">FFFFF Save changes</button>\n");
		out.println("      </div>\n");
		out.println("    </div><!-- /.modal-content -->\n");
		out.println("  </div><!-- /.modal-dialog -->\n");
		out.println("</div><!-- /.modal -->\n");
	}

}
