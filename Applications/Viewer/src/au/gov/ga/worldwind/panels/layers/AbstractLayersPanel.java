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
		boolean enable = false;
		if (selected != null)
		{
			Object o = selected.getLastPathComponent();
			if (o instanceof ILayerNode)
			{
				ILayerNode node = (ILayerNode) o;
				if (layerEnabler.hasLayer(node))
				{
					enable = true;
					double opacity = node.isEnabled() ? node.getOpacity() * 100d : 0;
					ignoreSliderChange = true;
					opacitySlider.setValue((int) Math.round(opacity));
					ignoreSliderChange = false;
				}
			}
		}
		opacitySlider.setEnabled(enable);
	}

	private void setSelectedOpacity()
	{
		TreePath selected = tree.getSelectionPath();
		if (selected != null)
		{
			Object o = selected.getLastPathComponent();
			if (o instanceof ILayerNode)
			{
				ILayerNode node = (ILayerNode) o;
				if (layerEnabler.hasLayer(node))
				{
					double opacity = opacitySlider.getValue() / 100d;
					boolean enabled = opacity > 0;
					tree.getModel().setEnabled(node, enabled);
					tree.getModel().setOpacity(node, opacity);

					((ClearableBasicTreeUI) tree.getUI()).relayout(selected);

					Rectangle bounds = tree.getPathBounds(selected);
					if (bounds != null)
						tree.repaint(bounds);
				}
			}
		}
	}

	protected void setupToolBarBeforeSlider(JToolBar toolBar)
	{
	}

	protected void setupToolBarAfterSlider(JToolBar toolBar)
	{
	}
}
