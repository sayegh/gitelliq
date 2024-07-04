<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.gitelliq.gqhc.registration.Engine"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%><%@page
	import="com.gitelliq.gqhc.jpa.PersistenceUnit"%><%@ page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String title = "GITELLIQ Registration - Administration";
%>
<%@include file="/WEB-INF/jsp/head.jsp"%>
<%
	EntityManager em = null;
	String context = request.getHeader("X-Forwarded-ContextPath");
	String path = request.getPathInfo() == null ? "" : request.getPathInfo();
	Map<String, String> paths = new HashMap<String, String>();

	if (context==null) context = request.getServletContext().getContextPath();
	
	paths.put("admin/engines", "Engines");
	paths.put("admin/users", "Users");

	try {
%>
<body>

	<div class="container">

		<nav class="navbar navbar-inverse navbar-fixed-top">
			<div class="container" ng-controller="gqNavbarCtrlr as navbar">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle collapsed"
						data-toggle="collapse" data-target="#navbar" aria-expanded="false"
						aria-controls="navbar">
						<span class="sr-only">Toggle navigation</span> <span
							class="icon-bar"></span> <span class="icon-bar"></span> <span
							class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="<%=context%>/admin"><img
						src="/gq-central/img/cozima_inverse.png" /></a>
				</div>

				<div id="navbar" class="collapse navbar-collapse">
					<button class="btn btn-default navbar-btn navbar-right"
						type="button">EN</button>
					<ul class="nav navbar-nav navbar-right">
						<%
							for (Map.Entry<String, String> entry : paths.entrySet()) {
						%>
						<li class='<%=path.indexOf(entry.getKey()) == 1 ? "active" : ""%>'><a
							data-toggle="collapse" data-target="#navbar.in"
							href='<%=context + "/" + entry.getKey()%>'><%=entry.getValue()%></a></li>
						<%
							}
						%>
					</ul>
				</div>
			</div>
		</nav>

		<div class="row">
			<div id="alerts" class="col-md-12"></div>
		</div>

		<div id="admin" class="frosted">
			<%
				if (request.getPathInfo() == null) {
					} else if (request.getPathInfo().startsWith("/engines")) {
			%><%@include file="/WEB-INF/jsp/engines.jsp"%><%
				} else if (request.getPathInfo().startsWith("/users")) {
			%><%@include file="/WEB-INF/jsp/users.jsp"%><%
				}
			%>
		</div>

		<div id="footer" class="container">
			<p class="text-muted">
				<span>Copyright &copy; 2016 by <a
					href="https://thinkbox.berlin">THINKBOX BERLIN GmbH</a></span> <a
					href="https://thinkbox.berlin/legal/deutsch.html">Impressum,
					Haftungsausschluss und Datenschutz</a> <span style="width: 100%"></span>
			</p>
		</div>
	</div>
	<div id="scripts">
		<script src="/common-lib/bootstrap/dist/js/bootstrap.min.js"></script>
		<script>
			String.prototype.gqFormat = function() {
				var args = arguments;

				return this.replace(/\{(\d+)\}/g, function() {
					return args[arguments[1]];
				});
			};

			var alerts = $("#alerts");
			var adminAlert = (function() {

				var alert = "<div class='alert alert-{0} alert-dismissible' role='alert'>";
				alert += "<button type='button' class='close' data-dismiss='alert' aria-label='Close'>";
				alert += "<span aria-hidden='true'>&times;</span>";
				alert += "</button>";
				alert += "{1}</div>";

				return function(lvl, msg) {
					alerts.append(alert.gqFormat(lvl, msg));
				}
			})();
		</script>
		<script>
			(function() {
				
				var contextPath = "<%= context %>";
				
				function remove() {

					var button = $(this);
					var id = $(this).closest("[id]").attr("id");

					button.prop('disabled', true)

					function onError(data, b, c) {

						adminAlert("danger", data.responseText);
						button.prop("disabled", false);
					}

					function onSuccess(data) {

						adminAlert("success", data);
					}

					if (button.attr("name") === "delete-user")
						$.ajax({
							url : contextPath + "/admin/user/" + id + "?action=delete",
							success : onSuccess,
							error : onError
						});
					if (button.attr("name") === "delete-engine")
						$.ajax({
							url : contextPath + "/admin/engine/" + id + "?action=delete",
							success : onSuccess,
							error : onError
						});
				}

				$("#admin").on("click", "button[name='delete-user']", remove);
				$("#admin").on("click", "button[name='delete-engine']", remove);
			})();
		</script>
	</div>
</body>
<%
	} finally { if (em != null) em.close(); }
%>
</html>