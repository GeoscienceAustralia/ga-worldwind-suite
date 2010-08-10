<%@ page import="java.io.*,java.net.URL"%>
<%!
	String paddedInt(String value, int charcount)
	{
		while (value.length() < charcount)
		{
			value = "0" + value;
		}
		return value;
	}
%>
<%
	//absolute path of the tiles directory on the web server
	String path = "/web/html/test_root/docs/resources/images/world-wind/tiles";
	
	//get the parameters from the request
	String X = request.getParameter("X");
	String Y = request.getParameter("Y");
	String L = request.getParameter("L");
	String T = request.getParameter("T");
	
	//if the essential parameters are not defined, just return
	if(X == null || Y == null || L == null || T == null)
	{
		return;
	}
	
	String ext = ".zip";
	String content = "application/zip";

	String filepath = path + "/" + T + "/" + L + "/" + paddedInt(Y, 4) + "/" + paddedInt(Y, 4) + "_" + paddedInt(X, 4) + ext;
	File file = new File(filepath);
	
	if(!file.exists())
	{
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return;
	}

	response.setContentType(content);
	InputStream is = new FileInputStream(file);
	ServletOutputStream os = response.getOutputStream();
	byte[] buffer = new byte[1024];
	int size;

	while((size = is.read(buffer)) >= 0)
	{
		os.write(buffer, 0, size);
	}

	is.close();
	os.close();
%>
