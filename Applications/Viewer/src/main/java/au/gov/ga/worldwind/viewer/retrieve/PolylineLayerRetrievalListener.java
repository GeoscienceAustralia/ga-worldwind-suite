package au.gov.ga.worldwind.viewer.retrieve;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Tile;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.viewer.settings.Settings;

import nasa.worldwind.retrieve.ExtendedRetrievalService.RetrievalListener;

public class PolylineLayerRetrievalListener extends RenderableLayer implements RetrievalListener
{
	private static final Color COLOR = new Color(1f, 0f, 0f, 0.5f);

	private final Map<Retriever, SectorPolyline> retrievingLines =
			new HashMap<Retriever, SectorPolyline>();

	@Override
	public void beforeRetrieve(Retriever retriever)
	{
		if (Settings.get().isShowDownloads())
		{
			Tile tile = RetrievalListenerHelper.getTile(retriever);
			if (tile != null)
			{
				Sector sector = tile.getSector();
				SectorPolyline s = new SectorPolyline(sector);
				s.setColor(COLOR);
				s.setLineWidth(2.0);
				s.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);

				synchronized (retrievingLines)
				{
					retrievingLines.put(retriever, s);
				}

				addRenderable(s);
			}
		}
	}

	@Override
	public void afterRetrieve(Retriever retriever)
	{
		SectorPolyline s = retrievingLines.get(retriever);
		if (s != null)
		{
			removeRenderable(s);
			firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	@Override
	protected synchronized void doRender(DrawContext dc)
	{
		super.doRender(dc);
	}

	@Override
	public synchronized void addRenderable(Renderable renderable)
	{
		super.addRenderable(renderable);
	}

	@Override
	public synchronized void removeRenderable(Renderable renderable)
	{
		super.removeRenderable(renderable);
	}
}
