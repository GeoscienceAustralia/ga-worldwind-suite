package au.gov.ga.worldwind.tiler.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class BufferedLineWriter extends BufferedWriter
{
	public BufferedLineWriter(Writer out)
	{
		super(out);
	}
	
	public void writeLine(String str) throws IOException
	{
		write(str);
		newLine();
	}
}
