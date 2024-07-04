<%@page import="java.util.Iterator"%>
<%@page import="com.gitelliq.gqhc.registration.Engine"%>
<%@page import="com.gitelliq.gqhc.registration.IpInterface"%>
<%@page import="com.gitelliq.gqhc.registration.MqttUser"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%><%@page
	import="com.gitelliq.gqhc.jpa.PersistenceUnit"%><%@ page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	EntityManager em = PersistenceUnit.getEntityManager(application);

	try {
		
		String id = request.getPathInfo();
		MqttUser user = id==null ? null : MqttUser.findByEngineId(em, id=id.substring(1));
		boolean delete = ("delete").equals(request.getParameter("action"));
		String error = null;
		String success = null;

		if (user==null) {
			error = "Not found: " + id;
			response.setStatus(404);
		} 	else if (delete) try { 
			success = user.delete(em);
		} 	catch (Throwable _t) { error=_t.toString(); }
		
		if (error!=null || success!=null) {
	%>
	<%= error == null ? success : error %>
	<%
		} else {
	%>

	<tr>
		<td>MQTT User</td>
		<td><%=(user == null) ? "" : user.getMqttUser()%></td>
	</tr>
	<tr>
		<td>eMail</td>
		<td><%=(user == null) ? "" : user.getMailAddress()%></td>
	</tr>
	<tr>
		<td/>
		<td>
			<button class="btn btn-default btn-sm" name="delete-engine"
				type="submit">Delete Engine</button>
			<button class="btn btn-default btn-sm" name="delete-user"
				type="submit">Delete User</button>
		</td>
	</tr>
	<% 
		}
	} 	finally {
		if (em!=null) em.close();
	}
%>