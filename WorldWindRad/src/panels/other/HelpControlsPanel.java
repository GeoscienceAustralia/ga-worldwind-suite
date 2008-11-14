package panels.other;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HelpControlsPanel extends JPanel
{
	public HelpControlsPanel()
	{
		super(new BorderLayout());

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		java.net.URL helpURL = this.getClass().getResource(
				"/data/help/controls");
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
			editorPane.setText("Could not find HTML page");
		}

		JScrollPane scrollPane = new JScrollPane(editorPane);
		add(scrollPane, BorderLayout.CENTER);
	}
}
