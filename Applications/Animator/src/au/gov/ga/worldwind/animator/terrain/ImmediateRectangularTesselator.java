package au.gov.ga.worldwind.animator.terrain;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.render.DrawContext;
import nasa.worldwind.terrain.RectangularTessellator;

/**
 * An extension of the {@link RectangularTessellator} that updates the cache of vertices
 * every frame, rather than every second.
 */
public class ImmediateRectangularTesselator extends RectangularTessellator
{

	@Override
	protected void makeVerts(DrawContext dc, RectTile tile)
	{
        MemoryCache cache = WorldWind.getMemoryCache(CACHE_ID);
        CacheKey cacheKey = this.createCacheKey(dc, tile);
        tile.ri = (RenderInfo) cache.getObject(cacheKey);

        tile.ri = this.buildVerts(dc, tile, isMakeTileSkirts());
        if (tile.ri != null)
        {
            cacheKey = this.createCacheKey(dc, tile);
            cache.add(cacheKey, tile.ri, tile.ri.getSizeInBytes());
        }
	}
	
}
