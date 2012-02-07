package au.gov.ga.worldwind.tiler.ribbon.definition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

/**
 * A {@link LayerDefinitionCreator} that creates a <code>&lt;path&gt;</code> element by
 * copying the contents of a file with the name <code>[tileset name].path</code>
 */
public class PathFilePathElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "Path";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<Path>");
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(getPathFile(context)));
			String line;
			while ((line = reader.readLine()) != null)
			{
				appendLine(result, level+1, line);
			}
		}
		catch (Exception e)
		{
			// Do nothing. In the case of an exception we will just print the empty <path> element
		}
		appendLine(result, level, "</Path>");
		return result.toString();
	}
	
	private File getPathFile(RibbonTilingContext context)
	{
		File pathFile = new File(context.getSourceLocation(), context.getTilesetName() + ".path");
		return pathFile;
	}

}
