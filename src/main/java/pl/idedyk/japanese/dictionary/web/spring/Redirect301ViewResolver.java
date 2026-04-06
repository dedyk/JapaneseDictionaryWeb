package pl.idedyk.japanese.dictionary.web.spring;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;

@Component
public class Redirect301ViewResolver implements ViewResolver {

    @Override
    public View resolveViewName(String viewName, Locale locale) {
    	
        if (viewName.startsWith("redirect301:")) {
            String url = viewName.substring("redirect301:".length());
            
            RedirectView view = new RedirectView(url);
            view.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            
            return view;
        }
        
        return null; // dalej inne resolvery
    }
}