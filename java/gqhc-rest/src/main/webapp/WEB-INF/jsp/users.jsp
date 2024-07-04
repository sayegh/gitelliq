<%@page import="java.util.Iterator"%>
<%@page import="com.gitelliq.gqhc.registration.MqttUser"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%><%@page
	import="com.gitelliq.gqhc.jpa.PersistenceUnit"%><%@ page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<div id="admin-users" class="row">
	<%
		em = PersistenceUnit.getEntityManager(application);
		List<MqttUser> users = MqttUser.findAll(em, 0, 0);
		
		for (int i=0; i<3; i++) {
	%>
	<div class="col-md-4">
		<%
			for (int u=i; u<users.size(); u+=3) {
				
				MqttUser user = users.get(u);
				%>
				<%@include file="/WEB-INF/jsp/user-panel.jsp"%>
				<%
			}
		%>
	</div>
	<% 
		}
	%>
</div>
