package pl.idedyk.japanese.dictionary.web.taglib;

import java.util.List;

import javax.servlet.jsp.tagext.TagSupport;

import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Li;
import pl.idedyk.japanese.dictionary.web.html.Script;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Ul;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;

public abstract class GenerateDictionaryDetailsTagAbstract extends TagSupport {

	private static final long serialVersionUID = 1L;

	protected Div generateMenu(Menu mainMenu) {
		
		Div menuDiv = new Div("col-md-2");
		
        Ul ul = new Ul(null, "width: 300px");
		menuDiv.addHtmlElement(ul);
		
		ul.setId("sidebar");
		
		generateMenuSubMenu(ul, mainMenu.getChildMenu());
		
		Script script = new Script();
		
		StringBuffer scriptBody = new StringBuffer();
		
		scriptBody.append("$(function() {\n");
		scriptBody.append("$( \"#sidebar\" ).menu(); \n");
		scriptBody.append("});\n\n");
				
		scriptBody.append("$(function() {\n");
		scriptBody.append("\n");
		scriptBody.append("    var $sidebar   = $(\"#sidebar\"), \n");
		scriptBody.append("        $window    = $(window),\n");
		scriptBody.append("        offset     = $sidebar.offset(),\n");
		scriptBody.append("        topPadding = 25;\n");
		scriptBody.append("\n");
		scriptBody.append("    $window.scroll(function() {\n");
		scriptBody.append("        if ($window.scrollTop() > offset.top) {\n");
		scriptBody.append("            $sidebar.stop().animate({\n");
		scriptBody.append("                marginTop: $window.scrollTop() - offset.top + topPadding\n");
		scriptBody.append("            });\n");
		scriptBody.append("        } else {\n");
		scriptBody.append("            $sidebar.stop().animate({\n");
		scriptBody.append("                marginTop: 0\n");
		scriptBody.append("            });\n");
		scriptBody.append("        }\n");
		scriptBody.append("    });\n");
		scriptBody.append("    \n");
		scriptBody.append("})\n");
		
		script.addHtmlElement(new Text(scriptBody.toString()));
		
		menuDiv.addHtmlElement(script);
		        
        return menuDiv;
	}
	
	private Ul generateMenuSubMenu(Ul parentUl, List<Menu> menuList) {
		
		Ul ul = null;
		
		if (parentUl != null) {
			ul = parentUl;
			
		} else {
			ul = new Ul(null, "width: 370px");
		}
				
		for (Menu currentMenuList : menuList) {
			
			Li li = new Li();
			ul.addHtmlElement(li);
			
			A link = new A(null, "padding-bottom: 0px; padding-top: 0px");
			li.addHtmlElement(link);
			
			link.setHref("#");
			
			link.setOnClick("$('html, body').animate({ " 
					+ "scrollTop: $('#" + currentMenuList.getId() + "').offset().top - 15 " 
					+ "}, 1000); return false; ");
					
			link.addHtmlElement(new Text(currentMenuList.getTitle()));
			
			if (currentMenuList.getChildMenu().size() > 0) {
				li.addHtmlElement(generateMenuSubMenu(null, currentMenuList.getChildMenu()));
			}
		}		
		
		return ul;		
	}

	
}
