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
package au.gov.ga.worldwind.viewer.theme;

import java.net.URL;

import au.gov.ga.worldwind.viewer.panels.dataset.LayerDefinition;

/**
 * Basic implementation of the {@link ThemeLayer} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicThemeLayer extends LayerDefinition implements ThemeLayer
{
	private boolean visible;

	public BasicThemeLayer(String name, URL url, URL infoURL, URL iconURL, boolean enabled, boolean visible)
	{
		super(name, url, infoURL, iconURL, true, true, enabled);
		setVisible(visible);
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
}
