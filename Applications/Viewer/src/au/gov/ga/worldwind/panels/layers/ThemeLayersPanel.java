package au.gov.ga.worldwind.panels.layers;

import javax.swing.Icon;

import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemeLayer;
import au.gov.ga.worldwind.util.Icons;

public class ThemeLayersPanel extends AbstractLayersPanel
{
	public ThemeLayersPanel()
	{
		super();
		setDisplayName("Theme Layers");
		tree.setShowsRootHandles(false);
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

		for (ThemeLayer layer : theme.getLayers())
		{
			if (layer.isVisible())
				tree.getModel().addLayer(layer, (Object[]) null);
			else
				tree.getModel().addInvisibleLayer(layer);
		}

		tree.getUI().relayout();
		tree.repaint();
	}

	@Override
	public void dispose()
	{
	}

	@Override
	protected INode createRootNode(Theme theme)
	{
		return new FolderNode(null, null, null, true);
	}
}
