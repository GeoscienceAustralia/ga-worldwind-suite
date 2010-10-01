package au.gov.ga.worldwind.viewer.panels.layers;

import javax.swing.Icon;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemeLayer;

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
				tree.getLayerModel().addLayer(layer, (Object[]) null);
			else
				tree.getLayerModel().addInvisibleLayer(layer);
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
