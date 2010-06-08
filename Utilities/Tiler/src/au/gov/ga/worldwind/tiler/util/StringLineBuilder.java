package au.gov.ga.worldwind.tiler.util;

public class StringLineBuilder
{
	private StringBuilder s;
	private String newLine;

	public StringLineBuilder()
	{
		s = new StringBuilder();
		newLine = System.getProperty("line.separator");
	}

	public void appendLine(String str)
	{
		s.append(str + newLine);
	}

	@Override
	public String toString()
	{
		return toString(false);
	}

	public String toString(boolean removeLastLine)
	{
		String str = s.toString();
		if (removeLastLine && str.endsWith(newLine))
			return str.substring(0, str.length() - newLine.length());
		return str;
	}
}
