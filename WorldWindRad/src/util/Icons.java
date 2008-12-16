package util;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Icons
{
	public static final ImageIcon remove;
	public static final ImageIcon search;
	public static final ImageIcon maximize;
	public static final ImageIcon close;
	public static final ImageIcon restore;
	public static final ImageIcon run;
	public static final ImageIcon monitor;
	public static final ImageIcon home;
	public static final ImageIcon delete;
	public static final ImageIcon edit;
	public static final ImageIcon add;
	public static final ImageIcon up;
	public static final ImageIcon down;

	static
	{
		remove = loadIcon("/data/icons/remove.gif");
		search = loadIcon("/data/icons/search.gif");
		maximize = loadIcon("/data/icons/maximize.gif");
		close = loadIcon("/data/icons/close.gif");
		restore = loadIcon("/data/icons/restore.gif");
		run = loadIcon("/data/icons/run.gif");
		monitor = loadIcon("/data/icons/monitor.gif");
		home = loadIcon("/data/icons/home.gif");
		delete = loadIcon("/data/icons/delete.gif");
		edit = loadIcon("/data/icons/edit.gif");
		add = loadIcon("/data/icons/add.gif");
		up = loadIcon("/data/icons/up.gif");
		down = loadIcon("/data/icons/down.gif");
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
