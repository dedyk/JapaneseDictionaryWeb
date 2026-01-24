package pl.idedyk.japanese.dictionary.web.spring;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class SessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    	// FM_FIXME: ustawiamy domyslny motyw strony
        se.getSession().setAttribute("theme", "dark");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // noop
    }
}