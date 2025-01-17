package com.gitelliq.gqhc.registration;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.gitelliq.gqhc.Constants;
import com.gitelliq.gqhc.jersey.EngineLookupException;
import com.sun.istack.NotNull;

import static com.gitelliq.gqhc.Constants.DEBUG;
import static com.gitelliq.gqhc.Constants.WARN;
import static com.gitelliq.gqhc.Constants.INFO;


/**
 * 	Represents a Gitelliq engine for the purposes of storage and serialization.	
 * 
 * 	This class can be persisted because they are declared in:
 * 
 * 	<pre>src/main/resources/persistence.xml</pre>
 * 
 *  Annotations define the details of the persistence.
 * 
 * 	@author sme
 */

@Entity
public class Engine implements Serializable {

    /**
	 * 
	 */
	
	private static final long serialVersionUID = 5088247188663961869L;

	private static final Logger LOG = Constants.Engine;
	    
	/**
	 * 	The unique engine identifier (GQ-ID). An UUID generated by GQ-HC when the server 
	 * 	is registered.
	 */
	
	@Id
	@NotNull
	@Column()
	String id = null;
		
	/**
	 * 	The identifier generated by the engine. Included in registration but currently not used.
	 */
	
	@Column()
	String localID = null;

	/**
	 * 	The remote IP associated with the engine. Determined by GQ-GC when the registration
	 * 	request is received.
	 */
	
	@Column()	
	String remoteIP = null;
	
	/**
	 * 	The port the engine listens for HTTP requests on. 
	 */
	
	@Column()
	int httpdPort = 0;
	
	/**
	 * 	The current <em>shared secret</em>. All requests to Gitelliq are encrypted via shared
	 * 	keys. The initial shared key must be retrieved from GQ-HC. 
	 */
	
	@Column(nullable=true)
	String secret = null;
	
	/**
	 * 	The interfaces of the Gitelliq host. This information is needed to construct a 
	 * 	local URL for clients in the same network as the Gitelliq host. The interfaces
	 * 	include the MAC-address, which can also be used when resolving a Gitelliq engine.<p/>
	 * 	
	 * 	The host can have multiple interfaces - hence a <code>List</code>, hence stored
	 * 	in a separate table and hence a <code>OneToMany</code> relation.
	 * 
	 * 	<ul>
	 * 	<li>
	 * 		<code>mappedBy</code>: To join the <code>engine</code> component of the 
	 * 		interfaces unique key (<code>engineMac</code>) matches this engine.
	 * 	</li>
	 * 	<li>
	 * 		<code>cascade</code>: Persistence operations on the engine should be <em>cascaded</em> to the
	 * 		interfaces. I.e. persist engine => persist interfaces. (Doesn't help
	 * 		with updates though - duh!)
	 * 	</li>
	 * 	<li>
	 * 		<code>orphanRemoval</code>:	Automatically remove any interfaces (the only children
	 * 		the engine has) if they are no longer referenced by an engine.
	 * 	</li>
	 * 	<li>
	 * 		<code>fetch</code>: Any operations on an engine always require the interfaces, so
	 * 		fetch the interfaces when fetching the engine.
	 * 	</li>
	 * 	</ul>
	 * 
	 */
	
	@OneToMany(mappedBy="engineMac.engine", cascade=CascadeType.PERSIST, orphanRemoval=true, fetch=FetchType.EAGER)
	List<IpInterface> interfaces = new ArrayList<IpInterface>();

