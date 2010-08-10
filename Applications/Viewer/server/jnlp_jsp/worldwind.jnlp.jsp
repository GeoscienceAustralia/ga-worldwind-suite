<%@ page
	contentType="application/x-java-jnlp-file"
	info="World Wind Viewer JNLP"
%>

<%
	StringBuffer url = HttpUtils.getRequestURL(request);
	int indexOfLastSlash = url.lastIndexOf("/");
	String codebase = url.substring(0, indexOfLastSlash + 1);
	String document = url.substring(indexOfLastSlash + 1);
%>

<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0" codebase="<%= codebase %>" href="<%= document %>">
	<information>
		<title>NASA World Wind Java</title>
		<vendor>NASA</vendor>
		<homepage href="http://worldwind.arc.nasa.gov/java" />
		<description>NASA World Wind Java</description>
		<description kind="short">NASA World Wind Planetary Visualization Software Library</description>
		<offline-allowed />
	</information>
	<security>
		<all-permissions />
	</security>
	<resources>
		<property name="sun.java2d.noddraw" value="true" />
		<jar href="worldwind.jar" />
		<extension name="jogl" href="http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl.jnlp" />
	</resources>
	<component-desc />
</jnlp>
