package au.gov.ga.worldwind.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

public enum Icons
{
	remove("remove.gif"),
	search("search.gif"),
	run("run.gif"),
	stop("stop.gif"),
	delete("delete.gif"),
	edit("edit.gif"),
	add("add.gif"),
	up("up.gif"),
	down("down.gif"),
	legend("legend.gif"),
	info("information.gif"),
	error("error.gif"),
	newfolder("newfolder.gif"),
	folder("folder.gif"),
	earth("earth.png");

	private static final String ICON_DIRECTORY = "/au/gov/ga/worldwind/data/icons/";
	private ImageIcon icon;
	private URL url;

	Icons(String filename)
	{
		url = createURL(filename);
	}

	public ImageIcon getIcon()
	{
		if (icon == null)
		{
			icon = loadIcon(url);
		}
		return icon;
	}

	public URL getURL()
	{
		return url;
	}

	public static ImageIcon newLoadingIcon()
	{
		return loadIcon(createURL("loading.gif"));
	}

	private static URL createURL(String filename)
	{
		return Icons.class.getResource(ICON_DIRECTORY + filename);
	}

	private static ImageIcon loadIcon(URL url)
	{
		try
		{
			InputStream is = url.openStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) >= 0)
			{
				baos.write(buffer, 0, read);
			}
			return new ImageIcon(baos.toByteArray());
		}
		catch (IOException e)
		{
			return null;
		}
	}
}
