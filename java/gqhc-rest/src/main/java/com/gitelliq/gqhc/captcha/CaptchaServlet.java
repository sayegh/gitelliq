package com.gitelliq.gqhc.captcha;

import static com.gitelliq.gqhc.Constants.DEBUG;
import static com.gitelliq.gqhc.Constants.INFO;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gitelliq.gqhc.Constants;
import com.gitelliq.gqhc.jpa.PersistenceUnit;
import com.github.cage.Cage;

public class CaptchaServlet extends HttpServlet {
	private static final long serialVersionUID = 1490947492185481844L;

	private static Logger LOG = Constants.Captcha;

	/*	Key for storing the servlet path in the application context. The servlet
	 * 	path is provided as an initParameter
	 */
	
	private static String PATH_KEY = CaptchaServlet.class.getName() + ".PATH";
	
	/*	Name of the initParameter containing the servlet path
	 */
	
	private static String PATH_PARAM = "mapped-path";

	CaptchaTokenSet tokenSet;

	/**
	 * 	Build a link for a captcha image. This static helper method receives the
	 * 	current request and the ctoken for the captcha image. The request headers
	 * 	are examined to determine if this is a forwarded (i.e. proxied) request
	 * 	and the servlet context-path selected accordingly. The servlet-path is
	 * 	stored in the application context when the servlet is initialized. <p/>
	 * 	   At the time of writing, the App uses the provided URL verbatim - so
	 * 	this needs to a URL. This is derived from the request URL.
	 * 	
	 * 
	 * 	@param request	The current request
	 * 	@param ctoken	The token for the image of the required URL
	 * 	@param asUrl	Indicates a URL (with protocol, host and port) is required
	 * 	@return	A link that will resolve to the captcha image for the given token
	 */
	
	public static String buildLink(HttpServletRequest request, String ctoken, boolean asUrl) {
	
		String link = request.getHeader("X-Forwarded-ContextPath");
		
		link = (link!=null) ? link: request.getServletContext().getContextPath();
		link += request.getServletContext().getAttribute(PATH_KEY);
		link += (link.endsWith("/")?"":"/") + ctoken;
		
		if (asUrl) {
			
			String url = request.getRequestURL().toString();

			return url.substring(0, url.indexOf(request.getContextPath())) + link;
		}
		
		return link;
	}
	
	@Override
	public void init() throws ServletException {
		
		super.init();
		
		try {
			
			getServletContext().setAttribute(PATH_KEY, getInitParameter(PATH_PARAM));
			tokenSet = CaptchaTokenSet.getInstance(true);
		} 	catch (NoSuchAlgorithmException e) {
			throw new ServletException(e);
		}
		LOG.log(INFO, "Initialised");
	}

//	private void addCaptchaToken(HttpServletResponse response, String text) {
//		
//		Cookie cookie = new Cookie("ctoken",tokenSet.add(text));
//		
////		cookie.setSecure(true);					//	This should be enabled (but not for testing)
//		cookie.setMaxAge(tokenSet.MAX_AGE); 	//	1 hour		response.
//		
//		response.addCookie(cookie);
//	}
//	
//	public static String getCaptchaToken(HttpServletRequest request) {
//		
//		Cookie[] cookies = request.getCookies();
//		
//		if (cookies!=null) 
//			for (Cookie cookie : request.getCookies()) 
//				if (cookie.getName().equals("ctoken"))
//					return cookie.getValue();
//				
//		return null;
//	}
	
	/**
	 * 	Generate a new captcha image and associated captcha-token (ctoken).
	 * 	The token is passed to the client as a cookie (the cookie expires
	 * 	quickly, e.g. after 5 minutes). 
	 */
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String token = null;
		String ctoken = req.getPathInfo();

		if (ctoken.startsWith("/"))
			ctoken = ctoken.substring(1, ctoken.length());
		
		LOG.log(DEBUG, "ctoken: " + ctoken);
		
		if (ctoken != null) token = tokenSet.getText(ctoken);
		if (token == null) token = tokenSet.getCage().getTokenGenerator().next();
		
		if (LOG.isLoggable(DEBUG))
			LOG.log(DEBUG,  "token: " + token);
		
//		addCaptchaToken(resp, token);		
		
		//	We have a once only approach now ... so can't be a problem
		
		setResponseHeaders(req, resp);
		tokenSet.getCage().draw(token, resp.getOutputStream());
	}

	/**
	 * Helper method, disables HTTP caching.
	 * 
	 * @param resp
	 *            response object to be modified
	 */
	protected void setResponseHeaders(HttpServletRequest req, HttpServletResponse resp) {
		resp.setContentType("image/" + tokenSet.getCage().getFormat());
		resp.setHeader("Cache-Control", "no-cache, no-store");
		resp.setHeader("Pragma", "no-cache");
		long time = System.currentTimeMillis();
		resp.setDateHeader("Last-Modified", time);
		resp.setDateHeader("Date", time);
		resp.setDateHeader("Expires", time);
		
//		resp.setHeader("Access-Control-Allow-Credentials", "true");
//		resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
//		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
//		resp.setHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept,Authorization,Accept-Language,Content-Language,Last-Event-ID,X-HTTP-Method-Override");

	}
}