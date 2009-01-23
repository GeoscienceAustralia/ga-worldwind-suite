package application;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.BrowserLauncher;

public class AboutDialog extends JDialog
{
	public AboutDialog(Frame parent)
	{
		super(parent, "About", true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}
		});

		BufferedImage image = null;
		try
		{
			InputStream is = AboutDialog.class
					.getResourceAsStream("/data/help/about.jpg");
			image = ImageIO.read(is);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		setLayout(new BorderLayout());

		ImageCanvas canvas = new ImageCanvas(image);
		Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
		canvas.setMinimumSize(imageSize);
		canvas.setPreferredSize(imageSize);
		canvas.setMaximumSize(imageSize);
		add(canvas, BorderLayout.WEST);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.white);
		add(panel, BorderLayout.CENTER);

		JTextArea text = new JTextArea();
		panel.add(text);
		text
				.setText("Geoscience Australia - World Wind\nVersion "
						+ Application.VERSION
						+ "\n\nBased on NASA World Wind\n\nLicensed under the NASA Open Source Agreement\n");
		text.setFont(Font.decode(""));
		
		final String gaURL = "http://www.ga.gov.au/";
		text = new JTextArea();
		panel.add(text);
		text.setText(gaURL);
		text.setFont(Font.decode(""));
		text.setForeground(Color.blue);
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				BrowserLauncher.openURL(gaURL);
			}
		});
		
		final String nasaURL = "http://worldwind.arc.nasa.gov/credits.html";
		text = new JTextArea();
		panel.add(text);
		text.setText(nasaURL);
		text.setFont(Font.decode(""));
		text.setForeground(Color.blue);
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				BrowserLauncher.openURL(nasaURL);
			}
		});
		
		panel = new JPanel(new BorderLayout());
		int spacing = 10;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEtchedBorder(), BorderFactory.createEmptyBorder(spacing,
				spacing, spacing, spacing)));
		add(panel, BorderLayout.PAGE_END);

		JButton button = new JButton("OK");
		panel.add(button, BorderLayout.EAST);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private static class ImageCanvas extends Canvas
	{
		private BufferedImage image;

		public ImageCanvas(BufferedImage image)
		{
			this.image = image;
		}

		@Override
		public void paint(Graphics g)
		{
			g.drawImage(image, 0, 0, null);
		}
	}
}
