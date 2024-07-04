package com.gitelliq.gqhc.captcha;
import static com.gitelliq.gqhc.Constants.DEBUG;
import static com.gitelliq.gqhc.Constants.WARN;
import static com.gitelliq.gqhc.Constants.INFO;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.gitelliq.gqhc.Constants;
import com.github.cage.Cage;

/**
 * 	Provides an abstraction of client verification for the REST service.
 * 		
 * 
 * 	@author sme
 *
 */


public class CaptchaTokenSet {

    private static Logger LOG = Constants.TokenSet;

    public final int MAX_AGE = (5 * 60);		//	5 minutes

	private final long TIMEOUT = 1000 * MAX_AGE;
	
	private final Cage cage = new CustomCage();

	private static CaptchaTokenSet instance = null;
	
	public static CaptchaTokenSet getInstance() {

		return instance;
	}
	
	/**
	 * 
	 * 	TODO:	Access via application context
	 * 
	 * 	@param init
	 * 	@return
	 * 	@throws NoSuchAlgorithmException
	 */
	
	public static CaptchaTokenSet getInstance(boolean init)
		throws NoSuchAlgorithmException
	{
		if (instance != null) return instance;
		else return instance = new CaptchaTokenSet();
	}
	
	/*
	 * 	The CaptchaToken collects all the Captcha-information:
	 *   
	 *     - The token
	 *     - The text
	 *     - Whether the token has been rendered (i.e. requested per HTTP)
	 *     - Whether it has been verified (correct text submitted per HTTP)
	 *     - timestamp - when created and verified
	 */
	
	private class CaptchaToken {
		
		long timestamp = 0;
		String text;
		String ctoken;
		boolean verified = false;
		boolean rendered = false;
		
		CaptchaToken(String text) { 
			
			this.text = text; 
			this.timestamp = System.currentTimeMillis();
		}
	}
	
	/*	The TokenSet maintains a set of tokens. 
	 */
	
	Map<String, CaptchaToken> tokens = new HashMap<String, CaptchaToken>();
	
	/*	Random number generator
	 */
	
	private SecureRandom rng;

	/*	Create a token set. The token set is a singleton associated
	 * 	with the application context, hence no public constructor.
	 */
	
	private CaptchaTokenSet() throws NoSuchAlgorithmException {

		rng = SecureRandom.getInstance("SHA1PRNG");
	}
	
	/*	Traverse the token set and removing old entries (determined
	 * 	by the timeout property). 
	 */
	
	private void removeExpired() {
		
		List<String> expired = new ArrayList<String>();
		long expireTime = System.currentTimeMillis() - TIMEOUT;
		
		for (CaptchaToken token : tokens.values())
			if (token.timestamp < expireTime)
				expired.add(token.ctoken);

		for (String ctoken : expired)  tokens.remove(ctoken);
	}
	
	/**
	 * 	Add a CaptchaToken for the given Captcha-Text to the set. This generates
	 * 	a random token that is used as a cookie value. This effectively will set
	 * 	the lifetime of the this CaptchaToken.<p/>
	 * 	We do not use sessions (unlike the examples) - they aren't needed
	 * 	and we can do without the overhead. In the long-term though we will
	 * 	need these tokens to be load-balancer capable. So we would need
	 * 	to add ".S1" or ".S2" to the tokens, for the config below to work.
	 * 	(edited example from Apache).
	 * 
	 * <pre>
	 *	<Proxy "balancer://mycluster">
     *		BalancerMember "http://192.168.1.50:80" route=S1
     *		BalancerMember "http://192.168.1.51:80" route=S2
     *		ProxySet stickysession=ctoken
	 *	</Proxy>
	 *	ProxyPass "/test" "balancer://mycluster"
	 *	ProxyPassReverse "/test" "balancer://mycluster"
	 * </pre>
	 * 
	 * 
	 * 	@param text
	 * 	@return
	 */
	
	
	public synchronized String add(String text) {

		removeExpired();
		
		CaptchaToken token = new CaptchaToken(text);

		do {

			byte bytes[] = new byte[16];
			rng.nextBytes(bytes);

//			byte hash[] = DigestUtils.sha256(bytes);
			byte hash[] = DigestUtils.sha(bytes);

			token.ctoken = Base64.encodeBase64URLSafeString(hash);
			
		} while (tokens.containsKey(token.ctoken));
		
	 	tokens.put(token.ctoken, token);
	 	
		return token.ctoken;
	}
	
	public String next() {
		
		return add(cage.getTokenGenerator().next());
	}
	
	public String getText(String ctoken) {
		
		if (!tokens.containsKey(ctoken)) return null;
		
		CaptchaToken token = tokens.get(ctoken);
		
		if (token.rendered) return null;
		token.rendered = true;
		
		return token.text;
	}
	
	public Cage getCage() { return cage; } 
	
	public synchronized boolean verified(String ctoken, String text) {
		
		LOG.log(DEBUG, "verified("+ctoken+","+text+")");
		
		if (ctoken == null) return false;
		
		removeExpired();

		CaptchaToken token = tokens.get(ctoken);

		if (token == null) return false;
		if (token.verified) return true;
		
		do {
			
			if (!token.rendered) break;
			if (text == null) break;
			if (!token.text.equals(text)) break;
			
			token.verified = true;
			token.timestamp = System.currentTimeMillis();
			
			return true;
			
		}	while (false);
		
		return false;
	}
}
