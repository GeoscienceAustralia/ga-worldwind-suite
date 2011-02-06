package au.gov.ga.worldwind.tiler.ribbon.definition;

public abstract class LayerDefinitionElementCreatorBase implements LayerDefinitionElementCreator
{

	protected void appendLine(StringBuffer buffer, int tabIndents, String line)
	{
		for (int i = 0; i < tabIndents; i++)
		{
			buffer.append('\t');
		}
		buffer.append(line).append('\n');
	}
	
}
