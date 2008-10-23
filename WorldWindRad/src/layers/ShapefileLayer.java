package layers;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class ShapefileLayer extends RenderableLayer
{
	public ShapefileLayer(URL shapefile)
	{
		try
		{
			ShapefileDataStore ds = new ShapefileDataStore(shapefile);
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = ds
					.getFeatureSource().getFeatures();
			FeatureIterator<SimpleFeature> fit = features.features();

			List<Polyline> lines = new ArrayList<Polyline>();
			List<Position> positions;
			while (fit.hasNext())
			{
				SimpleFeature sf = fit.next();

				String type = (String) sf.getAttribute(1);
				if (type.equals("BOUNDARY") /*|| type.equals("COASTLINE")*/)
				{
					Object obj = sf.getDefaultGeometry();
					if (obj instanceof MultiLineString)
					{
						MultiLineString mls = (MultiLineString) obj;
						for (int n = 0; n < mls.getNumGeometries(); n++)
						{
							Geometry geometry = mls.getGeometryN(n);
							if (geometry instanceof LineString)
							{
								LineString ls = (LineString) geometry;
								Coordinate[] coordinates = ls.getCoordinates();
								positions = new ArrayList<Position>();
								for (Coordinate coordinate : coordinates)
								{
									positions
											.add(new Position(
													Angle
															.fromDegreesLatitude(coordinate.y),
													Angle
															.fromDegreesLongitude(coordinate.x),
													0));
								}
								Polyline line = new Polyline(positions);
								if(type.equals("BOUNDARY"))
								{
									line.setFollowTerrain(true);
								}
								lines.add(line);
							}
						}
					}
				}
			}

			for (Polyline line : lines)
			{
				addRenderable(line);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
