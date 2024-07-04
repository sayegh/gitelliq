package com.gitelliq.gqhc;

import java.util.logging.Logger;

public class Constants {

	public static java.util.logging.Level DEBUG = java.util.logging.Level.FINER;
	public static java.util.logging.Level WARN = java.util.logging.Level.WARNING;
	public static java.util.logging.Level INFO = java.util.logging.Level.INFO;
	public static java.util.logging.Level ERROR = java.util.logging.Level.SEVERE;

	/*	God knows what is going on here - the loggers created in the entity
	 * 	classes(?) are not configured by the TomCat logging configuration.
	 * 
	 * 	However - declaring the loggers here has one advantage, it makes it
	 * 	easier to maintain a consistent "compact" naming convention.
	 */
	
	public static final Logger Example30 = 	  Logger.getLogger("0123456789012345678901234567890");
	public static final Logger RestServer =   Logger.getLogger("c.gq.hc.jersey.RestServer");
	public static final Logger Registration = Logger.getLogger("c.gq.hc.jersey.RgstrtionServer");
	public static final Logger Engine = 	  Logger.getLogger("c.gq.hc.registration.Engine");
	public static final Logger MqttUser = 	  Logger.getLogger("c.gq.hc.registration.MattUser");
	public static final Logger Captcha = 	  Logger.getLogger("c.gq.hc.captcha.CaptchaServlet");
    public static final Logger TokenSet = 	  Logger.getLogger("c.gq.hc.captcha.CaptchaTokenSet");
    public static final Logger Persistence =  Logger.getLogger("c.gq.hc.jpa.PersistenceUnit");
    public static final Logger AppInit =      Logger.getLogger("c.gq.hc.jpa.AppInit");
}
