package au.gov.ga.worldwind.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;

public class Icons
{
	private static final String ICON_DIRECTORY = "/au/gov/ga/worldwind/data/icons/";

	public static final ImageIcon remove;
	public static final ImageIcon search;
	public static final ImageIcon run;
	public static final ImageIcon stop;
	public static final ImageIcon delete;
	public static final ImageIcon edit;
	public static final ImageIcon add;
	public static final ImageIcon up;
	public static final ImageIcon down;
	public static final ImageIcon legend;
	public static final ImageIcon info;
	public static final ImageIcon error;

	static
	{
		remove = loadIcon(ICON_DIRECTORY + "remove.gif");
		search = loadIcon(ICON_DIRECTORY + "search.gif");
		run = loadIcon(ICON_DIRECTORY + "run.gif");
		stop = loadIcon(ICON_DIRECTORY + "stop.gif");
		delete = loadIcon(ICON_DIRECTORY + "delete.gif");
		edit = loadIcon(ICON_DIRECTORY + "edit.gif");
		add = loadIcon(ICON_DIRECTORY + "add.gif");
		up = loadIcon(ICON_DIRECTORY + "up.gif");
		down = loadIcon(ICON_DIRECTORY + "down.gif");
		legend = loadIcon(ICON_DIRECTORY + "legend.gif");
		info = loadIcon(ICON_DIRECTORY + "information.gif");
		error = loadIcon(ICON_DIRECTORY + "error.gif");
	}

	private static ImageIcon loadIcon(String path)
	{
		try
		{
			InputStream is = Icons.class.getResourceAsStream(path);
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

	public static ImageIcon newLoadingIcon()
	{
		return loadIcon(ICON_DIRECTORY + "loading.gif");
	}
}
