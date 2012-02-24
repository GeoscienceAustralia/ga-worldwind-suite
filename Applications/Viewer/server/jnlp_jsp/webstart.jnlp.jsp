<%
	StringBuffer url = HttpUtils.getRequestURL(request);
	int indexOfLastSlash = url.lastIndexOf("/");
	String codebase = url.substring(0, indexOfLastSlash + 1);
	String document = url.substring(indexOfLastSlash + 1);
	
	response.setContentType("application/x-java-jnlp-file");
	String filename = document.substring(0, document.indexOf('.')) + ".jnlp";	
	response.addHeader("Content-disposition", "inline; filename=" + filename);
	
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
		<j2se href="http://java.sun.com/products/autodl/j2se" version="1.6+" initial-heap-size="1024m" max-heap-size="1024m" />
		<property name="sun.java2d.noddraw" value="true" />
		<jar href="viewer.jar" main="true" />
		<jar href="common.jar" />
		<jar href="resources.jar" />
		<jar href="worldwind.jar" />
		<jar href="worldwindx.jar" />
		<jar href="gdal.jar" />
		<jar href="jhlabs-filters.jar" />
		<!-- Java.net jogl jar certificate has expired. Using jars on NASA servers instead. -->
		<!-- <extension name="jogl" href="http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl.jnlp" /> -->
		<extension name="jogl" href="http://worldwind.arc.nasa.gov/java/jogl/webstart/jogl.jnlp"/>
	</resources>
	<resources os="Windows" arch="x86">
		<nativelib href="worldwind-natives-windows-i586.jar" />
	</resources>
	<resources os="Windows" arch="amd64">
		<nativelib href="worldwind-natives-windows-amd64.jar" />
	</resources>
	<resources os="Windows" arch="x86_64">
		<nativelib href="worldwind-natives-windows-amd64.jar" />
	</resources>
	<resources os="Linux" arch="i386">
		<nativelib href="worldwind-natives-linux-i586.jar" />
	</resources>
	<resources os="Linux" arch="x86">
		<nativelib href="worldwind-natives-linux-i586.jar" />
	</resources>
	<resources os="Linux" arch="amd64">
		<nativelib href="worldwind-natives-linux-amd64.jar" />
	</resources>
	<resources os="Linux" arch="x86_64">
		<nativelib href="worldwind-natives-linux-amd64.jar" />
	</resources>
	<resources os="Mac OS X" arch="i386">
		<nativelib href="worldwind-natives-macosx-universal.jar" />
	</resources>
	<resources os="Mac OS X" arch="x86_64">
		<nativelib href="worldwind-natives-macosx-universal.jar" />
	</resources>
	<application-desc main-class="au.gov.ga.worldwind.viewer.application.Application"><%
			if(theme != null)
			{
		%>
		<argument><%= theme %></argument><%
			}
			if(sandpit)
			{
		%>
		<argument>--url-regex</argument>
		<argument>http://www\.ga\.gov\.au/</argument>
		<argument>--url-replacement</argument>
		<argument>http://www.ga.gov.au:8500/</argument><%
			}
		%>
	</application-desc>
</jnlp>
