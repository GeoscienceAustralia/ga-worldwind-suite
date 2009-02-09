package util;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
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
import javax.swing.plaf.basic.BasicHTML;

public class HtmlViewer extends JDialog
{
	public HtmlViewer(Frame owner, String title, String page)
	{
		this(owner, title, page, false);
	}

	public HtmlViewer(Frame owner, String title, String page, boolean showOk)
	{
		super(owner, title);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		
		URL url = null, base = null;
		try
		{
			url = this.getClass().getResource(page);
		}
		catch (Exception e)
		{
		}
		if (url != null)
		{
			try
			{
				editorPane.setPage(url);
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
		
		try
		{
			base = this.getClass().getResource(
					page.substring(0, page.lastIndexOf('/')));
		}
		catch (Exception e)
		{
		}
		if (base != null)
		{
			editorPane.putClientProperty(BasicHTML.documentBaseKey, base);
		}

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

		if(showOk)
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
}
