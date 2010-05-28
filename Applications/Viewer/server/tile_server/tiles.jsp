<%@ page import="java.io.*, java.net.URL" %>
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
	String X = request.getParameter("X");
	String Y = request.getParameter("Y");
	String L = request.getParameter("L");
	String T = request.getParameter("T");
	String mask = request.getParameter("mask");
	String path = "/web/html/test_root/docs/resources/images/world-wind";
	String ext = ".jpg";
	String content = "image/jpeg";

	if(X == null || Y == null || L == null || T == null)
		return;
	
	if(mask != null)
	{
		ext = ".png";
		content = "image/png";
	}
	
	if(T.startsWith("radio_"))
	{
		if(mask != null)
		{
			T = "radio_mask";
		}
		T = "radiometrics/" + T;
	}
	else if(T.startsWith("radioareas_"))
	{
		if(mask != null)
		{
			T = "radioareas_mask";
		}
		T = "radiometrics/" + T;
	}
	else
	{
		if(mask != null)
		{
			T += "/mask";
		}
		else
		{
			T += "/image";
		}
	}

	String url = path + "/" + T + "/" + L + "/" + paddedInt(Y, 4) + "/" + paddedInt(Y, 4) + "_" + paddedInt(X, 4) + ext;
	response.setContentType(content);
	File file = new File(url);
	if(!file.exists())
	{
		url = path + "/blank" + ext;
		file = new File(url);
	}

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
