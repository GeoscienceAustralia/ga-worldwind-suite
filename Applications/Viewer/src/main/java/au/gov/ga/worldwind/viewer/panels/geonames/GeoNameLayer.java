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
package au.gov.ga.worldwind.viewer.panels.geonames;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.GeographicTextRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple layer used to render {@link GeoName}s. Uses a
 * {@link GeographicTextRenderer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GeoNameLayer extends AbstractLayer
{
	private final GeographicTextRenderer nameRenderer = new GeographicTextRenderer();
	private List<GeographicText> text = new ArrayList<GeographicText>();

	@Override
	protected void doRender(DrawContext dc)
	{
		synchronized (text)
		{
			nameRenderer.render(dc, text);
		}
	}

	public void addText(GeographicText text)
	{
		synchronized (this.text)
		{
			this.text.add(text);
		}
	}

	public void clearText()
	{
		synchronized (this.text)
		{
			this.text.clear();
		}
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}
}
