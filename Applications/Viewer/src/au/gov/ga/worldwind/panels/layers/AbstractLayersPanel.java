package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.WorldWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.panels.layers.LayerEnabler.RefreshListener;
import au.gov.ga.worldwind.theme.AbstractThemePanel;
import au.gov.ga.worldwind.theme.Theme;

public abstract class AbstractLayersPanel extends AbstractThemePanel
{
	protected WorldWindow wwd;

	protected LayerTree tree;
	protected INode root;

	protected LayerEnabler layerEnabler;
	protected JSlider opacitySlider;
	private boolean ignoreSliderChange = false;

	public AbstractLayersPanel(LayoutManager layout)
	{
		super(layout);

		root = createRootNode();
		layerEnabler = new LayerEnabler();
		tree = new LayerTree(root, layerEnabler);
		layerEnabler.setTree(tree);

		tree.setShowsRootHandles(false);
		tree.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(MINIMUM_LIST_HEIGHT, MINIMUM_LIST_HEIGHT));

		createActions();
		createToolBar();

		layerEnabler.addRefreshListener(new RefreshListener()
		{
			@Override
			public void refreshed()
			{
				enableSlider();
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				enableSlider();
			}
		});

		enableSlider();
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();
		layerEnabler.setWwd(theme.getWwd());
	}

	protected abstract void createActions();

	protected abstract INode createRootNode();

	private void createToolBar()
	{
		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.setFloatable(false);
		add(toolBar, BorderLayout.PAGE_START);

		setupToolBarBeforeSlider(toolBar);

		createOpacitySlider();
		toolBar.add(opacitySlider);

		setupToolBarAfterSlider(toolBar);
	}

	private void createOpacitySlider()
	{
		opacitySlider = new JSlider(0, 100, 100);
		opacitySlider.setToolTipText("Layer opacity");
		Dimension size = opacitySlider.getPreferredSize();
		size.width = 50;
		opacitySlider.setPreferredSize(size);
		opacitySlider.setMinimumSize(size);
		opacitySlider.setMaximumSize(size);

		opacitySlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (!ignoreSliderChange)
					setSelectedOpacity();
			}
		});
	}

	private void enableSlider()
	{
		TreePath selected = tree.getSelectionPath();
		ILayerNode node = getLayer(selected);
		if (node != null && layerEnabler.hasLayer(node))
		{
			double opacity = node.isEnabled() ? node.getOpacity() * 100d : 0;
			ignoreSliderChange = true;
			opacitySlider.setValue((int) Math.round(opacity));
			ignoreSliderChange = false;
			opacitySlider.setEnabled(true);
		}
		else
		{
			opacitySlider.setEnabled(false);
		}
	}

	private void setSelectedOpacity()
	{
		TreePath[] selected = tree.getSelectionPaths();
		ILayerNode[] nodes = new ILayerNode[selected.length];
		for (int i = 0; i < selected.length; i++)
		{
			nodes[i] = getLayer(selected[i]);
			if (nodes[i] == null)
				return;
		}

		for (int i = 0; i < nodes.length; i++)
		{
			ILayerNode node = nodes[i];
			TreePath path = selected[i];
			if (layerEnabler.hasLayer(node))
			{
				double opacity = opacitySlider.getValue() / 100d;
				boolean enabled = opacity > 0;
				tree.getModel().setEnabled(node, enabled);
				tree.getModel().setOpacity(node, opacity);

				tree.getUI().relayout(path);

				Rectangle bounds = tree.getPathBounds(path);
				if (bounds != null)
					tree.repaint(bounds);
			}
		}
	}

	private ILayerNode getLayer(TreePath path)
	{
		if (path != null)
		{
			Object o = path.getLastPathComponent();
			if (o instanceof ILayerNode)
			{
				return (ILayerNode) o;
			}
		}
		return null;
	}

	protected void setupToolBarBeforeSlider(JToolBar toolBar)
	{
	}

	protected void setupToolBarAfterSlider(JToolBar toolBar)
	{
	}
}
