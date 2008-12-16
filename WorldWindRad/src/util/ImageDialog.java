package util;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JDialog;

public class ImageDialog extends JDialog
{
	private final static int BORDER = 5;

	public ImageDialog(Frame frame, String title, boolean modal,
			final Image image)
	{
		super(frame, title, modal);

		setLayout(new BorderLayout());
		Canvas canvas = new Canvas()
		{
			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
				int x = BORDER, y = BORDER, width = getWidth() - BORDER * 2, height = getHeight()
						- BORDER * 2;
				if (width < 0 || height < 0)
					return;
				int imageWidth = image.getWidth(null), imageHeight = image
						.getHeight(null);
				float canvasAspect = (float) width / (float) height;
				float imageAspect = (float) imageWidth / (float) imageHeight;

				if (canvasAspect > imageAspect)
				{
					int newWidth = (int) (height * imageAspect);
					x = (width - newWidth) / 2 + BORDER;
					width = newWidth;
				}
				else if (canvasAspect < imageAspect)
				{
					int newHeight = (int) (width / imageAspect);
					y = (height - newHeight) / 2 + BORDER;
					height = newHeight;
				}

				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
						RenderingHints.VALUE_DITHER_ENABLE);
				g.drawImage(image, x, y, width, height, null);
			}
		};
		add(canvas, BorderLayout.CENTER);
		canvas.setBackground(Color.white);
	}
}
