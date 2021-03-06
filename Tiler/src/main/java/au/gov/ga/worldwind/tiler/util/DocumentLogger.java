/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.tiler.util;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * {@link Logger} subclass that supports logging to a {@link StyledDocument},
 * using different {@link Color}s for the different logging {@link Level}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = document.addStyle(Level.ALL.getName(), def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = document.addStyle(Level.SEVERE.getName(), regular);
		StyleConstants.setForeground(s, Color.red);

		s = document.addStyle(Level.WARNING.getName(), regular);
		StyleConstants.setForeground(s, Color.orange);

		s = document.addStyle(Level.INFO.getName(), regular);
		StyleConstants.setForeground(s, new Color(0, 128, 0));

		s = document.addStyle(Level.FINE.getName(), regular);
		StyleConstants.setForeground(s, Color.gray);

		s = document.addStyle(Level.FINER.getName(), regular);
		StyleConstants.setForeground(s, Color.lightGray);

		s = document.addStyle(Level.FINEST.getName(), regular);
		StyleConstants.setForeground(s, Color.lightGray);
	}

	@Override
	public void log(LogRecord record)
	{
		String level = record.getLevel().getName();
		String msg = record.getMessage();
		String line = msg == null ? "Unknown" : msg;
		DateFormat df = new SimpleDateFormat("[HH:mm:ss]");
		String prefix = df.format(new Date()) + " - " + level + " - ";
		String suffix = System.getProperty("line.separator");
		String text = prefix + line + suffix;
		if (record.getThrown() != null)
			text += ": " + record.getThrown();

		Style style = document.getStyle(level);
		if (style == null)
			style = document.getStyle(Level.ALL.getName());
		try
		{
			document.insertString(document.getLength(), text, style);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
