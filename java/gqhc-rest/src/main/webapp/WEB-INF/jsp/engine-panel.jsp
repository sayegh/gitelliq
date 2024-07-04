<%@page import="java.util.Iterator"%>
<%@page import="com.gitelliq.gqhc.registration.Engine"%>
<%@page import="com.gitelliq.gqhc.registration.IpInterface"%>
<%@page import="com.gitelliq.gqhc.registration.MqttUser"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%><%@page
	import="com.gitelliq.gqhc.jpa.PersistenceUnit"%><%@ page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<div class="panel panel-info">
	<div class="panel-heading" data-target="#<%=engine.getId()%>" data-toggle="collapse">
		<%=engine.getId()%>
		<div class="text-right" style="border-top: solid 1px; font-size: 60%">
			<%=engine.getTouchedAt()%>
		</div>		
	</div>
	<div id="<%=engine.getId()%>" class="panel-body collapse">
		<table class="table">
			<tbody>
				<tr>
					<th>Local ID:</th>
					<td><%=engine.getlocalId()%></td>
				</tr>
				<tr>
					<th>Remote IP:</th>
					<td><a href='?rip=<%=engine.getRemoteIp()%>'><%=engine.getRemoteIp()%></a></td>
				</tr>
				<tr>
					<th>Verified:</th>
					<td><%=engine.getVerifiedAt()%></td>
				</tr>
				<tr>
					<th>Touched:</th>
					<td><%=engine.getTouchedAt()%></td>
				</tr>
				<%
					for (IpInterface ipif : engine.getInterfaces()) {
				%>
				<tr>
					<td><%=ipif.getIp()%></td>
					<td><%=ipif.getMac()%></td>
				</tr>
				<%
					}
				%>
                                <tr>
                                        <th>Port:</th>
                                        <td><%=engine.getHttpdPort()%></td>
                                </tr>
			</tbody>
			<tbody>
				<tr>
					<td>MQTT User</td>
					<td>
						<button class="btn btn-default btn-sm" name="get-user" type="submit">Fetch</button>
					</td>
					<td></td>
				</tr>
				<tr>
					<td/>
					<td>
						<button class="btn btn-default btn-sm" name="delete-engine" type="submit">Delete Engine</button>
					</td>
				</tr>
			</tbody>
		</table>
		<p class="text-left"></p>
	</div>
</div>



