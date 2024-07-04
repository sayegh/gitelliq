package com.gitelliq.gqhc.jersey;

import static com.gitelliq.gqhc.Constants.DEBUG;
import static com.gitelliq.gqhc.Constants.WARN;
import static com.gitelliq.gqhc.Constants.ERROR;
import static com.gitelliq.gqhc.Constants.INFO;
import static com.gitelliq.gqhc.jpa.PersistenceUnit.getEntityManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.gitelliq.gqhc.Constants;
import com.gitelliq.gqhc.captcha.CaptchaServlet;
import com.gitelliq.gqhc.captcha.CaptchaTokenSet;
import com.gitelliq.gqhc.registration.Challenge;
import com.gitelliq.gqhc.registration.Engine;
import com.gitelliq.gqhc.registration.IpInterface;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("engine")
public class RestServer {

	
    private static final Logger LOG = Constants.RestServer;
	       
	/*	Private class collecting the configuration for the service. 
	 * 	Configuration is via the servlet init-parameters (or some other
	 * 	aspect of the servlet, like path).
	 * 	
	 * 	Although there may be an initial race causing multiple initializations, 
	 * 	once they have finished, further calls to get will return the initialized
	 * 	Config instance stored in CONFIG.
	 */
	
	private class Config {

		/*
		 * If the context path contains "dev" run in a development mode, which
		 * uses MAC and remote-IP to distinguish engines when registering.
		 */

		public boolean DEV_ENV = false;
		
		/*
		 * 
		 */
		
		public int MAX_PENDING = 2;

		/*
		 * 
		 */
		
		public int MAX_INFORM_AGE =  60 * 60;	// 1 hour
		
		/*	
		 * 
		 */
		
		public String DEV_ENV_IP = null;
		
		/*
		 * 
		 */
		
		public String DEMO_LID = null;
		public String DEMO_GQID = null;
		
		/**
		 * 
		 * @param config
		 * @return
		 */
		
		public Config get(final ServletConfig config) {

			return RestServer.this.CONFIG = new Config() {

				{
					String param = config.getInitParameter("com.gitelliq.gqhc.MAX_PENDING");
					MAX_PENDING = param == null ? 2 : Integer.parseInt(param);
					param = config.getInitParameter("com.gitelliq.gqhc.DEV_ENV");
					DEV_ENV = config.getServletContext().getContextPath().contains("dev");
					DEV_ENV |= ("true").equals(param);
					param = config.getInitParameter("com.gitelliq.gqhc.MAX_INFORM_AGE");
					if (param!=null) MAX_INFORM_AGE = Integer.parseInt(param);
					param = config.getInitParameter("com.gitelliq.gqhc.DEV_ENV_IP");
					if (param!=null) DEV_ENV_IP = param;
					if (param!=null) DEV_ENV = true;
					DEMO_LID = config.getInitParameter("com.gitelliq.gqhc.DEMO_LID");
					DEMO_GQID = config.getInitParameter("com.gitelliq.gqhc.DEMO_GQID");
				}

				@Override
				public Config get(ServletConfig config) {

					return RestServer.this.CONFIG;
				}
			};
		}
	}
	
	private Config CONFIG = new Config();
		
