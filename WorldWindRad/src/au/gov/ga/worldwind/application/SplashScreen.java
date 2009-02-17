package au.gov.ga.worldwind.application;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JDialog;

public class SplashScreen extends JDialog
{
	private BufferedImage image;

	public SplashScreen()
	{
		InputStream is = SplashScreen.class
				.getResourceAsStream("/images/400x230-splash-nww.png");
		if (is != null)
		{
			try
			{
				image = ImageIO.read(is);
			}
			catch (IOException e)
			{
			}
		}
		if (image != null)
		{
			setUndecorated(true);
			setResizable(false);
			setSize(image.getWidth(), image.getHeight());
			setLocationRelativeTo(null);
			setVisible(true);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		}
	}

	@Override
	public void paint(Graphics g)
	{
		g.drawImage(image, 0, 0, null);
	}
}
