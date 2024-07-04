package com.gitelliq.gqhc.jersey;

/**
 * 	The <code>PersistenceException</code> is thrown when trying to create an
 * 	<code>EntityManager</code>. Distinguishing this exception from other
 * 	persistence exceptions thrown by JPA makes it much easier to see initialisation
 * 	problems in the log.	
 * 
 * 	@author sme
 */

public class PersistenceException extends Exception {

	/*	Thank you eclipse
	 */

	private static final long serialVersionUID = 8328702584074017333L;

	/**
	 * 	Create an exception with the given message and JPA exception.
	 * 
	 * 	@param m	Message
	 * 	@param c	JPA Exception
	 */
	
	public PersistenceException(String m, Throwable c) { super(m, c); }
}
