package com.interface21.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstrap listener to start up Spring's root WebApplicationContext.
 * Simply delegates to ContextLoader.
 *
 * <p>Note: This listener should be registered after Log4jConfigListener,
 * if the latter is used.
 *
 * @author Juergen Hoeller
 * @see com.interface21.web.util.Log4jConfigListener
 * @since 17.02.2003
 */
public class ContextLoaderListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		ContextLoader.initContext(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event) {
	}

}
