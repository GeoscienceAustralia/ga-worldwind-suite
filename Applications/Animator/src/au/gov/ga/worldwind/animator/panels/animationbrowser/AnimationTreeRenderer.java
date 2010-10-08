package au.gov.ga.worldwind.animator.panels.animationbrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox;
import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBoxModel;
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.util.HSLColor;

/**
 * An extension of the {@link DefaultTreeCellRenderer} that decorates the standard tree components
 * with additional elements specific to the animation browser (check boxes, buttons etc.)
 */
class AnimationTreeRenderer extends JPanel implements TreeCellRenderer
{
	private static final long serialVersionUID = 1433749823115631800L;
	
	private JTree tree;
	private DefaultTreeCellRenderer label;
	
	private JPanel buttonPanel;
	private TriStateCheckBox enabledTriCheck;
	private TriStateCheckBox armedTriCheck;
	
	private Map<Integer, TriStateModelLocation> enabledTriCheckMap = new HashMap<Integer, TriStateModelLocation>();
	private Map<Integer, TriStateModelLocation> armedTriCheckMap = new HashMap<Integer, TriStateModelLocation>();

	private TreeCellInteractionListener interactionListener;
	
	public AnimationTreeRenderer()
	{
		setOpaque(false);
		setLayout(new BorderLayout(3,3));
		setBorder(BorderFactory.createEmptyBorder());
		
		initialiseLabel();
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		enabledTriCheck = new TriStateCheckBox();

		armedTriCheck = new TriStateCheckBox(Icons.armed.getIcon(), Icons.disarmed.getIcon(), Icons.partialArmed.getIcon());
		
		interactionListener = new TreeCellInteractionListener();
	}

	private void initialiseLabel()
	{
		label = new DefaultTreeCellRenderer();
		label.setTextNonSelectionColor(Color.black);
		label.setTextSelectionColor(Color.black);
		Color backgroundSelection = label.getBackgroundSelectionColor();
		HSLColor hsl = new HSLColor(backgroundSelection);
		label.setBackgroundSelectionColor(hsl.adjustTone(80));
		label.setBorderSelectionColor(null);
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		updateTree(tree);
		
		updateButtonPanel(value);
		updateLabel(tree, value, sel, expanded, leaf, row, hasFocus);
		
		validate();
		
		updateEnabledTriCheckMap(value, row);
		updateArmedTriCheckMap(value, row);
		
		return this;
	}

	/**
	 * @param value
	 */
	private void updateButtonPanel(Object value)
	{
		buttonPanel.setOpaque(false);
		
		updateEnabledTriCheck(value);
		updateArmedTriCheck(value);
		
		add(buttonPanel, BorderLayout.WEST);
	}

	/**
	 * Updates the state of the enabled tristate checkbox for the given value and packs it into the parent container.
	 */
	private void updateEnabledTriCheck(Object value)
	{
		if (!(value instanceof Enableable))
		{
			buttonPanel.remove(enabledTriCheck);
			return;
		}
		
		EnableableTriStateModel model = new EnableableTriStateModel((Enableable)value);
		enabledTriCheck.setModel(model);
		buttonPanel.add(enabledTriCheck);
	}
	
	/**
	 * Updates the state of the armed tristate checkbox for the given value and packs it into the parent container.
	 */
	private void updateArmedTriCheck(Object value)
	{
		if (!(value instanceof Armable))
		{
			buttonPanel.remove(armedTriCheck);
			return;
		}
		
		ArmableTriStateModel model = new ArmableTriStateModel((Armable)value);
		armedTriCheck.setModel(model);
		buttonPanel.add(armedTriCheck);
	}
	
	
	private void updateEnabledTriCheckMap(Object value, int row)
	{
		if (!(value instanceof Enableable))
		{
			enabledTriCheckMap.remove(row);
		}
		
		Rectangle triCheckBounds = new Rectangle(enabledTriCheck.getPreferredSize());
		triCheckBounds.x += enabledTriCheck.getLocation().x;
		
		enabledTriCheckMap.put(row, new TriStateModelLocation(triCheckBounds, enabledTriCheck.getModel()));
	}

	private void updateArmedTriCheckMap(Object value, int row)
	{
		if (!(value instanceof Armable))
		{
			armedTriCheckMap.remove(row);
		}
		
		Rectangle triCheckBounds = new Rectangle(armedTriCheck.getPreferredSize());
		triCheckBounds.x += armedTriCheck.getLocation().x;
		
		armedTriCheckMap.put(row, new TriStateModelLocation(triCheckBounds, armedTriCheck.getModel()));
	}

