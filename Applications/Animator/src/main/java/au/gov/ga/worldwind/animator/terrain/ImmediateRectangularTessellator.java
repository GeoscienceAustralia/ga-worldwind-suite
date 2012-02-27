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
package au.gov.ga.worldwind.animator.terrain;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.RectangularTessellator;
import au.gov.ga.worldwind.common.terrain.WireframeRectangularTessellator;

/**
 * An extension of the {@link RectangularTessellator} that ignores the cache and
 * regenerates vertices every repaint, instead of caching and regenerating
 * every second.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImmediateRectangularTessellator extends WireframeRectangularTessellator
{
	@Override
	protected void makeVerts(DrawContext dc, RectTile tile)
	{
		this.buildVerts(dc, tile, isMakeTileSkirts());
	}
}
