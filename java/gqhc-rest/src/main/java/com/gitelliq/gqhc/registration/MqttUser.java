package com.gitelliq.gqhc.registration;

import static com.gitelliq.gqhc.Constants.INFO;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import com.gitelliq.gqhc.Constants;


@Entity
public class MqttUser implements Serializable {

	private static final Logger LOG = Constants.MqttUser;

	@OneToOne()
	@Id()
	Engine engine;
	
	@Column()
	String mailAddress;
	
	@Column()
	String mqttUser;

	@Column()
	String mqttHash;
	
	@Transient()
	String mqttPassword;
	
	@Transient()
	boolean force = false;
	
	
	public Engine getEngine() {
		return engine;
	}
	
	public void setEngine(Engine e) {
		engine = e;
	}
	
	public String getMailAddress() {
		return mailAddress;
	}
	
	public void setMailAddress(String addr) {
		mailAddress = addr;
	}
	
	
	public String getMqttUser() {
		return mqttUser;
	}
	
	public void setMqttUser(String user) {
		mqttUser = user;
	}
	
	
	public String getMqttHash() {
		return mqttHash;
	}
	
	public void setMqttHash(String hash) {
		mqttHash = hash;
	}
	
	public String getMqttPassword() {
		return mqttPassword;
	}
	
	public void setMqttPassword(String p) {
		mqttPassword = p;
	}
	
	public boolean getForce() {
		return force;
	}
	
	public void setForce(boolean b) {
		force = b;
	}
	
	public static MqttUser findByEngineId(EntityManager em, String id) {
		
		String qstring = "SELECT c FROM MqttUser c WHERE c.engine.id = :id";
		TypedQuery<MqttUser> query = em.createQuery(qstring, MqttUser.class);
		
		query = query.setParameter("id", id);
		List<MqttUser> users = query.getResultList();
		
		return users.size() == 1 ? users.get(0) : null;
	}
	
	public static List<MqttUser> findAll(EntityManager em, int pageSize, int pageNum) {
		
		String qstring = "SELECT c FROM MqttUser c";
		TypedQuery<MqttUser> query = em.createQuery(qstring, MqttUser.class);
		
		return query.getResultList();
	}
	
	public String delete(EntityManager em) {

		LOG.log(INFO, "DELETING: for " + this.getEngine().getId());

		em.getTransaction().begin();
		em.remove(this);
		em.getTransaction().commit();

		return "Deleted MqttUser: " + getEngine().getId();
	}
	
	public JsonRegistration forJSON() {
		JsonRegistration result = new JsonRegistration();
		result.setMqttUser(mqttUser);
		result.setMqttPassword(mqttPassword);
		return result;
	}
	
	
}
