package com.gitelliq.gqhc.jpa;

import static com.gitelliq.gqhc.Constants.DEBUG;
import static com.gitelliq.gqhc.Constants.WARN;
import static com.gitelliq.gqhc.Constants.INFO;

import java.lang.ref.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.gitelliq.gqhc.Constants;
import com.gitelliq.gqhc.jersey.PersistenceException;

/**
 * 	Utility class primarily for use in JSPs as the preferred method for obtaining
 * 	the entity manager. The <code>PersistenceUnit</code> is a singleton wrapping
 * 	an instance of <code>EntityManagerFactory</code> that is stored in the
 * 	application context. Ideally other code should use this method as well.
 * 
 * 	@author sme
 *
 */

public class PersistenceUnit {

    private static final Logger LOG = Constants.Persistence;

	/*	Name of the persistence unit definition in persistence.xml
	 */
	
    private static final String PERSISTENCE_UNIT_NAME = "GQ-Central";

    /*	EntityManagerFactory for the application persistence unit.
     */
    
    private EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
         
    private List<WeakReference<EntityManager>> managers = 
    		new ArrayList<WeakReference<EntityManager>>();
    
    
    /*	Create an EntityManager for the application persistence unit. Invokes
     * 	the create method on the applications EntityManagerFactory
     */
    
	private EntityManager getEntityManager() throws PersistenceException {
		
		try {
			
//			return new EntityManagerProxy(factory.createEntityManager(), managers);
			return factory.createEntityManager();
//		}	catch (NamingException ne) {
//			throw new PersistenceException("Failed: createEntityManager() ", ne);
		} 	catch (javax.persistence.PersistenceException pe) {
			throw new PersistenceException("Failed: createEntityManager() ", pe);
		}
	}	

	/*
	 * 
	 */
	
	private void close() { factory.close(); }

	/**
	 * 	Create an EntityManager for the application persistence unit. The initial
	 * 	invocation will create and instance of <code>PersistenceUnit</code> and
	 * 	store it in the application context. This instance is used by all future
	 * 	calls this to this method.
	 * 
	 * 	@param context	The application context
	 * 	@return	A new instance of EntityManager using the application persistence
	 * 			unit
	 * 	@throws PersistenceException Typically if the persistence unit is not 
	 * 			available
	 */
	
	public static EntityManager getEntityManager(ServletContext context) throws PersistenceException {
		
		String key = PersistenceUnit.class.getName();
		PersistenceUnit pu = (PersistenceUnit) context.getAttribute(key);

		if (pu==null) context.setAttribute(key, (pu=new PersistenceUnit()));
		
		return pu.getEntityManager();
	}

	
	public static void close(ServletContext context) {
		
		LOG.log(INFO, "close(...)");
		
		String key = PersistenceUnit.class.getName();
		PersistenceUnit pu = (PersistenceUnit) context.getAttribute(key);
		
		if (pu!=null) pu.close();
	}
	
}