	private void updateLabel(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		label.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		label.setIcon(getIconForRowValue(value));
		
		// We MUST re-add the label to our container after calling getTreeCellRendererComponent or the tree row sizes become wrong
		add(label, BorderLayout.CENTER);
	}

	private void updateTree(JTree newTree)
	{
		if (tree == newTree)
		{
			return;
		}
		
		tree = newTree;
		tree.addMouseListener(interactionListener);
	}

	private Icon getIconForRowValue(Object rowValue)
	{
		if (isAnimationRow(rowValue))
		{
			return Icons.world.getIcon();
		}
		if (isAnimatableObjectRow(rowValue))
		{
			if (isAnimatableLayerRow(rowValue))
			{
				return Icons.animatableLayer.getIcon();
			}
			return Icons.animatableObject.getIcon();
		}
		if (isParameterRow(rowValue))
		{
			return Icons.parameter.getIcon();
		}
		if (isElevationModelRow(rowValue))
		{
			return Icons.exaggeration.getIcon();
		}
		return null;
	}
	
	private boolean isElevationModelRow(Object rowValue)
	{
		return rowValue instanceof ElevationModelIdentifier;
	}

	private boolean isAnimatableLayerRow(Object rowValue)
	{
		return rowValue instanceof AnimatableLayer;
	}

	private boolean isAnimationRow(Object rowValue)
	{
		return rowValue instanceof Animation;
	}
	
	private boolean isAnimatableObjectRow(Object rowValue)
	{
		return rowValue instanceof Animatable;
	}
	
	private boolean isParameterRow(Object rowValue)
	{
		return rowValue instanceof Parameter;
	}
	
	/**
	 * A simple container class that relates a {@link TriStateCheckBoxModel} to {@link Rectangle} screen bounds.
	 * <p/>
	 * Used to locate the correct model to fire on a mouse event.
	 */
	private static final class TriStateModelLocation
	{
		private Rectangle bounds;
		private TriStateCheckBoxModel model;
		
		public TriStateModelLocation(Rectangle bounds, TriStateCheckBoxModel model)
		{
			this.bounds = bounds;
			this.model = model;
		}
		
		public boolean containsPoint(Point p)
		{
			return bounds.contains(p);
		}
		
		public TriStateCheckBoxModel getModel()
		{
			return model;
		}
	}
	
	/**
	 * A listener that listens for interactions with the tree cells and propagates events to sub-components as appropriate.
	 */
	private class TreeCellInteractionListener extends MouseAdapter
	{

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (!SwingUtilities.isLeftMouseButton(e))
			{
				return;
			}
			
			int mouseRow = tree.getRowForLocation(e.getX(), e.getY());
			if (isEnabledTriCheckClick(mouseRow, e.getPoint()))
			{
				enabledTriCheckMap.get(mouseRow).getModel().iterateState();
			}
			else if (isArmedTriCheckClick(mouseRow, e.getPoint()))
			{
				armedTriCheckMap.get(mouseRow).getModel().iterateState();
			}
			
			tree.repaint();
		}

		private boolean isEnabledTriCheckClick(int mouseRow, Point clickPoint)
		{
			if (!enabledTriCheckMap.containsKey(mouseRow))
			{
				return false;
			}
			
			Point relativeMouseCoords = calculateRelativeMouseCoords(clickPoint, mouseRow);
			
			TriStateModelLocation modelLocation = enabledTriCheckMap.get(mouseRow);
			return modelLocation.containsPoint(relativeMouseCoords);
		}
		
		private boolean isArmedTriCheckClick(int mouseRow, Point clickPoint)
		{
			if (!armedTriCheckMap.containsKey(mouseRow))
			{
				return false;
			}
			
			Point relativeMouseCoords = calculateRelativeMouseCoords(clickPoint, mouseRow);
			
			TriStateModelLocation modelLocation = armedTriCheckMap.get(mouseRow);
			return modelLocation.containsPoint(relativeMouseCoords);
		}
		
		/**
		 * Adjust the provided mouse coordinates to coordinates relative to the current row in the tree
		 */
		private Point calculateRelativeMouseCoords(Point absoluteCoords, int mouseRow)
		{
			Rectangle rowBounds = tree.getRowBounds(mouseRow);
			return new Point(absoluteCoords.x - rowBounds.x, absoluteCoords.y - rowBounds.y);
		}
		
	}
}