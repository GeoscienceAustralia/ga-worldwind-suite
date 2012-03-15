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

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link LayerList} subclass which implements the {@link SectionList}
 * interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SectionListLayerList extends LayerList implements SectionList<Layer>
{
	private Map<Object, Layer> sectionMap = new HashMap<Object, Layer>();

	@Override
	public void registerSectionObject(Object section)
	{
		Layer layer = new DummyLayer();
		sectionMap.put(section, layer);
		add(layer);
	}

	@Override
	public void addAllFromSection(Object section, Collection<? extends Layer> c)
	{
		int index = -1;
		if (sectionMap.containsKey(section))
		{
			index = indexOf(sectionMap.get(section));
		}

		if (index < 0)
		{
			addAll(c);
			return;
		}

		addAll(index + 1, c);
	}

	@Override
	public void removeAllFromSection(Object section, Collection<? extends Layer> c)
	{
		removeAll(c);
	}

	private class DummyLayer extends AbstractLayer
	{
		@Override
		public void render(DrawContext dc)
		{
		}

		@Override
		public void pick(DrawContext dc, Point point)
		{
		}

		@Override
		public void preRender(DrawContext dc)
		{
		}

		@Override
		protected void doRender(DrawContext dc)
		{
		}
	}
}