	@OneToMany(mappedBy="engine", fetch=FetchType.EAGER)
	List<MqttUser> users = new ArrayList<MqttUser>();

	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP) 
	private Date touchedAt = new Date();

	@Column(nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date verifiedAt = null;


	public boolean isRemoteEnabled() {
		
		return !users.isEmpty();
	}

	@JsonIgnore
	public boolean isPending() { return verifiedAt==null;	}

	@JsonIgnore
	public Date getVerifiedAt() { return verifiedAt; }

	public void setVerifiedAt(Date verifiedAt) {
		
		if (this.verifiedAt==null) this.verifiedAt = verifiedAt;
	}

	@JsonIgnore
	public Date getTouchedAt() { return touchedAt; }

	public void setTouchedAt(Date value) { touchedAt = value; }

	public String getId() { return id; }
	
	public String getlocalId() { return localID; }

	public String getRemoteIp() { return remoteIP; }
	
	public String getSecret() { return secret; }
	
	public List<IpInterface> getInterfaces() { return interfaces; }
	
	public void setId(String value) {  id = value; }
	
	public void setlocalId(String value) {  localID = value; }

	public void setRemoteIp(String value) {  remoteIP = value; }
	
	public void setSecret(String value) {  secret = value; }
	
	public void setInterfaces(List<IpInterface> value) { interfaces = value; }
		
	public int getHttpdPort() {
		
		return httpdPort;
	}

	public void setHttpdPort(int httpdPort) {
		
		if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "setHttpdPort("+httpdPort+")");
		this.httpdPort = httpdPort;
	}
	
	public static List<Engine> findAll(EntityManager em, int pageSize, int pageNum) {
		
		if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "findAll(...) => ");

		String qstring = "SELECT c FROM Engine c ORDER BY c.touchedAt DESC";
		TypedQuery<Engine> query = em.createQuery(qstring, Engine.class);

		List<Engine> results = query.getResultList();
		
		if (LOG.isLoggable(DEBUG)) LOG.log(DEBUG, "findAll(...) => " + results.size() + " rows");
		
		return results;
	}
 	
	/**
	 * 	Find an engine with the given GQ-ID. 
	 * 
	 * 	TODO: Use EntityManager:
	 * 
	 * 	Something like; Employee employee = em.find(Employee.class, 1);
	 * 
	 * 	@param id
	 * 	@return
	 * 	@throws NamingException
	 */
	
	public static Engine findById(EntityManager em, String id)
	{
		String qstring = "SELECT c FROM Engine c WHERE c.id = :id";
		TypedQuery<Engine> query = em.createQuery(qstring, Engine.class);
		
		query = query.setParameter("id", id);
		List<Engine> engines = query.getResultList();
		
		
		return engines.size() == 1 ? engines.get(0) : null;
	}
	
	/**
	 * 	Find engines with the given remote IP-address.
	 * 
	 * 	@param remoteIp
	 * 	@return
	 * 	@throws NamingException
	 */
	
	public static List<Engine> findByRemoteIp(EntityManager em, String remoteIp, int max_age) 
	{
		String qstring = "SELECT c FROM Engine c WHERE c.remoteIP = :rip";
		Calendar since = new GregorianCalendar();
		
		if (max_age>0) qstring+=" AND c.touchedAt > :since";
		since.add(Calendar.SECOND, -max_age);
		
		TypedQuery<Engine> query = em.createQuery(qstring, Engine.class);
		
		query = query.setParameter("rip", remoteIp);
		if (max_age>0) query = query.setParameter("since", since.getTime());
		
		return query.getResultList();
	}
	
	/**
	 * 	Find an engine matching the given MAC remote-IP addresses. In general it 
	 * 	is only necessary to match on the remote-IP (or the id). But there can
	 * 	be more than one Gitelliq instance behind a remote-IP. This is the complicated
	 * 	query requiring a join.
	 * @param engine
	 * 
	 * 	@return
	 * 	@throws NamingException
	 * 	@throws EngineLookupException 
	 */
	
	
	public static Engine findByMacAndRemoteIp(EntityManager em, String remoteIp, List<String> macs, int max_age) 
		throws EngineLookupException
	{
		LOG.log(DEBUG, "findByMacAndRemoteIp("+remoteIp+","+macs+","+max_age+")");		
		
		if (macs == null || macs.isEmpty()) return null;
		
		String qstring = "SELECT DISTINCT e FROM IpInterface AS c JOIN c.engineMac.engine AS e WHERE c.engineMac.mac IN :macs AND e.remoteIP = :rip";
		Calendar since = new GregorianCalendar();
		
		if (max_age>0) qstring+=" AND e.touchedAt > :since";
		qstring += " ORDER BY e.touchedAt DESC";
		since.add(Calendar.SECOND, -max_age);

		TypedQuery<Engine> query = em.createQuery(qstring, Engine.class);

		if (max_age>0) query = query.setParameter("since", since.getTime());
		query = query.setParameter("rip", remoteIp);
		query = query.setParameter("macs", macs);

		List<Engine> results = query.getResultList();
		
		if (LOG.isLoggable(DEBUG))
			LOG.log(DEBUG, "   Found " + results.size() + " rows");
		
		if (LOG.isLoggable(DEBUG)) 
			for (Engine ipif : results) LOG.log(DEBUG, "    " + ipif);
		
		if (results.size()<0) 
			throw new EngineLookupException("Multiple matches for " + remoteIp + " + MAC: " + macs);
		if (results.size()>1)
			LOG.log(WARN, "Multiple matches for " + remoteIp + " + MAC: " + macs + " - using most recent");
		
		return results.size() != 0 ? (results.get(0)) : null;
	}
	
	/**
	 * 	When GITELLIQ puts an inform request, if the request does not contain
	 * 	a known engine, a new engine (i.e. database entry) is created. A
	 * 	trivial DoS attack could flood the database with bogus entries. So
	 * 	<code>limitPending</code> is invoked in <code>RestServer</code> before
	 * 	a new engine is created.<p/>
	 * 	   An engine is <em>pending</em> if it has not been <em>verified</em>,
	 * 	i.e. the engine has not been identified via a GET request with a
	 * 	successful reCaptcha interaction.<p/><ul>
	 *  
	 *  <li>Check if the number of pending entries exceeds the limit</li>
	 *  <li>
	 *  	Select pending entries, ordered by access (=creation) time.
	 *  	(Select enough to bring pending under max).
	 *  </li>
	 *  <li>Delete these engines</li></ul>
	 *  
	 *  <b>Notes:</b><ul>
	 *  <li>Should we remove old pending entries?<li>
	 *  <li>Could cause a lot of deletes for a PUT request</li>
	 *  <li>Currently there will usually only be one deletion</li>
	 *  <li>But this is not part of the user experience</li>
	 *  </ul>
	 *  
	 */
		
	static public int limitPending(EntityManager em, int max) {

		TypedQuery<Long> countQuery = em.createQuery("SELECT COUNT(c) FROM Engine c WHERE c.verifiedAt IS NULL", Long.class);
		long pending = countQuery.getSingleResult();
		
		LOG.log(DEBUG, "pending: " + pending + ", max: " + max);
		
		if (pending<=max) return 0;
		
		String qstring = "SELECT c FROM Engine c WHERE c.verifiedAt IS NULL ORDER BY c.touchedAt";
		TypedQuery<Engine> query = em.createQuery(qstring, Engine.class);
		
		query.setMaxResults((int)pending-max);
		
		for (Engine engine : query.getResultList()) {
			
			LOG.log(DEBUG, "REMOVING:  " + engine);
			em.remove(engine);
		}
		
		return (int) (pending-max);
	}
	
	/**
	 * 
	 * 	@param em
	 */
	
	public Engine persist(EntityManager em) {

		LOG.log(INFO, "CREATING: " + this);
		
		em.persist(this);
		for (IpInterface ipif : interfaces) {

			ipif.setEngine(this);
			em.persist(ipif);
		}

		return this;
	}
	
	/**
	 * 	
	 *	 @param update
	 */
	
	public Engine update(EntityManager em, Engine update) {
		
		//	Why was this test necessary
		
		if (this == update) 
			throw new EngineUpdateException("Cannot update an engine with itself");

		httpdPort = update.getHttpdPort();
		localID = update.getlocalId();
		remoteIP = update.getRemoteIp();
		secret = update.getSecret();
		touchedAt = update.getTouchedAt();

		LOG.log(INFO, "UPDATING: " + this + " ("+this.getTouchedAt()+")");
		
		//	Update interface list - Step 1: remove interfaces from
		//	the existing engine that are not present in the PUT data
		
		for (int i=0; i<interfaces.size(); ) {
			
			IpInterface ipif = interfaces.get(i);

			if (update.getInterfaces().contains(ipif)) {

				i++;
			}
			else {
				
				if (LOG.isLoggable(DEBUG))
					LOG.log(DEBUG, "REMOVING: " + ipif.toString(false));
				
				em.remove(getInterfaces().get(i));
				getInterfaces().remove(i);
			}
		}

		//	Update interface list - Step 2: Add interfaces any interfaces
		//	in the PUT data that are not present in the existing engine.
		//	And update those which are present
		
		for (IpInterface ipif : update.getInterfaces()) {
			
        	ipif.setEngine(this);

        	if (interfaces.contains(ipif)) {
        		
				if (LOG.isLoggable(DEBUG))
					LOG.log(DEBUG, "UPDATING: " + ipif.toString(false));
				
				interfaces.remove(ipif);	//	equals only compares MAC
				interfaces.add(ipif);
				em.merge(ipif);
        		
        	} else {
        		
				if (LOG.isLoggable(DEBUG))
					LOG.log(DEBUG, "PERSISTING: " + ipif.toString(false));
        		
        		em.persist(ipif);
        		interfaces.add(ipif);
        	}
		}
		
		if (LOG.isLoggable(DEBUG))
			LOG.log(DEBUG, "existing.interfaces: " +interfaces.size());
		
		em.merge(this); // try to save
		return this;
	}
	
	/**
	 * 
	 * 	@param em
	 * 	@return
	 */
	
	public String delete(EntityManager em) {

		em.getTransaction().begin();
		em.remove(this);
		em.getTransaction().commit();
		
		return "Deleted Engine: " + id;
	}
	
	
	
	
	
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof Engine)) return false;
		
		Engine other = (Engine) object;

		if (id == null || other.id==null) return false;
		return id.equals(other.id);
	}

	/**
	 * 	Return a serializable equivalent for the engine - work-around for
	 * 	problem with Jackson annotations. Engines reference interfaces and
	 * 	interfaces reference engines - a circular structure we cannot 
	 * 	serialize. The Json equivalent breaks the circular structure.
	 * 
	 * 	@return Serializable equivalent of the instance.
	 */
	
//	public Engine forJson() {
//		
//		JsonEngine json = new JsonEngine();
//		
//		json.id = this.id;
//		json.localID = this.localID;
//		json.remoteIP = this.remoteIP;
//		json.secret = this.secret;
//		
//		for (IpInterface ipif : this.interfaces)
//			json.interfaces.add(ipif.forJson());
//		
//		return this;
//	}
	
	/**
	 * 	Human readable string representation of an interface and its
	 * 	interfaces.
	 */
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder("Engine[");
		
		sb.append("id: ").append(id);
		if (isPending()) sb.append(", pending");
		sb.append(", localID: ").append(localID);
		sb.append(", remoteIP: ").append(remoteIP);
		sb.append(", httpdPort: ").append(httpdPort);
		sb.append(", interfaces: [");

		for (Iterator<IpInterface> i = interfaces.iterator(); i.hasNext(); ) {
			IpInterface ipif = i.next();
			sb.append(ipif.toString(false)).append(i.hasNext()?", ":"");
		}
		
		sb.append("], secret: ").append(secret).append("]");
		
		return sb.toString();
	}
}
