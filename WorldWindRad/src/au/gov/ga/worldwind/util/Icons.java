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
		String base = "/au/gov/ga/worldwind/data/icons/";
		remove = loadIcon(base + "remove.gif");
		search = loadIcon(base + "search.gif");
		run = loadIcon(base + "run.gif");
		delete = loadIcon(base + "delete.gif");
		edit = loadIcon(base + "edit.gif");
		add = loadIcon(base + "add.gif");
		up = loadIcon(base + "up.gif");
		down = loadIcon(base + "down.gif");
		legend = loadIcon(base + "legend.gif");
		info = loadIcon(base + "information.gif");
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