	public RestServer() {}
		
	
	/**
	 * 	Fetch an engine identified by it's remote-IP and optional MAC-address. The IP is
	 * 	determined from the request, the MAC-address is query parameter. 
	 * 
	 * 	@param request
	 * 	@param mac
	 * 	@return
	 */
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findEngine(	@Context HttpServletRequest request,
								@Context ServletConfig config, @QueryParam("mac") 
								String mac, @QueryParam("captcha") String captcha,
								@QueryParam("ctoken") String ctoken)
	{
		CONFIG.get(config);
		
		List<Engine> engines = new ArrayList<Engine>();;
		List<String> macs = new ArrayList<String>();
		String ip = request.getRemoteAddr();
		Response.Status status = null;
		EntityManager em = null;
		Object entity = null;
		Engine engine = null;
		Throwable _t = null;

		do try {
			
			em = getEntityManager(config.getServletContext());
			
			//	Verify the request captcha. The ctoken may need verifying
			//	or already have been verified (the ctoken does not expire
			//	immediately). If the verification fails the response 
			//	contains a challenge.
			
			if (!CaptchaTokenSet.getInstance().verified(ctoken, captcha))  {
						
				Challenge challenge = null;

				ctoken = CaptchaTokenSet.getInstance().next();

				if (captcha==null) challenge = new Challenge("CAPTCHA","User verification required");
				else  challenge = new Challenge("BAD_CAPTCHA","User verification failed");

				challenge.setCtoken(ctoken);
				challenge.setUrl(CaptchaServlet.buildLink(request, ctoken, true));
				entity = challenge;
				status = Status.OK;
				
				break;									//	Construct response
			}
			
			//	Look for an engine matching the remote-IP and MAC-address.
			//	If the MAC-address has not been required, we can use a
			//	faster finder-method.
			//	 - findByRemoteIp => List<Engine>
			//	 - findByMaxAndRemoteIp => Engine/PersistenceException
			
			if (mac==null)
				engines = Engine.findByRemoteIp(em, ip, CONFIG.MAX_INFORM_AGE);

			else {

				mac = mac.replaceAll("(\\w\\w)", ":$1").toUpperCase().substring(1);
				macs.add(mac);
				engine = Engine.findByMacAndRemoteIp(em, ip, macs, CONFIG.MAX_INFORM_AGE);
				if (engine !=null ) engines.add(engine);
			} 
							
			//	Just in case: If a MAC was provided - maybe it wasn't needed? 
			
			if (mac != null && (engines==null || engines.size()==0))
				engines = Engine.findByRemoteIp(em, ip, CONFIG.MAX_INFORM_AGE);
			
			status = Status.OK;
			
			if (engines==null || engines.size()==0) {		//	Not found
				
				status = Status.NOT_FOUND;
				
			}	else if (engines.size()!=1) {				//	Found multiple
				
				entity = new Challenge("MAC_ADDRESS");

			}	else {										//	Found
			
				Engine found = engines.get(0);
				String foundSecret = found.getSecret();
												
				//	Delete current secret and/or set the verified time-stamp
				
				if (foundSecret!=null || found.isPending()) {
					
					if (found.isPending()) {
						
						LOG.log(INFO, "VERIFIED: " + found);
						found.setVerifiedAt(new Date());
					}

					found.setSecret(null);
					em.getTransaction().begin();
					em.merge(found);
				    em.getTransaction().commit();
				}
				
				found.setSecret(foundSecret);
				entity = found;
			}
			
		} 	catch (PersistenceException pe) {	_t = pe;
		} 	catch (EngineLookupException ele) {	_t = ele;
		}	catch (RuntimeException re) { 		_t = re;
		}	finally {
			if (em!=null) em.close();
			if (LOG.isLoggable(DEBUG)) logMemStats(LOG, DEBUG);
		}	while (false);
			
		//	If there is no status something must have gone wrong (and there 
		//	should be an exception).
		
		if (status == null) {
			
			status = Status.INTERNAL_SERVER_ERROR;
			entity = _t!=null ? _t.toString() : "no exception";
			LOG.log(ERROR, "find(ip: "+ip+", mac:"+mac+") => Internal Error ("+entity+")");
			if (!CONFIG.DEV_ENV) entity=null;
		}
		else if (LOG.isLoggable(DEBUG))
			LOG.log(DEBUG, "find(ip: "+ip+", mac:"+mac+") => " + status + " ("+entity+")");
			
		ResponseBuilder builder = Response.status(status);
		
		if (entity!=null) builder = builder.entity(entity);
		builder = builder.header("Access-Control-Allow-Origin", "*");
		builder = builder.header("Access-Control-Allow-Methods", "GET,OPTIONS");
		builder = builder.header("Connection", "close");
		
		return builder.build();
	}
		
		
	/**
	 * 	Fetch an engine identified by it's GQ-ID.
	 * 
	 * 	@param request
	 * 	@param id
	 * 	@return
	 */
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEngine(@Context HttpServletRequest request, @Context ServletConfig config, @PathParam("id") String id) {
		
		CONFIG.get(config);

		ResponseBuilder builder = null;
		Engine engine = null;
		EntityManager em = null;
		String secret = null;
		Throwable _t = null;
		
		try {
			
			if ((engine = Engine.findById(em=getEntityManager(config.getServletContext()), id)) != null) {
				
				if (LOG.isLoggable(DEBUG))
					LOG.log(DEBUG, "find(id: "+id+") => OK");
				builder = Response.status(Response.Status.OK).entity(engine)	;
				secret = engine.getSecret();
				
				if (secret!=null) {
					
					engine.setSecret(null);
					em.getTransaction().begin();
					em.merge(engine);
					em.getTransaction().commit();
					engine.setSecret(secret);
				}
			}
		}	catch (RuntimeException re) {		_t = re;
		} 	catch (PersistenceException e) {	_t = e;
		}	finally {
			if (em!=null) em.close();
			logMemStats(LOG, DEBUG);
		}
		
		if (builder==null) {
			
			if (LOG.isLoggable(DEBUG))
				LOG.log(DEBUG, "find(id: "+id+") => 404 ("+_t+")");
			builder = Response.status(Response.Status.NOT_FOUND);
			if (CONFIG.DEV_ENV && _t!=null) builder.entity(_t.toString());
		}
		
		builder = builder.header("Connection", "close");
		
        return builder.header("Access-Control-Allow-Origin", "*").build();
	}
	

