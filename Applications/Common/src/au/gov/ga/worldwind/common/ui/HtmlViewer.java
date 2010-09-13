package au.gov.ga.worldwind.common.ui;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicHTML;

import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.message.CommonMessageConstants;

public class HtmlViewer extends JDialog
{
	public HtmlViewer(Frame owner, String title, String resource)
	{
		this(owner, title, false, resource, false);
	}

	public HtmlViewer(Frame owner, String title, boolean modal, String resource, boolean showOk)
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
			base = this.getClass().getResource(resource.substring(0, resource.lastIndexOf('/')));
		}
		catch (Exception e)
		{
		}
		init(url, base, showOk, title);
	}

	public HtmlViewer(Frame owner, String title, URL page, URL base)
	{
		this(owner, title, false, page, base, false);
	}

	public HtmlViewer(Frame owner, String title, boolean modal, URL page, URL base)
	{
		this(owner, title, modal, page, base, false);
	}

	public HtmlViewer(Frame owner, String title, boolean modal, URL page, URL base, boolean showOk)
	{
		super(owner, title, modal);
		init(page, base, showOk, title);
	}

	private void init(URL page, URL base, boolean showOk, final String title)
	{
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);

		if (page != null)
		{
			setTitle(title + " - " + getMessage(CommonMessageConstants.getHtmlLoadingTitleKey()));
			editorPane.addPropertyChangeListener(new PropertyChangeListener()
			{
				@Override
				public void propertyChange(PropertyChangeEvent evt)
				{
					if (evt.getPropertyName().equals("page"))
					{
						setTitle(title);
					}
				}
			});

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
			editorPane.setText(getMessage(CommonMessageConstants.getHtmlNotFoundMessageKey()));
		}

		if (base != null)
		{
			editorPane.putClientProperty(BasicHTML.documentBaseKey, base);
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

		JScrollPane scrollPane = new JScrollPane(editorPane);
		add(scrollPane, BorderLayout.CENTER);

		Action disposeAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};

		JRootPane rootPane = getRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
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

			JButton ok = new JButton(getMessage(CommonMessageConstants.getTermOkKey()));
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
}
