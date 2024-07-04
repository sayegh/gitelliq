package com.gitelliq.gqhc.jpa;

import static com.gitelliq.gqhc.Constants.INFO;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.gitelliq.gqhc.Constants;

@WebListener
public class AppInit implements ServletContextListener {

    private static final Logger LOG = Constants.AppInit;

    public void contextInitialized(ServletContextEvent sce) {
    	
		LOG.log(INFO, "contextInitialized(...)");
    	
    }

    public void contextDestroyed(ServletContextEvent sce) {
    	
		LOG.log(INFO, "contextDestroyed(...)");
    	
         PersistenceUnit.close(sce.getServletContext());
    }

}