package au.gov.ga.worldwind.animator.panels.animationbrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
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
	
	private static final Icon CHECKED_ICON = Icons.check.getIcon();
	private static final Icon UNCHECKED_ICON = Icons.uncheck.getIcon();
	private static final Icon PARTIAL_CHECKED_ICON = Icons.partialCheck.getIcon();
	
	private JTree tree;
	private DefaultTreeCellRenderer label;
	private JLabel enabledTriCheck;
	
	public AnimationTreeRenderer()
	{
		setOpaque(false);
		setLayout(new BorderLayout(3,3));
		
		initialiseLabel();
		
		enabledTriCheck = new JLabel();
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
		
		updateEnabledTriCheck(value);
		updateLabel(tree, value, sel, expanded, leaf, row, hasFocus);
		
		validate();
		
		return this;
	}

	private void updateEnabledTriCheck(Object value)
	{
		if (value instanceof Enableable)
		{
			Enableable enableableValue = (Enableable)value;
			if (!enableableValue.isEnabled())
			{
				enabledTriCheck.setIcon(UNCHECKED_ICON);
			}
			else if (enableableValue.isAllChildrenEnabled())
			{
				enabledTriCheck.setIcon(CHECKED_ICON);
			}
			else
			{
				enabledTriCheck.setIcon(PARTIAL_CHECKED_ICON);
			}
		}
		else
		{
			enabledTriCheck.setIcon(null);
		}
		add(enabledTriCheck, BorderLayout.WEST);
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
	}

	private Icon getIconForRowValue(Object rowValue)
	{
		if (isAnimationRow(rowValue))
		{
			return Icons.world.getIcon();
		}
		if (isAnimatableObjectRow(rowValue))
		{
			return Icons.animatableObject.getIcon();
		}
		if (isParameterRow(rowValue))
		{
			return Icons.parameter.getIcon();
		}
		return null;
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
}