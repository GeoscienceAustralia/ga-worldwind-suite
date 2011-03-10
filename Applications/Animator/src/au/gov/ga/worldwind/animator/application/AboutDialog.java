package au.gov.ga.worldwind.animator.application;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.*;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;

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
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import au.gov.ga.worldwind.common.util.DefaultLauncher;

public class AboutDialog extends JDialog
{
	private static final long serialVersionUID = 20110310L;

	private static final String BASE_LOCATION = "/au/gov/ga/worldwind/animator/data/help/";
	
	private final LicenceDialog licenceDialog;
	
	private final HyperlinkListener hyperlinkListener = new HyperlinkListener()
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
	};
	
	public AboutDialog(Frame parent, String title)
	{
		super(parent, title, true);
		licenceDialog = new LicenceDialog(parent);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}
		});

		setLayout(new BorderLayout());
		
		// Load the image canvas
		ImageCanvas canvas = new ImageCanvas(loadImage());
		add(canvas, BorderLayout.WEST);

		// Load the about content
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		java.net.URL aboutURL = this.getClass().getResource(BASE_LOCATION + "about.html");
		if (aboutURL != null)
		{
			try
			{
				editorPane.setPage(aboutURL);
			}
			catch (IOException e)
			{
				editorPane.setText(e.toString());
			}
		}
		else
		{
			editorPane.setText(getMessage(getHtmlNotFoundMessageKey()));
		}
		editorPane.addHyperlinkListener(hyperlinkListener);
		add(editorPane, BorderLayout.CENTER);

		// Add the button panel
		JPanel panel = new JPanel(new BorderLayout());
		int spacing = 10;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		add(panel, BorderLayout.PAGE_END);

		JButton okButton = new JButton(getMessage(getTermOkKey()));
		panel.add(okButton, BorderLayout.EAST);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		
		JButton licenceButton = new JButton(getMessage(getShowLicenceLabelKey()));
		panel.add(licenceButton, BorderLayout.WEST);
		licenceButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				licenceDialog.setVisible(true);
			}
		});

		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private BufferedImage loadImage()
	{
		BufferedImage image = null;
		try
		{
			InputStream is = AboutDialog.class.getResourceAsStream(BASE_LOCATION + "about.jpg");
			image = ImageIO.read(is);
			is.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return image;
	}

	private static class ImageCanvas extends Canvas
	{
		private static final long serialVersionUID = 20110310L;
		
		private BufferedImage image;

		public ImageCanvas(BufferedImage image)
		{
			this.image = image;
			Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
			setMinimumSize(imageSize);
			setPreferredSize(imageSize);
			setMaximumSize(imageSize);
		}

		@Override
		public void paint(Graphics g)
		{
			g.drawImage(image, 0, 0, null);
		}
	}
	
	private class LicenceDialog extends JDialog
	{
		private static final long serialVersionUID = 20110310L;

		public LicenceDialog(Frame parent)
		{
			super(parent, getMessage(getLicenceDialogTitleKey()), true);
			
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					dispose();
				}
			});
			
			Dimension size = new Dimension(600, 400);
			setMaximumSize(size);
			setPreferredSize(size);
			setLayout(new BorderLayout());
			
			// Load the about content
			JEditorPane editorPane = new JEditorPane();
			editorPane.setEditable(false);
			java.net.URL aboutURL = this.getClass().getResource(BASE_LOCATION + "eula.html");
			if (aboutURL != null)
			{
				try
				{
					editorPane.setPage(aboutURL);
				}
				catch (IOException e)
				{
					editorPane.setText(e.toString());
				}
			}
			else
			{
				editorPane.setText(getMessage(getHtmlNotFoundMessageKey()));
			}
			editorPane.addHyperlinkListener(hyperlinkListener);
			JScrollPane scrollPane = new JScrollPane(editorPane);
			add(scrollPane, BorderLayout.CENTER);
			
			// Add the button panel
			JPanel panel = new JPanel(new BorderLayout());
			int spacing = 10;
			panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
			add(panel, BorderLayout.PAGE_END);

			JButton okButton = new JButton(getMessage(getTermOkKey()));
			panel.add(okButton, BorderLayout.EAST);
			okButton.addActionListener(new ActionListener()
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
		}
		
	}
}
