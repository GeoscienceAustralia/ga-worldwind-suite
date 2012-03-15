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
package au.gov.ga.worldwind.animator.panels.animationbrowser;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.animator.ui.AnimationTreeRenderer;
import au.gov.ga.worldwind.animator.ui.tristate.ArmableTriStateModel;
import au.gov.ga.worldwind.animator.ui.tristate.EnableableTriStateModel;
import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox;
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.animator.util.Icons;

/**
 * An extension of the {@link AnimationTreeRenderer} that adds a tristate-checkbox for enabled/disabled
 * and another for armed/disarmed.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
class AnimationBrowserTreeRenderer extends AnimationTreeRenderer
{
	private static final long serialVersionUID = 1433749823115631800L;
	
	private TriStateCheckBox enabledTriCheck;
	private TriStateCheckBox armedTriCheck;
	
	private Map<Integer, TriStateModelLocation> enabledTriCheckMap = new HashMap<Integer, TriStateModelLocation>();
	private Map<Integer, TriStateModelLocation> armedTriCheckMap = new HashMap<Integer, TriStateModelLocation>();

	public AnimationBrowserTreeRenderer()
	{
		super();
		
		enabledTriCheck = new TriStateCheckBox();
		armedTriCheck = new TriStateCheckBox(Icons.armed.getIcon(), Icons.disarmed.getIcon(), Icons.partialArmed.getIcon());
	}

	@Override
	protected void updateButtonPanelContents(Object value)
	{
		updateEnabledTriCheck(value);
		updateArmedTriCheck(value);
	}

	/**
	 * Updates the state of the enabled tristate checkbox for the given value and packs it into the button panel.
	 */
	private void updateEnabledTriCheck(Object value)
	{
		if (!(value instanceof Enableable))
		{
			getButtonPanel().remove(enabledTriCheck);
			return;
		}
		
		EnableableTriStateModel model = new EnableableTriStateModel((Enableable)value);
		enabledTriCheck.setModel(model);
		getButtonPanel().add(enabledTriCheck);
	}
	
	/**
	 * Updates the state of the armed tristate checkbox for the given value and packs it into the button panel.
	 */
	private void updateArmedTriCheck(Object value)
	{
		if (!(value instanceof Armable))
		{
			getButtonPanel().remove(armedTriCheck);
			return;
		}
		
		ArmableTriStateModel model = new ArmableTriStateModel((Armable)value);
		armedTriCheck.setModel(model);
		getButtonPanel().add(armedTriCheck);
	}
	
	@Override
	protected void updateButtonPanelLocationMaps(Object value, int row)
	{
		updateEnabledTriCheckMap(value, row);
		updateArmedTriCheckMap(value, row);
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
	
	@Override
	protected void handleMousePressed(int mouseRow, Point clickPoint)
	{
		if (isEnabledTriCheckClick(mouseRow, clickPoint))
		{
			enabledTriCheckMap.get(mouseRow).getModel().iterateState();
		}
		else if (isArmedTriCheckClick(mouseRow, clickPoint))
		{
			armedTriCheckMap.get(mouseRow).getModel().iterateState();
		}
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
}
