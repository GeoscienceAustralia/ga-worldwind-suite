package au.gov.ga.worldwind.tiler.ribbon.definition.aem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;
import au.gov.ga.worldwind.tiler.ribbon.definition.LayerDefinitionElementCreatorBase;
import au.gov.ga.worldwind.tiler.util.Util;

public abstract class AemElementCreatorBase extends LayerDefinitionElementCreatorBase
{

	protected FileReader getDataFileReader(RibbonTilingContext context)
	{
		try
		{
			return new FileReader(new File(context.getSourceLocation(), Util.stripExtension(context.getSourceFile().getName()) + ".txt"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	

}
