<%
	StringBuffer url = HttpUtils.getRequestURL(request);
	int indexOfLastSlash = url.lastIndexOf("/");
	String codebase = url.substring(0, indexOfLastSlash + 1);
	String document = url.substring(indexOfLastSlash + 1);
	
	response.setContentType("application/x-java-jnlp-file");
	String filename = document.substring(0, document.indexOf('.')) + ".jnlp";	
	response.addHeader("Content-disposition", "inline; filename=" + filename);
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
		<!-- Oracle packaged jogl's not signed correctly. Reverting to world wind signed jars - JN 20110919
		<extension name="jogl" href="http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl.jnlp" />
		-->
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
	<component-desc />
</jnlp>
