<%@page import="java.util.Iterator"%>
<%@page import="com.gitelliq.gqhc.registration.Engine"%>
<%@page import="com.gitelliq.gqhc.registration.IpInterface"%>
<%@page import="com.gitelliq.gqhc.registration.MqttUser"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%><%@page
	import="com.gitelliq.gqhc.jpa.PersistenceUnit"%><%@ page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%-- <% MqttUser user = null; %> --%>
<div class="panel panel-info">
	<div class="panel-heading">
		<div class="panel-title">
			<a href="#<%=user.getEngine().getId()%>" data-toggle="collapse"><small><%=user.getMailAddress()%></small></a>
		</div>
	</div>
	<div id="<%=user.getEngine().getId()%>" class="panel-body collapse">
		<table class="table">
			<tbody>
				<tr>
					<th>Engine:</th>
					<td><%=user.getEngine().getId()%></td>
				</tr>
				<tr>
					<th>Username:</th>
					<td><%=user.getMqttUser()%></td>
				</tr>
				<tr>
					<th>Remote IP:</th>
					<td><%=user.getEngine().getRemoteIp()%></td>
				</tr>
				<tr>
					<th>Verified:</th>
					<td><%=user.getEngine().getVerifiedAt()%></td>
				</tr>
				<tr>
					<th>Touched:</th>
					<td><%=user.getEngine().getTouchedAt()%></td>
				</tr>
				<tr>
					<td/>
					<td>
						<button class="btn btn-default btn-sm" name="delete-engine" type="submit">Delete Engine</button>
						<button class="btn btn-default btn-sm" name="delete-user" type="submit">Delete User</button>
					</td>
				</tr>
			</tbody>
		</table>
		<p class="text-left"></p>
	</div>
</div>



