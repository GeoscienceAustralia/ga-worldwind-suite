package util;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.plaf.basic.BasicHTML;

import layers.ga.GALayer;

public class HtmlViewer extends JDialog
{
	public HtmlViewer(Frame owner, String title, String resource)
	{
		this(owner, title, false, resource, false);
	}

	public HtmlViewer(Frame owner, String title, boolean modal,
			String resource, boolean showOk)
	{
		super(owner, title, modal);
		URL url = null, base = null;
		try
		{
			url = this.getClass().getResource(resource);
		}
		catch (Exception e)
		{
		}
		try
		{
			base = this.getClass().getResource(
					resource.substring(0, resource.lastIndexOf('/')));
		}
		catch (Exception e)
		{
		}
		init(url, base, showOk);
	}

	public HtmlViewer(Frame owner, String title, URL page, URL base)
	{
		this(owner, title, false, page, base, false);
	}

	public HtmlViewer(Frame owner, String title, boolean modal, URL page,
			URL base)
	{
		this(owner, title, modal, page, base, false);
	}

	public HtmlViewer(Frame owner, String title, boolean modal, URL page,
			URL base, boolean showOk)
	{
		super(owner, title, modal);
		init(page, base, showOk);
	}

	private void init(URL page, URL base, boolean showOk)
	{
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);

		if (page != null)
		{
			try
			{
				editorPane.setPage(page);
			}
			catch (IOException e)
			{
				editorPane.setText(e.toString());
			}
		}
		else
		{
			editorPane.setText("Could not find page");
		}

		if (base != null)
		{
			editorPane.putClientProperty(BasicHTML.documentBaseKey, base);
		}

		editorPane.addHyperlinkListener(new HyperlinkListener()
		{
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

		JScrollPane scrollPane = new JScrollPane(editorPane);
		add(scrollPane, BorderLayout.CENTER);

		Action disposeAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};

		JRootPane rootPane = getRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		InputMap inputMap = rootPane
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", disposeAction);

		if (showOk)
		{
			JPanel panel = new JPanel(new GridBagLayout());
			add(panel, BorderLayout.PAGE_END);
			GridBagConstraints c;

			JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			panel.add(separator, c);

			JButton ok = new JButton("OK");
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(10, 10, 10, 10);
			panel.add(ok, c);
			ok.addActionListener(disposeAction);

			ok.setDefaultCapable(true);
			rootPane.setDefaultButton(ok);
		}
	}

	public static void main(String[] args)
	{
		URL page = null, base = null;
		try
		{
			base = new URL(GALayer.getMetadataBaseUrl() + "radiometrics/");
			page = new URL(base, "areas_legend.html");
		}
		catch (MalformedURLException e)
		{
		}
		HtmlViewer dialog = new HtmlViewer(null, "Radiometrics", page, base);
		dialog.setSize(600, 440);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}
