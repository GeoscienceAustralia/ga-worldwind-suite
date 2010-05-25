package au.gov.ga.worldwind.panels.layers;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JScrollPane;

import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.theme.AbstractThemePanel;
import au.gov.ga.worldwind.theme.Theme;

public class ThemeLayersPanel extends AbstractThemePanel
{
	private LayerTree tree;
	private INode root;

	private LayerEnabler layerEnabler;

	public ThemeLayersPanel()
	{
		super(new BorderLayout());
		setDisplayName("Theme Layers");

		root = new FolderNode("root", null, true);
		layerEnabler = new LayerEnabler();
		tree = new LayerTree(root, layerEnabler);
		layerEnabler.setTree(tree);

		tree.setShowsRootHandles(false);
		tree.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(50, 50));
	}

	@Override
	public void setup(Theme theme)
	{
		layerEnabler.setWwd(theme.getWwd());

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
}
