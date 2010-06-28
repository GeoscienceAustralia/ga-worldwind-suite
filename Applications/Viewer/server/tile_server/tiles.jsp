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
	//get the parameters from the request
	String X = request.getParameter("X");
	String Y = request.getParameter("Y");
	String L = request.getParameter("L");
	String T = request.getParameter("T");
	String F = request.getParameter("F");
	boolean mask = request.getParameter("mask") != null;
	String path = "/web/html/test_root/docs/resources/images/world-wind/tiles";
	boolean returnBlankOnError = false;
	
	//if the essential parameters are not defined, just return
	if(X == null || Y == null || L == null || T == null)
	{
		return;
	}
	
	//default to JPG tiles
	String ext = ".jpg";
	String content = "image/jpeg";

	//if the mask is requested, the default tile type changes to PNG
	if(mask)
	{
		ext = ".png";
		content = "image/png";
	}
	
	//if the F (format) parameter was passed, set the extension and content type accordingly
	if(F != null)
	{
		F = F.toLowerCase();
		if(F.contains("png"))
		{
			ext = ".png";
			content = "image/png";
		}
		else if(F.contains("jpg") || F.contains("jpeg"))
		{
			ext = ".jpg";
			content = "image/jpeg";
		}
		else if(F.contains("dds"))
		{
			ext = ".dds";
			content = "image/dds";
		}
	}
	
	//if the mask is requested, change the last directory to 'mask'
	if(mask)
	{
		int indexOfLastSlash = T.lastIndexOf('/');
		if(indexOfLastSlash >= 0)
		{
			T = T.substring(0, indexOfLastSlash + 1);
		}
		else
		{
			T = "";
		}
		T += "mask";
	}

	String filepath = path + "/" + T + "/" + L + "/" + paddedInt(Y, 4) + "/" + paddedInt(Y, 4) + "_" + paddedInt(X, 4) + ext;
	File file = new File(filepath);
	
	if(!file.exists() && returnBlankOnError)
	{
		filepath = path + "/blank" + ext;
		file = new File(filepath);
	}
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
