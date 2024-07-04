<%@page import="java.util.Iterator"%>
<%@page import="com.gitelliq.gqhc.registration.Engine"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%><%@page
	import="com.gitelliq.gqhc.jpa.PersistenceUnit"%><%@ page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%
		em = PersistenceUnit.getEntityManager(application);
		String rip = request.getParameter("rip");
		List<Engine> engines;
		
		if (rip==null) engines = Engine.findAll(em, 0, 0);
		else engines = Engine.findByRemoteIp(em, rip, 0);
		
		if (rip!=null) {
			
			%>
<div class="row">
	<div class="col-md-12">
		<div class="alert alert-info">
			Filtering by Remote-IP: <%= rip %>
		</div>
	</div>
</div>
			<%
		}
		for (int i = 0; i < 3; i++) {
	%>
<div id="admin-engines" class="row">
	<div class="col-md-4">
		<%
			for (int e = i; e < engines.size(); e += 3) {
				Engine engine = engines.get(e);
				%><%@include file="/WEB-INF/jsp/engine-panel.jsp"%><%
			}
		%>
	</div>
	<%
		}
	%>
</div>
<script>
(function() {
		
	var contextPath = "<%= context %>";

	function getMqttUser() {
		
		var button = $(this);
		var id = button.closest("[id]").attr("id");
		var tbody = button.closest("tbody");

		function onError(response, status, text) {
			
			if (response.status==404)  button.parent().html("None");
			else  adminAlert("danger", response.status + " : " + text);
			button.prop('disabled', false)
		}
		
		button.prop('disabled', true)
		$.ajax({url: contextPath + "/admin/user/"+id, error: onError, success: function(data) { tbody.html(data) } });
	}
	
	$("#admin-engines").on("click", "button[name='get-user']", getMqttUser );
})();
</script>




