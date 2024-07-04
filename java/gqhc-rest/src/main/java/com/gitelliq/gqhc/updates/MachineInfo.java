package com.gitelliq.gqhc.updates;

public class MachineInfo {
	String system = null;
	String release = null;
	String version = null;
	String machine = null;
	String machineID = null;
	String gqHash = null;
	String gqRelease = null;

	Plugin[] plugins = {};
	
	public Plugin[] getPlugins() {
		return plugins;
	}
	public void setPlugins(Plugin[] plugins) {
		this.plugins = plugins;
	}
	
	public String getSystem() {
		return system;
	}
	public void setSystem(String s) {
		system = s;
	}
	
	public String getRelease() {
		return release;
	}
	public void setRelease(String s) {
		release = s;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String s) {
		version = s;
	}
	
	public String getMachine() {
		return machine;
	}
	public void setMachine(String s) {
		machine = s;
	}
	
	public String getMachineID() {
		return machineID;
	}
	public void setMachineID(String s) {
		machineID = s;
	}
	
	public String getGqHash() {
		return gqHash;
	}
	public void setGqHash(String s) {
		gqHash = s;
	}
	
	public String getGqRelease() {
		return gqRelease;
	}
	public void setGqRelease(String gqRelease) {
		this.gqRelease = gqRelease;
	}
}
