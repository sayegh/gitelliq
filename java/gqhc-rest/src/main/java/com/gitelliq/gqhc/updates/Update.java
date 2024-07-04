package com.gitelliq.gqhc.updates;

public class Update {

	private String url = null;
	private String hash = null;
	private String filename = null;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String f) {
		this.filename = f;
	}
}
