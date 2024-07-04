package com.gitelliq.gqhc.jersey;

import static com.gitelliq.gqhc.Constants.DEBUG;
import static com.gitelliq.gqhc.Constants.INFO;
import static com.gitelliq.gqhc.Constants.WARN;

import static com.gitelliq.gqhc.jpa.PersistenceUnit.getEntityManager;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gitelliq.gqhc.Constants;
import com.gitelliq.gqhc.registration.Engine;
import com.gitelliq.gqhc.registration.MqttUser;
import com.sun.jersey.spi.resource.Singleton;



@Singleton
@Path("user-registration")
public class RegistrationServer {

    private static final Logger LOG = Constants.Registration;

	private static SecureRandom random = new SecureRandom();
	  
	private class Config {
		
		public String DEMO_GQID = null;
		
		public Config get(final ServletConfig config) {

			return RegistrationServer.this.CONFIG = new Config() {

				{
					DEMO_GQID = config.getInitParameter("com.gitelliq.gqhc.DEMO_GQID");
				}

				@Override
				public Config get(ServletConfig config) {

					return RegistrationServer.this.CONFIG;
				}
			};
		}
	}
	
	private Config CONFIG = new Config();

//	protected EntityManager getEntityManager() throws NamingException {
//		
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("GQ-Central");
//		return emf.createEntityManager();
//	}

	public MqttUser findById(EntityManager em, String id) {
		
		String qstring = "SELECT c FROM MqttUser c WHERE c.engine.id = :id";
		TypedQuery<MqttUser> query = em.createQuery(qstring, MqttUser.class);
		
		query = query.setParameter("id", id);
		List<MqttUser> resultList = query.getResultList();
		
		return resultList.size() == 1 ? resultList.get(0) : null;
	}
	
	
//	public Engine findEngineById(String id) throws NamingException {
//		
//		String qstring = "SELECT c FROM Engine c WHERE c.id = :id";
//		TypedQuery<Engine> query = getEntityManager().createQuery(qstring, Engine.class);
//		
//		query = query.setParameter("id", id);
//		List<Engine> engines = query.getResultList();
//		
//		return engines.size() == 1 ? engines.get(0) : null;
//	}
	
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putRegistration(@Context HttpServletRequest request, 
									@Context ServletConfig config,
									MqttUser registration) throws IOException 
	{
		LOG.log(DEBUG, "RegistrationServer.putRegistration");
		LOG.log(DEBUG, "Engine: " + registration.getEngine());

		// registration.setMailAddress("@");
	
		EntityManager em = null;
		Engine engine = null;
		MqttUser existing = null;
		Throwable _t = null;
		
		try {

			em = getEntityManager(request.getServletContext());
			engine = Engine.findById(em, registration.getEngine().getId());

			if (engine == null) {
				LOG.log(WARN, "Engine ID does not exist, response = 400");
				return Response.status(Response.Status.BAD_REQUEST).entity("Engine ID does not exist").build();
			}

			existing = findById(em, registration.getEngine().getId());

			if (existing == null) {
				String password;
				if (engine.getId().equals(CONFIG.get(config).DEMO_GQID))
					password = "u1s6e9d91pg9eg3u3bm4l2k9o4";
//					password = "demo";
				else
					password = newPassword();
				
				LOG.log(DEBUG, password);
				String pkb = toPBKDF2(password);
				LOG.log(DEBUG, pkb);
				
				registration.setMqttUser(registration.getEngine().getId());
				registration.setMqttPassword(password);
				registration.setMqttHash(pkb);
				
				LOG.log(INFO, "CREATING: for " + registration.getEngine().getId());

				em.getTransaction().begin();
				em.persist(registration);
				em.getTransaction().commit();
				
				return Response.status(Response.Status.OK).entity(registration.forJSON()).build();

			} else {
//				if (registration.getForce()) {
//					String password = newPassword();
//					LOG.log(DEBUG, password);
//					String pkb = toPBKDF2(password);
//					LOG.log(DEBUG, pkb);
//					
//					existing.setMqttPassword(password);
//					existing.setMqttHash(pkb);
//					
//					em.getTransaction().begin();
//					em.merge(existing);
//					em.getTransaction().commit();
//			    	
//			    	registration = existing;
//				} else
				{
					LOG.log(WARN, "Registration already exists, response = 400");
					return Response.status(Response.Status.BAD_REQUEST).entity("Registration already carried out for this engine ID").build();
				}
			}
			
		} 	catch (PersistenceException e) { 	_t = e;
		}	catch (RuntimeException re) {		_t = re;
		}	finally {
			if (em!=null) em.close();
		}
		
		if (_t != null) {
			
        	LOG.log(WARN, _t.toString(), _t);	
        	
        	//	TODO:	Only do this for dev requests.
        	
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(_t.toString()).build();			
		}
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();					
	}
	
	
	  private static final int ITERATIONS = 901;
	  private static final int KEY_LENGTH = 128; // bits
	  
	  public static byte[] hashPassword(String password, byte[] saltBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
	    char[] passwordChars = password.toCharArray();

	    PBEKeySpec spec = new PBEKeySpec(
	        passwordChars,
	        saltBytes,
	        ITERATIONS,
	        KEY_LENGTH
	    );
	    SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    byte[] hashedPassword = key.generateSecret(spec).getEncoded();
	    return hashedPassword;
	    // return String.format("%x", new BigInteger(hashedPassword));
	  }
	  
	  public static byte[] getNextSalt() {
		    byte[] salt = new byte[16];
		    random.nextBytes(salt);
		    return salt;
		  }
	  
	public static String toPBKDF2(String password) {
		
		String result = null;
		
		byte[] salt = getNextSalt();
		
		try {
			byte[] hashed = hashPassword(password, salt);
//		    byte[] encodedSalt = Base64.getEncoder().encode(salt);
//			byte[] encodedBytes = Base64.getEncoder().encode(hashed);
		    result = "PBKDF2" +"$" + "sha256" + "$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hashed);
		    LOG.log(DEBUG, "toPBKDF2: " + result);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//	    MessageDigest md_SHA256 = null;
//	    try {
//	        md_SHA256 = MessageDigest.getInstance("SHA-256");
//	    }
//	    catch(NoSuchAlgorithmException e) {
//	        e.printStackTrace();
//	    } 
	    

	    
	    
//	    String result = "";
//	    for (int i=0; i < b.length; i++) {
//	        result +=
//	           Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
//	    }
	    return result;
	}

	public String newPassword() {
		return new BigInteger(130, random).toString(32);
	}
}
