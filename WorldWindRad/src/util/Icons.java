package util;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Icons
{
	public static final ImageIcon remove;
	//public static final ImageIcon delete;
	public static final ImageIcon search;

	static
	{
			remove = loadIcon("/data/icons/remove.gif");
			//delete = loadIcon("/data/icons/delete.gif");
			search = loadIcon("/data/icons/search.gif");
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
