package layers;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.BasicLineStyle;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.sun.opengl.util.texture.TextureIO;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Simple Vector layer that draws in-memory tiles from any geotools vector
 * source.
 * 
 * Right now limitted to files in WGS84, but that should not be too hard to fix.
 */
public class VectorLayer extends TiledImageLayer
{

	MapContext context = new DefaultMapContext(DefaultGeographicCRS.WGS84);

	public VectorLayer()
	{
		super(makeLevels());
	}

	public void addLayer(String name,
			FeatureCollection<SimpleFeatureType, SimpleFeature> features,
			Style style)
	{
		style = new BasicLineStyle();
		context.addLayer(new DefaultMapLayer(features, style, name));
	}

	public void addShapefileLayer(URL shapefile, URL styleFile)
			throws Exception
	{
		String name = shapefile.getFile();
		ShapefileDataStore ds = new ShapefileDataStore(shapefile);
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = ds
				.getFeatureSource().getFeatures();

		addLayer(name, features, null);
		
		/*StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);
		SLDParser stylereader = new SLDParser(factory, styleFile);
		org.geotools.styling.Style[] style = stylereader.readXML();

		addLayer(name, features, style[0]);*/
	}


	protected void requestTexture(DrawContext dc, final TextureTile tile)
	{
		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());

		if (this.getReferencePoint() != null)
			tile.setPriority(centroid.distanceTo3(this.getReferencePoint()));

		BufferedImage image = new BufferedImage(tile.getLevel().getTileWidth(),
				tile.getLevel().getTileHeight(), BufferedImage.TYPE_INT_ARGB);

		this.getRequestQ().add(new RenderTask(tile, image));
	}

	public void drawImage(BufferedImage image, TextureTile tile)
	{
		Sector sector = tile.getSector();


		Envelope env = new Envelope(sector.getMinLongitude().getDegrees(),
				sector.getMaxLongitude().getDegrees(), sector.getMinLatitude()
						.getDegrees(), sector.getMaxLatitude().getDegrees());

		ReferencedEnvelope re = new ReferencedEnvelope(env, context
				.getCoordinateReferenceSystem());

		Graphics2D graphics = image.createGraphics();

		StreamingRenderer renderer = new StreamingRenderer();
		renderer.setContext(context);

		renderer.paint(graphics, new Rectangle(0, 0, image.getWidth(), image
				.getHeight()), re);
	}


	private void addTileToCache(TextureTile tile)
	{
		WorldWind.getMemoryCache(TextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected void forceTextureLoad(TextureTile tile)
	{
		BufferedImage image = new BufferedImage(tile.getLevel().getTileWidth(),
				tile.getLevel().getTileHeight(), BufferedImage.TYPE_INT_ARGB);

		new RenderTask(tile, image).run();
	}

	class RenderTask implements Runnable, Comparable<RenderTask>
	{
		TextureTile tile;
		BufferedImage image;

		public RenderTask(TextureTile tile, BufferedImage image)
		{
			this.tile = tile;
			this.image = image;
		}

		public void run()
		{
			drawImage(image, tile);

			tile.setTextureData(TextureIO.newTextureData(image, false));

			addTileToCache(tile);

			getLevels().unmarkResourceAbsent(tile);
			firePropertyChange(AVKey.LAYER, null, VectorLayer.this);
		}

		public int compareTo(RenderTask that)
		{
			return this.tile.getPriority() == that.tile.getPriority() ? 0
					: this.tile.getPriority() < that.tile.getPriority() ? -1
							: 1;
		}

		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RenderTask that = (RenderTask) o;

			// Don't include layer in comparison so that requests are shared among layers
			return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
		}

		public int hashCode()
		{
			return (tile != null ? tile.hashCode() : 0);
		}
	}

	protected static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Vector");
		params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
		params.setValue(AVKey.SERVICE, "http://www.example.com");
		params.setValue(AVKey.DATASET_NAME, "vector");
		params.setValue(AVKey.NUM_LEVELS, 20);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);

		return new LevelSet(params);
	}
}