	/**	
	 * 	Register or update an engine. Update is a little complicated, as the list
	 * 	of interfaces has to be updated - interface entries may need to be created
	 * 	and deleted.
	 * 
	 * 	An engine registration is created for any PUT request without a
	 * 	GQID. The registration is initially marked as pending. The pending
	 * 	flag is removed, when a GET request for that remote-IP and (possibly)
	 * 	MAC are received. This GET request requires the completion of a 
	 * 	captcha dialog.
	 * 
	 * 	The number of pending registrations is limited (old registrations
	 * 	are deleted before creating the new registration. So (in theory) 
	 * 	this sets a limit on the number of registrations for which the
	 * 	captcha dialog was completed.
	 * 
	 * 	@param request
	 * 	@param engine
	 * 	@return
	 */


	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putEngine(@Context HttpServletRequest request, @Context ServletConfig config, Engine engine) {

		Engine existing = null;
		EntityManager em = null;
		Throwable _t = null;
		
		CONFIG.get(config);
		engine.setRemoteIp(request.getRemoteAddr());
		
		if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "putEngine("+engine+")");
		
		if (CONFIG.DEV_ENV && engine.getRemoteIp().equals("127.0.0.1") && engine.getInterfaces().isEmpty()) {
			
			engine.getInterfaces().add(new IpInterface("FF:FF:FF:FF:FF:FF","127.0.0.1"));
		}
		
		try {
			
			em = getEntityManager(config.getServletContext());
			
			if (engine.getId() == null) {
				if (CONFIG.DEMO_LID!=null && engine.getlocalId().equals(CONFIG.DEMO_LID)) {
					if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "Setting engine ID to " + CONFIG.DEMO_GQID);
					engine.setId(CONFIG.DEMO_GQID);
				} else {
					String uuid = UUID.randomUUID().toString();
					if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "Setting engine ID to " + uuid);
					engine.setId(uuid);
				}
			} else {

				if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "findBy("+engine.getId()+")");				
				existing = Engine.findById(em, engine.getId());
			}

			if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "existing: " + existing);							
			
			//	In dev-mode, if an existing engine has not been found, select
			//	an engine matching the remote-IP and MAC address. This means
			//	after deleting their local DB, the engine will receive the
			//	same GQID, and paired clients will not need to re-resolve the
			//	engine (although they will need repairing).
			
			if (existing == null && CONFIG.DEV_ENV) {
				
				if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "DEV_ENV: DEV_ENV_IP=" + CONFIG.DEV_ENV_IP);							
				
				if (CONFIG.DEV_ENV_IP!=null && engine.getRemoteIp().equals(CONFIG.DEV_ENV_IP)) {
					
					if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "DEV_ENV_IP Matched");							

					List<String> macs = new ArrayList<String>();
					
					for (IpInterface ipif : engine.getInterfaces()) macs.add(ipif.getMac());
					
					existing = Engine.findByMacAndRemoteIp(em, engine.getRemoteIp(), macs, 0);
				}
			}
						
			if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "existing: " + existing);							

			em.getTransaction().begin();
	        
			if (existing != null)  engine = existing.update(em, engine);
			else
			{
				Engine.limitPending(em, CONFIG.get(config).MAX_PENDING-1);
				engine = engine.persist(em);
			}
	        
	        em.getTransaction().commit();
			if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "=> OK : " + engine);							
			return Response.status(201).entity(engine).build();

        } 	catch (PersistenceException e) {	_t = e;
		}	catch (RuntimeException re) {		_t = re;
		} 	catch (EngineLookupException ele) {	_t = ele;
		}	finally {
			if (em!=null) em.close();
			logMemStats(LOG, DEBUG);
		}
		
        ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);					

        if (CONFIG.DEV_ENV && _t!=null) builder.entity(_t.toString());
        	
		if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "=> 500 ("+(_t)+") : " + engine);							
    	return builder.build();			
	}
	
	private void logMemStats(Logger log, Level level) {
		
		Runtime rt = Runtime.getRuntime();
		StringBuilder sb = new StringBuilder("MEMSTATS: ");

		sb.append("max=").append(rt.maxMemory());
		sb.append(";total=").append(rt.totalMemory());
		sb.append(";free=").append(rt.freeMemory());

		log.log(level, sb.toString());
	}
}
