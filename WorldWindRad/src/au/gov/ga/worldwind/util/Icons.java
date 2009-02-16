package au.gov.ga.worldwind.util;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Icons
{
	public static final ImageIcon remove;
	public static final ImageIcon search;
	public static final ImageIcon run;
	public static final ImageIcon delete;
	public static final ImageIcon edit;
	public static final ImageIcon add;
	public static final ImageIcon up;
	public static final ImageIcon down;
	public static final ImageIcon legend;
	public static final ImageIcon info;

	static
	{
		remove = loadIcon("/data/icons/remove.gif");
		search = loadIcon("/data/icons/search.gif");
		run = loadIcon("/data/icons/run.gif");
		delete = loadIcon("/data/icons/delete.gif");
		edit = loadIcon("/data/icons/edit.gif");
		add = loadIcon("/data/icons/add.gif");
		up = loadIcon("/data/icons/up.gif");
		down = loadIcon("/data/icons/down.gif");
		legend = loadIcon("/data/icons/legend.gif");
		info = loadIcon("/data/icons/information.gif");
	}

	private static ImageIcon loadIcon(String path)
	{
		try
		{
			return new ImageIcon(ImageIO.read(Icons.class
					.getResourceAsStream(path)));
		}
		catch (IOException e)
		{
			return null;
		}
	}
}
