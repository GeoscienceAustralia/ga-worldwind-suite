package util;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Icons
{
	public static final ImageIcon remove;
	public static final ImageIcon key;
	public static final ImageIcon add;
	static
	{
		String base = "/data/icons/";
		remove = loadIcon(base + "remove.gif");
		add = loadIcon(base + "add.gif");
		key = loadIcon(base + "key.gif");
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
