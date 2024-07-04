package com.gitelliq.gqhc.registration;

/**
 * 	The <code>Challenge</code> class is simply a container allowing
 * 	us to serialize a challenge response from the server. Consists
 * 	of the challenge code and an optional detail message.
 * 
 * 	@author sme
 *
 */

public class Challenge {

	private String challenge;
	private String detail;
	private String url;
	private String ctoken;
	
	public Challenge() {}
	
	public Challenge(String challenge) {
		
		this.challenge = challenge;
	}
	
	public Challenge(String challenge, String detail) {
		
		this.challenge = challenge;
		this.detail = detail;
	}

	public String getChallenge() { return challenge; }
	
	public void setChallenge(String challenge) { this.challenge = challenge; }
	
	public String getDetail() { return detail; }
	
	public void setDetail(String detail) { this.detail = detail; }
	
	public String getUrl() { return url; }

	public void setUrl(String url) { this.url = url; }

	public String getCtoken() { return ctoken; }

	public void setCtoken(String ctoken) { this.ctoken = ctoken; }

	public String toString() {
		
		return "Challenge["+challenge+"]";
	}
}
