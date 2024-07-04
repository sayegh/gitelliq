<%@page import="java.util.Iterator"%>
<%@page import="com.gitelliq.gqhc.registration.Engine"%>
<%@page import="com.gitelliq.gqhc.registration.IpInterface"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%><%@page
	import="com.gitelliq.gqhc.jpa.PersistenceUnit"%><%@ page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	EntityManager em = PersistenceUnit.getEntityManager(application);
	String id = request.getPathInfo();
	Engine engine = id==null ? null : em.find(Engine.class, id=id.substring(1));
	boolean delete = ("delete").equals(request.getParameter("action"));
	String error = null;
	String success = null;

	try {

		if (engine==null) {
			error = "Not found: " + id;
			response.setStatus(404);
		} 	else { 
			if (delete) success = engine.delete(em);
			else error = "Not supported";
		}
	} 	catch (Throwable _t) { 
		_t.printStackTrace(System.err); 
		error=_t.toString();
		response.setStatus(500);
	}	finally {
		if (em!=null) em.close();
	}
%>
<%= error == null ? success : error %>
