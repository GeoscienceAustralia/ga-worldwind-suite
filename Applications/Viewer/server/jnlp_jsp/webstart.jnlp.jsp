<%
	StringBuffer url = HttpUtils.getRequestURL(request);
	int indexOfLastSlash = url.lastIndexOf("/");
	String codebase = url.substring(0, indexOfLastSlash + 1);
	String document = url.substring(indexOfLastSlash + 1);
	
	response.setContentType("application/x-java-jnlp-file");
	String filename = document.substring(0, document.indexOf('.')) + ".jnlp";	
	response.addHeader("Content-Disposition", "Inline; fileName=" + filename);
	
	boolean sandpit = request.getServerPort() == 8500;
	String query = request.getQueryString();
	String theme = null;
	
	if(query != null && query.length() > 0)
	{
		boolean absolute = query.toLowerCase().startsWith("http:") || query.toLowerCase().startsWith("https:");
		theme = absolute ? query : codebase + query;
		document += "?" + query;
	}
%>

<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0" codebase="<%= codebase %>" href="<%= document %>">
	<information>
		<title>World Wind Viewer</title>
		<vendor>Geoscience Australia</vendor>
		<homepage href="http://www.ga.gov.au" />
		<description></description>
		<description kind="short"></description>
		<icon href="32x32-icon-earth.png" />
		<icon kind="splash" href="400x230-splash-nww.jpg" />
		<offline-allowed />
	</information>
	<security>
		<all-permissions />
	</security>
	<resources>
		<j2se href="http://java.sun.com/products/autodl/j2se" version="1.6+" initial-heap-size="512m" max-heap-size="512m" />
		<property name="sun.java2d.noddraw" value="true" />
		<jar href="application.jar" main="true" />
		<extension name="worldwind" href="<%= codebase %>worldwind.jnlp.jsp" />
	</resources>
	<application-desc main-class="au.gov.ga.worldwind.application.Application"><%
			if(theme != null)
			{
		%>
		<argument><%= theme %></argument><%
			}
			if(sandpit)
			{
		%>
		<argument>-sandpit</argument><%
			}
		%>
	</application-desc>
</jnlp>
