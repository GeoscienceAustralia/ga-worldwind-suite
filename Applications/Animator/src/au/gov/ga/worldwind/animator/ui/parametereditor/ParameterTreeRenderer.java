package au.gov.ga.worldwind.animator.ui.parametereditor;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.ui.AnimationTreeRenderer;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * An extension of the {@link AnimationTreeRenderer} that adds a tristate checkbox
 * for 'selected' parameters to be included in the parameter editor graph window.
 */
class ParameterTreeRenderer extends AnimationTreeRenderer
{
	private static final long serialVersionUID = 20101102L;
	
	private final ParameterTreeModel treeModel;

	private JCheckBox selectedParameterCheckBox;
	private Map<Integer, ParameterCheckboxLocation> parameterCheckMap = new HashMap<Integer, ParameterCheckboxLocation>();
	
	public ParameterTreeRenderer(ParameterTreeModel treeModel)
	{
		Validate.notNull(treeModel, "A parameter tree model is required");
		
		this.treeModel = treeModel;
		
		selectedParameterCheckBox = new JCheckBox();
		selectedParameterCheckBox.setIcon(Icons.uncheck.getIcon());
		selectedParameterCheckBox.setSelectedIcon(Icons.check.getIcon());
		selectedParameterCheckBox.setOpaque(false);
		selectedParameterCheckBox.setMargin(new Insets(0,0,0,0));
	}

	@Override
	protected void updateButtonPanelContents(Object value)
	{
		if (!(value instanceof Parameter))
		{
			getButtonPanel().remove(selectedParameterCheckBox);
			return;
		}
		selectedParameterCheckBox.setSelected(treeModel.isSelected((Parameter)value));
		getButtonPanel().add(selectedParameterCheckBox);
	}

	@Override
	protected void updateButtonPanelLocationMaps(Object value, int row)
	{
		if (!(value instanceof Parameter))
		{
			parameterCheckMap.remove(row);
			return;
		}
		
		Rectangle checkBounds = new Rectangle(selectedParameterCheckBox.getPreferredSize());
		checkBounds.x += selectedParameterCheckBox.getLocation().x;
		
		parameterCheckMap.put(row, new ParameterCheckboxLocation((Parameter)value, checkBounds));
	}

	@Override
	protected void handleMousePressed(int mouseRow, Point clickPoint)
	{
		if (!isCheckParameterCheckClick(mouseRow, clickPoint))
		{
			return;
		}

		Parameter clickedParameter = parameterCheckMap.get(mouseRow).parameter;
		
		treeModel.toggleParameterSelection(clickedParameter);
	}
	
	private boolean isCheckParameterCheckClick(int mouseRow, Point clickPoint)
	{
		if (!parameterCheckMap.containsKey(mouseRow))
		{
			return false;
		}
		
		return parameterCheckMap.get(mouseRow).containsPoint(calculateRelativeMouseCoords(clickPoint, mouseRow));
	}
	/**
	 * A container that links a parameter to the location of a 'selected' checkbox
	 */
	private static class ParameterCheckboxLocation 
	{
		private Parameter parameter;
		private Rectangle bounds;
		
		public ParameterCheckboxLocation(Parameter parameter, Rectangle bounds)
		{
			this.parameter = parameter;
			this.bounds = bounds;
		}

		public boolean containsPoint(Point p)
		{
			return bounds.contains(p);
		}
		
	}
}
