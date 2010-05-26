package au.gov.ga.worldwind.panels.layers;

import java.awt.BorderLayout;

import javax.swing.Icon;

import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.util.Icons;

public class ThemeLayersPanel extends AbstractLayersPanel
{
	public ThemeLayersPanel()
	{
		super(new BorderLayout());
		setDisplayName("Theme Layers");
	}

	@Override
	public Icon getIcon()
	{
		return Icons.list.getIcon();
	}

	@Override
	public void setup(Theme theme)
	{
		super.setup(theme);

		for (ILayerDefinition layer : theme.getLayers())
		{
			tree.getModel().addLayer(layer, null);
		}

		((ClearableBasicTreeUI) tree.getUI()).relayout();
		tree.repaint();
	}

	@Override
	public void dispose()
	{
	}

	@Override
	protected INode createRootNode()
	{
		return new FolderNode("root", null, true);
	}
}
