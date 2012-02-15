package au.gov.ga.worldwind.viewer.panels.layers;

import javax.swing.Icon;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemeLayer;

/**
 * Special layers panel that displays a flat list of layers that are hard-coded
 * in a theme.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ThemeLayersPanel extends AbstractLayersPanel
{
	//TODO probably needs refactoring to support layer hierarchies, currently a bit useless
	
	public ThemeLayersPanel()
	{
		super();
		setDisplayName("Theme Layers");
		if (tree != null)
		{
			tree.setShowsRootHandles(false);
		}
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

		if (tree != null)
		{
			tree.setShowsRootHandles(false);
		}
		for (ThemeLayer layer : theme.getLayers())
		{
			if (layer.isVisible())
			{
				tree.getLayerModel().addLayer(layer, (Object[]) null);
			}
			else
			{
				tree.getLayerModel().addInvisibleLayer(layer);
			}
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
