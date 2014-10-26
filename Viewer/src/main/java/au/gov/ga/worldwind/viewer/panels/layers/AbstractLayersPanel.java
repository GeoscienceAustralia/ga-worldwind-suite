/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.panels.layers;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.util.message.ViewerMessageConstants.*;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.util.layertree.KMLFeatureTreeNode;
import gov.nasa.worldwind.util.tree.TreeNode;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwindx.examples.kml.KMLViewController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.common.layers.Bounds;
import au.gov.ga.worldwind.common.retrieve.ExtendedRetrievalService;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.resizabletoolbar.ResizableToolBar;
import au.gov.ga.worldwind.common.util.FlyToSectorAnimator;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.view.orbit.FlyToOrbitViewAnimator;
import au.gov.ga.worldwind.viewer.panels.layers.LayerEnabler.RefreshListener;
import au.gov.ga.worldwind.viewer.retrieve.LayerTreeRetrievalListener;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

/**
 * Superclass for panels that display layer information.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractLayersPanel extends AbstractThemePanel
{
	protected WorldWindow wwd;

	protected LayerTree tree;
	protected INode root;

	protected BasicAction enableAction;
	protected BasicAction disableAction;

	protected LayerEnabler layerEnabler;
	protected JSlider opacitySlider;
	private boolean ignoreOpacityChange = false;

	public AbstractLayersPanel()
	{
		super(new BorderLayout());
	}

	@Override
	public void setup(Theme theme)
	{
		root = createRootNode(theme);
		tree = new LayerTree(theme.getWwd(), root);
		layerEnabler = tree.getEnabler();

		RetrievalService rs = WorldWind.getRetrievalService();
		if (rs instanceof ExtendedRetrievalService)
		{
			LayerTreeRetrievalListener listener = new LayerTreeRetrievalListener(layerEnabler);
			((ExtendedRetrievalService) rs).addRetrievalListener(listener);
		}

		tree.setRootVisible(false);
		tree.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(MINIMUM_LIST_HEIGHT, MINIMUM_LIST_HEIGHT));

		createActions();
		createToolBar();

		addEnableComponentListeners();
		addFlyToLayerListener();
		addResizedListener();

		enableComponents();

		wwd = theme.getWwd();
	}

	private void addEnableComponentListeners()
	{
		layerEnabler.addRefreshListener(new RefreshListener()
		{
			@Override
			public void refreshed()
			{
				enableComponents();
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				enableComponents();
			}
		});
	}

	private void addResizedListener()
	{
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				Dimension size = getLayout().preferredLayoutSize(AbstractLayersPanel.this);
				size.width = 100;
				setPreferredSize(size);
			}
		});
	}

	/**
	 * Adds a mouse listener that will fly to a layer extents on double-click
	 */
	private void addFlyToLayerListener()
	{
		tree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					int row = tree.getRowForLocation(e.getX(), e.getY());
					Rectangle bounds = tree.getRowBounds(row);
					if (bounds == null)
					{
						return;
					}

					//remove height from left edge to ignore space taken by the checkbox
					bounds.width -= bounds.height;
					bounds.x += bounds.height;
					if (!bounds.contains(e.getPoint()))
					{
						return;
					}
					TreePath path = tree.getPathForRow(row);
					if (path == null)
					{
						return;
					}

					Object o = path.getLastPathComponent();
					if (!(o instanceof ILayerNode))
					{
						return;
					}
					flyToLayer((ILayerNode) o);
				}
			}
		});
	}

	public void flyToLayer(ILayerNode layer)
	{
		//first check if the tree node is pointing to a KML feature; if so, goto the feature 
		if (layer instanceof TreeNodeLayerNode)
		{
			TreeNode treeNode = ((TreeNodeLayerNode) layer).getTreeNode();
			if (treeNode instanceof KMLFeatureTreeNode)
			{
				KMLAbstractFeature feature = ((KMLFeatureTreeNode) treeNode).getFeature();

				KMLViewController viewController = KMLViewController.create(wwd);
				if (viewController != null)
				{
					viewController.goTo(feature);
					wwd.redraw();
					return;
				}
			}
		}

		Bounds bounds = layerEnabler.getLayerExtents(layer);
		Sector sector = bounds.toSector();
		if (sector == null || !(wwd.getView() instanceof OrbitView))
		{
			return;
		}

		OrbitView orbitView = (OrbitView) wwd.getView();
		Position center = orbitView.getCenterPosition();
		Position newCenter;
		if (sector.contains(center) && sector.getDeltaLatDegrees() > 90 && sector.getDeltaLonDegrees() > 90)
		{
			newCenter = center;
		}
		else
		{
			newCenter = new Position(sector.getCentroid(), 0);
		}

		LatLon endVisibleDelta = new LatLon(sector.getDeltaLat(), sector.getDeltaLon());
		long lengthMillis = SettingsUtil.getScaledLengthMillis(center, newCenter);

		FlyToOrbitViewAnimator animator =
				FlyToSectorAnimator.createFlyToSectorAnimator(orbitView, center, newCenter, orbitView.getHeading(),
						orbitView.getPitch(), orbitView.getZoom(), endVisibleDelta, lengthMillis);
		orbitView.stopAnimations();
		orbitView.stopMovement();
		orbitView.addAnimator(animator);

		wwd.redraw();
	}

	protected abstract INode createRootNode(Theme theme);

	protected void createActions()
	{
		enableAction =
				new BasicAction(getMessage(getLayersEnableLayerLabelKey()),
						getMessage(getLayersEnableLayerTooltipKey()), Icons.check.getIcon());
		enableAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableSelected(true);
			}
		});

		disableAction =
				new BasicAction(getMessage(getLayersDisableLayerLabelKey()),
						getMessage(getLayersDisableLayerTooltipKey()), Icons.uncheck.getIcon());
		disableAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableSelected(false);
			}
		});
	}

	private void createToolBar()
	{
		JToolBar toolBar = new ResizableToolBar();
		add(toolBar, BorderLayout.PAGE_START);

		setupToolBarBeforeSlider(toolBar);

		toolBar.add(enableAction);
		toolBar.add(disableAction);
		createOpacitySlider();
		toolBar.add(opacitySlider);

		setupToolBarAfterSlider(toolBar);
	}

	private void createOpacitySlider()
	{
		opacitySlider = new JSlider(0, 100, 100);
		opacitySlider.setToolTipText(getMessage(getLayersOpacityTooltipKey()));
		Dimension size = opacitySlider.getPreferredSize();
		size.width = 60;
		opacitySlider.setPreferredSize(size);
		opacitySlider.setMinimumSize(size);
		opacitySlider.setMaximumSize(size);

		opacitySlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				setSelectedOpacity();
			}
		});
	}

	private void enableComponents()
	{
		setOpacitySlider();

		TreePath[] selected = tree.getSelectionPaths();
		ILayerNode layer = firstChildLayer(selected, false);
		enableAction.setEnabled(layer != null);
		disableAction.setEnabled(layer != null);
	}

	private void enableSelected(boolean enabled)
	{
		TreePath[] selected = tree.getSelectionPaths();
		Set<ILayerNode> nodes = getChildLayers(selected, false);

		for (ILayerNode node : nodes)
		{
			if (tree.getLayerModel().isEnabled(node) != enabled)
			{
				setEnabled(node, enabled);
			}
		}
	}

	public void setEnabled(ILayerNode node, boolean enabled)
	{
		tree.getLayerModel().setEnabled(node, enabled);
		relayoutRepaint(node);
	}

	private void setOpacitySlider()
	{
		if (ignoreOpacityChange)
		{
			return;
		}

		TreePath[] selected = tree.getSelectionPaths();
		ILayerNode layer = firstChildLayer(selected, true);
		if (layer != null)
		{
			double opacity = layer.isEnabled() ? layer.getOpacity() * 100d : 0;
			ignoreOpacityChange = true;
			opacitySlider.setValue((int) Math.round(opacity));
			ignoreOpacityChange = false;
			opacitySlider.setEnabled(true);
		}
		else
		{
			opacitySlider.setEnabled(false);
		}
	}

	private void setSelectedOpacity()
	{
		if (ignoreOpacityChange)
		{
			return;
		}
		ignoreOpacityChange = true;

		TreePath[] selected = tree.getSelectionPaths();
		Set<ILayerNode> nodes = getChildLayers(selected, true);

		double opacity = opacitySlider.getValue() / 100d;
		for (ILayerNode node : nodes)
		{
			setOpacity(node, opacity);
		}

		ignoreOpacityChange = false;
		setOpacitySlider();
	}

	public void setOpacity(ILayerNode node, double opacity)
	{
		tree.getLayerModel().setEnabled(node, opacity > 0);
		tree.getLayerModel().setOpacity(node, opacity);
		relayoutRepaint(node);
	}

	private void relayoutRepaint(INode node)
	{
		TreePath path = new TreePath(tree.getLayerModel().getPathToRoot(node));
		tree.getUI().relayout(path);

		Rectangle bounds = tree.getPathBounds(path);
		if (bounds != null)
		{
			tree.repaint(bounds);
		}
	}

	protected ILayerNode firstChildLayer(TreePath[] selected, boolean hasLayer)
	{
		if (selected == null)
			return null;

		for (TreePath path : selected)
		{
			Object o = path.getLastPathComponent();
			if (o instanceof INode)
			{
				ILayerNode layer = firstChildLayer((INode) o, hasLayer);
				if (layer != null)
				{
					return layer;
				}
			}
		}
		return null;
	}

	protected ILayerNode firstChildLayer(INode node, boolean hasLayer)
	{
		if (node instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) node;
			if (!hasLayer || layerEnabler.hasLayer(layer))
			{
				return layer;
			}
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			ILayerNode layer = firstChildLayer(node.getChild(i), hasLayer);
			if (layer != null)
			{
				return layer;
			}
		}
		return null;
	}

	protected Set<ILayerNode> getChildLayers(TreePath[] selected, boolean hasLayer)
	{
		Set<ILayerNode> layers = new HashSet<ILayerNode>();
		if (selected != null)
		{
			for (TreePath path : selected)
			{
				Object o = path.getLastPathComponent();
				if (o instanceof INode)
				{
					addChildLayers((INode) o, layers, hasLayer);
				}
			}
		}
		return layers;
	}

	protected Set<ILayerNode> getChildLayers(INode node, boolean hasLayer)
	{
		Set<ILayerNode> layers = new HashSet<ILayerNode>();
		addChildLayers(node, layers, hasLayer);
		return layers;
	}

	private void addChildLayers(INode node, Set<ILayerNode> list, boolean hasLayer)
	{
		if (node instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) node;
			if (!hasLayer || layerEnabler.hasLayer(layer))
			{
				list.add(layer);
			}
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			addChildLayers(node.getChild(i), list, hasLayer);
		}
	}

	protected void setupToolBarBeforeSlider(JToolBar toolBar)
	{
	}

	protected void setupToolBarAfterSlider(JToolBar toolBar)
	{
	}

	public void addQueryClickListener(QueryClickListener listener)
	{
		tree.getLayerCellRenderer().addQueryClickListener(listener);
	}

	public void removeQueryClickListener(QueryClickListener listener)
	{
		tree.getLayerCellRenderer().removeQueryClickListener(listener);
	}

	public boolean containsLayer(Layer layer)
	{
		return layerEnabler.hasLayer(layer);
	}

	public INode getRoot()
	{
		return root;
	}

	public LayerTree getTree()
	{
		return tree;
	}
}
