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
package au.gov.ga.worldwind.viewer.theme.hud;

import javax.swing.Icon;

import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.viewer.theme.ThemeHUD;

/**
 * {@link ThemeHUD} implementation that displays the {@link CompassLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CompassHUD extends AbstractThemeHUD
{
	private CompassLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new CompassLayer();
		return layer;
	}

	@Override
	public void doSetPosition(String position)
	{
		layer.setPosition(position);
	}

	@Override
	public String getPosition()
	{
		return layer.getPosition();
	}

	@Override
	public Icon getIcon()
	{
		return Icons.compass.getIcon();
	}
}
