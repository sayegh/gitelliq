package com.gitelliq.gqhc.registration;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * 	Represents an Ip-Interface on the Gitelliq host-system.<p/>
 * 
 * 	This class can be persisted because it is declared in:
 * 
 * 	<pre>src/main/resources/persistence.xml</pre>
 * 
 *  Annotations define the details of the persistence.
 * 
 * 	@author sme
 */

@Entity
public class IpInterface {
	
	/**
	 * 	The unique key for the interface is the engine (i.e. the engine-id)
	 * 	and the mac-address. This is modelled by the <em>embeddable</em>
	 * 	(i.e. not stored in another table) <code>EngineMac</code>. 	
	 */
	
    @EmbeddedId
	EngineMac engineMac = new EngineMac();
	    
	/**
	 * 	The IP-address of the interface.
	 */
	
	@Column()
	String ip = null;
		
	/**
	 * 	Create an <code>IpInterface</code> instance.
	 */
	
	public IpInterface() {};
	
	/**
	 * 	Create an <code>IpInterface</code> instance - initializing the MAC 
	 * 	and IP	
	 * 	addresses as given. The URL for local connections to Gitelliq
	 * 	is also initialised.
	 * 
	 * 	@param mac IP-Interface MAC-address
	 * 	@param ip IP-Interface IP-address
	 */
	
	public IpInterface(String mac, String ip) {
		
		this.ip = ip;
		this.engineMac.mac = mac;
	}

	@JsonIgnore
	public EngineMac getEngineMac()  { return engineMac; }

	public void setEngineMac(EngineMac engineMac) { this.engineMac = engineMac; }
	
	public String getIp() { return ip; }
	
	/**
	 * 	Set the IP-address of the interface and update the url
	 * 	accordingly.	
	 * 
	 * 	@param value
	 */
	
	public void setIp(String value) {  ip = value; }
	
	@JsonIgnore
	public Engine getEngine() { return engineMac.engine; };
	
	public void setEngine(Engine value) { engineMac.engine = value; };
	
	public String getMac() { return engineMac.mac; }
	
	public void setMac(String value) { engineMac.mac = value; }

	public String getUrl() { return "http://" + this.ip + ":"+engineMac.getEngine().getHttpdPort()+"/dispatch/client"; }
	
	/**
	 * 	To simplify merge and update options on the interface-lists
	 * 	we implement the <code>hash</code> and <code>equals</code> methods.
	 * 
	 * 	Note:	Slight abuse here; we consider them to be equal, if the
	 * 			MAC-addresses are the same - simplifies the update code
	 */
	
	@Override
	public int hashCode() {
		
		return engineMac.toString().hashCode();
	}

	/**
	 * 	To simplify merge and update options on the interface-lists
	 * 	we implement the <code>hash</code> and <code>equals</code> methods.
	 * 
	 * 	Note:	Slight abuse here; we consider them to be equal, if the
	 * 			MAC-addresses are the same - simplifies the update code
	 */
	
	@Override
	public boolean equals(Object obj) {
		
		boolean result = false;
		
		if (obj instanceof IpInterface) {

			IpInterface other = (IpInterface) obj;
			
			result =  this.engineMac.mac.equals(other.engineMac.mac);
			
//			result = other.engineMac.engine.id .equals(engineMac.engine.id) && other.engineMac.mac.equals(engineMac.mac);
			
			System.err.println(this.toString(false) + "==" + other.toString(false) + " => " + result);
		}
		else System.err.println(this.toString(false) + "==" + obj.toString() + " => " + result);
		
		
		return result;
	}

	/**
	 * 	Return a serializable equivalent for the interface - work-around for
	 * 	problem with Jackson annotations. Interfaces reference the engine and
	 * 	the engine references the interface - a circular structure we cannot 
	 * 	serialize. The Json equivalent breaks the circular structure.
	 * 
	 * 	@return Serializable equivalent of the instance.
	 */
	
//	public JsonIpInterface forJson() {
//		
//		JsonIpInterface json = new JsonIpInterface();
//		
//		json.ip = this.ip;
//		json.url = ("http://" + this.ip + ":8888/dispatch/client");
//		json.mac = this.getMac();
//		
//		return json;
//	}

	/**
	 * 	Generate a readable representation of the interface, including
	 * 	the engine if required.
	 * 
	 * @param recurse Include engine in representation
	 * @return Readable representation of the interface
	 */
		
	public String toString(boolean recurse) { 
		
		StringBuilder sb = new StringBuilder("IpInterface[");
		
		sb.append(engineMac.mac).append(",").append(ip).append("]");
		
		return sb.toString();
	}

	/**
	 * 	Generate a human-readable representation of the imterface.
	 */
	
	public String toString() { 
				
		StringBuilder sb = new StringBuilder("IpInterface[");
		
		sb.append(engineMac.engine).append(":").append(":").append(ip).append("]");
		
		return sb.toString();
	}
}
