package tiler;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class DocumentLogger extends Logger
{
	private StyledDocument document;

	public DocumentLogger(String name, StyledDocument document)
	{
		super(name, null);
		this.document = document;
		setupLogStyles();
	}

	private void setupLogStyles()
	{
		Style def = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);

		Style regular = document.addStyle("REGULAR", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = document.addStyle("SEVERE", regular);
		StyleConstants.setForeground(s, Color.red);

		s = document.addStyle("WARNING", regular);
		StyleConstants.setForeground(s, Color.orange);

		s = document.addStyle("INFO", regular);
		StyleConstants.setForeground(s, new Color(0, 128, 0));

		s = document.addStyle("FINE", regular);
		StyleConstants.setForeground(s, Color.gray);

		s = document.addStyle("FINER", regular);
		StyleConstants.setForeground(s, Color.lightGray);

		s = document.addStyle("FINEST", regular);
		StyleConstants.setForeground(s, Color.lightGray);
	}

	private void addLogLine(String msg, String type)
	{
		String line = msg == null ? "Unknown" : msg;
		DateFormat df = new SimpleDateFormat("[HH:mm:ss]");
		String prefix = df.format(new Date()) + " - " + type + " - ";
		String suffix = System.getProperty("line.separator");
		String text = prefix + line + suffix;

		Style style = document.getStyle(type);
		if (style == null)
			style = document.getStyle("REGULAR");
		try
		{
			document.insertString(document.getLength(), text, style);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void warning(String msg)
	{
		addLogLine(msg, "WARNING");
	}

	@Override
	public void severe(String msg)
	{
		addLogLine(msg, "SEVERE");
	}

	@Override
	public void info(String msg)
	{
		addLogLine(msg, "INFO");
	}

	@Override
	public void fine(String msg)
	{
		addLogLine(msg, "FINE");
	}

	@Override
	public void finer(String msg)
	{
		addLogLine(msg, "FINER");
	}

	@Override
	public void finest(String msg)
	{
		addLogLine(msg, "FINEST");
	}
}
