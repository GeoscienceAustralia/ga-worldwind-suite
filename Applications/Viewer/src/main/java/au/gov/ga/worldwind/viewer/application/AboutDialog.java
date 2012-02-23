package au.gov.ga.worldwind.viewer.application;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.viewer.util.Message;

/**
 * About dialog for the GA WorldWind Data Viewer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AboutDialog extends JDialog
{
	public AboutDialog(Frame parent, String title)
	{
		super(parent, title, true);
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
			InputStream is = AboutDialog.class.getResourceAsStream("/images/about.jpg");
			image = ImageIO.read(is);
			is.close();
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

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		java.net.URL helpURL = this.getClass().getResource("/html/about.html");
		if (helpURL != null)
		{
			try
			{
				editorPane.setPage(helpURL);
			}
			catch (IOException e)
			{
				editorPane.setText(e.toString());
			}
		}
		else
		{
			editorPane.setText(Message.getMessage(Message.getHtmlNotFoundMessageKey()));
		}
		editorPane.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == EventType.ACTIVATED)
				{
					DefaultLauncher.openURL(e.getURL());
				}
				else if (e.getEventType() == EventType.ENTERED)
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				else if (e.getEventType() == EventType.EXITED)
				{
					setCursor(null);
				}
			}
		});
		add(editorPane, BorderLayout.CENTER);

		JPanel panel = new JPanel(new BorderLayout());
		int spacing = 10;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		add(panel, BorderLayout.PAGE_END);

		JButton button = new JButton(Message.getMessage(Message.getTermOkKey()));
		panel.add(button, BorderLayout.EAST);
		button.addActionListener(new ActionListener()
		{
			@Override
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
