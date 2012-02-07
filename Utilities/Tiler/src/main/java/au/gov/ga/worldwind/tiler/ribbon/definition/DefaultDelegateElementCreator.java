package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultDelegateElementCreator extends LayerDefinitionElementCreatorBase 
{

	@Override
	public String getElementName()
	{
		return "Delegates";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<Delegates>");
		
		for (String delegate : context.getDelegateStrings())
		{
			appendLine(result, level+1, "<Delegate>" + delegate + "</Delegate>");
		}
		appendLine(result, level+1, "<Delegate>LocalRequester</Delegate>");
		appendLine(result, level+1, "<Delegate>ResizeTransformer(512,512)</Delegate>");
		
		appendLine(result, level, "</Delegates>");
		
		return result.toString();
	}

}
