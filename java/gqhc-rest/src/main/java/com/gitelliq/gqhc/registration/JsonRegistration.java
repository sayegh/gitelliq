package com.gitelliq.gqhc.registration;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

public class JsonRegistration {

	String mqttUser;
	String mqttPassword;
	
	public String getMqttUser() {
		return mqttUser;
	}
	
	public void setMqttUser(String user) {
		mqttUser = user;
	}
	
	
	public String getMqttPassword() {
		return mqttPassword;
	}
	
	public void setMqttPassword(String p) {
		mqttPassword = p;
	}
}
