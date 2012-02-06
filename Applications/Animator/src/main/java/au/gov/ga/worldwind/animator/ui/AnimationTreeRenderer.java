package au.gov.ga.worldwind.animator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBoxModel;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.util.HSLColor;

/**
 * An extension of the {@link DefaultTreeCellRenderer} that allows the standard tree components
 * to be decorated with additional elements (check boxes, buttons etc.) that can be tailored to specific uses.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AnimationTreeRenderer extends JPanel implements TreeCellRenderer
{
	private static final long serialVersionUID = 1433749823115631800L;
	
	private JTree tree;
	private DefaultTreeCellRenderer label;
	
	private JPanel buttonPanel;
	
	private TreeCellInteractionListener interactionListener;
	
	public AnimationTreeRenderer()
	{
		setOpaque(false);
		setLayout(new BorderLayout(3,3));
		setBorder(BorderFactory.createEmptyBorder());
		
		initialiseLabel();
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		
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
		
		updateButtonPanelLocationMaps(value, row);
		
		return this;
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
	
	private void updateButtonPanel(Object value)
	{
		buttonPanel.setOpaque(false);
		
		updateButtonPanelContents(value);
		
		add(buttonPanel, BorderLayout.WEST);
	}

	/**
	 * Update any items to be added to this row's button panel
	 * 
	 * @param value The value at the current row in the tree
	 */
	protected abstract void updateButtonPanelContents(Object value);
	
	private void updateLabel(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		label.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		label.setIcon(getIconForRowValue(value));
		
		// We MUST re-add the label to our container after calling getTreeCellRendererComponent or the tree row sizes become wrong
		add(label, BorderLayout.CENTER);
	}

	/**
	 * Update the location maps for items to be included in the button panel
	 * 
	 * @param value The value at the current row in the tree
	 * @param row The current row in the tree
	 */
	protected abstract void updateButtonPanelLocationMaps(Object value, int row);

	
	/**
	 * @return The icon to use for the object at the current row in the tree
	 */
	protected Icon getIconForRowValue(Object rowValue)
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
	
	protected boolean isElevationModelRow(Object rowValue)
	{
		return rowValue instanceof ElevationModelIdentifier;
	}

	protected boolean isAnimatableLayerRow(Object rowValue)
	{
		return rowValue instanceof AnimatableLayer;
	}

	protected boolean isAnimationRow(Object rowValue)
	{
		return rowValue instanceof Animation;
	}
	
	protected boolean isAnimatableObjectRow(Object rowValue)
	{
		return rowValue instanceof Animatable;
	}
	
	protected boolean isParameterRow(Object rowValue)
	{
		return rowValue instanceof Parameter;
	}
	
	/**
	 * A simple container class that relates a {@link TriStateCheckBoxModel} to {@link Rectangle} screen bounds.
	 * <p/>
	 * Used to locate the correct model to fire on a mouse event.
	 */
	protected static final class TriStateModelLocation
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
			handleMousePressed(mouseRow, e.getPoint());
			
			tree.repaint();
		}
		
	}
	
	/**
	 * Handle a mouse pressed event at the provided row in the tree
	 */
	protected abstract void handleMousePressed(int mouseRow, Point clickPoint);
	
	/**
	 * Adjust the provided mouse coordinates to coordinates relative to the current row in the tree
	 */
	protected Point calculateRelativeMouseCoords(Point absoluteCoords, int mouseRow)
	{
		Rectangle rowBounds = tree.getRowBounds(mouseRow);
		return new Point(absoluteCoords.x - rowBounds.x, absoluteCoords.y - rowBounds.y);
	}
	
	public JPanel getButtonPanel()
	{
		return buttonPanel;
	}
}