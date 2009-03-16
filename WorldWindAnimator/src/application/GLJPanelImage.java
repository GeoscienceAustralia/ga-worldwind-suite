package application;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.media.opengl.GLJPanel;

public class GLJPanelImage extends GLJPanel
{
	@Override
	protected void paintComponent(Graphics g)
	{
		GraphicsProxy gp = new GraphicsProxy(g)
		{
			@Override
			public boolean drawImage(Image img, int x, int y, int width,
					int height, ImageObserver observer)
			{
				System.out.println(img);
				return super.drawImage(img, x, y, width, height, observer);
			}
		};
		super.paintComponent(gp);
	}
}
