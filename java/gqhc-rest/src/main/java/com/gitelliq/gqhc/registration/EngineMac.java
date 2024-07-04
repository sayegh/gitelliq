package com.gitelliq.gqhc.registration;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 	<code>EngineMac</code> serves as the unique key for <code>IpInterface</code> 
 * 	(hence <em>embeddable</em>). In principle, this means the unique key is composed
 * 	of the engine-id and the mac-address (while very tempting, we are not relying on
 * 	the uniqueness of mac-addresses).<p/>
 * 
 * 	The engine-id is in turn the unique key of an <code>Engine</code>, and so
 * 	that we can make the required joins, the class must be constructed
 * 	accordingly. Hence the key consists of:
 * 
 * 	<ul>
 * 	<li><code>engine</code> : <code>Engine</code></li>
 * 	<li><code>mac</code> : <code>String</code></li>
 * 	</ul>
 * 
 * 	@author sme
 */

/*	JSON Annotations not available (or not working).
 * 	
 * 	When serializing the interfaces associated with the engine, these should
 * 	not in turn serialize the engine associated with the interface. In theory
 * 	the following annotation does this, but ... the targeted annotation has
 * 	just as little effect.
 */

// @JsonIgnoreProperties({ "engine" })
@Embeddable
public class EngineMac implements Serializable {

	/*	Thanks eclipse */
	
	private static final long serialVersionUID = 1435414631226179121L;

	/**
	 * 	A mac address - the usual format.
	 */
	
	String mac = null;
	
	/**
	 * 	An engine. An engine can have multiple interfaces, hence
	 * 	declared as many-to-one.
	 */
	
	@ManyToOne()
	@JsonIgnore
	Engine engine;
			
	public Engine getEngine() { return engine; }

	public void setEngine(Engine engine) { this.engine = engine; }

	public String getEngineId() { return engine.id; }

	public void setEngineId(String engineId) { this.engine.id = engineId; }

	public String getMac() { return mac; }

	public void setMac(String mac) { this.mac = mac; }
	
	private StringBuilder toStringBuilder() {
				
		return new StringBuilder(mac).append(engine!=null?engine.id:"null");
	}
	
	public String toString() {
		
		return toStringBuilder().insert(0, "EngineMac[").append("]").toString();
	}

	@Override
	public int hashCode() {
		
		return toStringBuilder().toString().hashCode();
	}

	@Override
	public boolean equals(Object object) {

		if (!(object instanceof EngineMac)) return false;

		EngineMac other = (EngineMac) object;
		
		return (toString().equals(other.toString()));
	}
	
	
}
