package au.gov.ga.worldwind.tiler.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * {@link BufferedWriter} subclass that adds functionality for writing lines
 * within one function call.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BufferedLineWriter extends BufferedWriter
{
	public BufferedLineWriter(Writer out)
	{
		super(out);
	}

	/**
	 * Write the given string, and then a newline.
	 * 
	 * @param str
	 *            String to write
	 * @throws IOException
	 */
	public void writeLine(String str) throws IOException
	{
		write(str);
		newLine();
	}
}
