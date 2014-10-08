/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.worldwind.tiler.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility that uses one polygon shapefile to erase the polygons from another
 * shapefile (inverted clip). Only erases polygons fully contained by the eraser
 * polygons; doesn't chop polygons at edges.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Eraser
{
	public static void main(String[] args) throws IOException
	{
		File input = new File("E:/World/water/worldwaterbodies/worldwaterbodies.shp");
		File erase = new File("E:/World/water/coastline/polygons_wgs84.shp");
		File output = new File("E:/World/water/worldwaterbodies/worldwaterbodies_noaustralia.shp");

		ShapefileDataStore inputStore = null, eraseStore = null, outputStore = null;

		try
		{
			ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

			eraseStore = (ShapefileDataStore) factory.createDataStore(erase.toURI().toURL());
			ContentFeatureSource eraseSource = eraseStore.getFeatureSource();
			ReferencedEnvelope eraseBounds = eraseSource.getBounds();
			ContentFeatureCollection eraseCollection = eraseSource.getFeatures();
			SimpleFeatureIterator eraseFeatures = eraseCollection.features();
			List<Geometry> eraseGeometries = new ArrayList<Geometry>();

			try
			{
				while (eraseFeatures.hasNext())
				{
					SimpleFeature feature = eraseFeatures.next();
					Geometry geometry = (Geometry) feature.getDefaultGeometry();
					eraseGeometries.add(geometry);
				}
			}
			finally
			{
				eraseFeatures.close();
			}

			inputStore = (ShapefileDataStore) factory.createDataStore(input.toURI().toURL());
			inputStore.dispose();
			inputStore = (ShapefileDataStore) factory.createDataStore(input.toURI().toURL());
			ContentFeatureSource inputSource = inputStore.getFeatureSource();
			ContentFeatureCollection inputCollection = inputSource.getFeatures();
			SimpleFeatureIterator inputFeatures = inputCollection.features();
			SimpleFeatureType inputSchema = inputStore.getSchema();

			outputStore = new ShapefileDataStore(output.toURI().toURL());
			outputStore.createSchema(inputSchema);
			FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter =
					outputStore.getFeatureWriter(outputStore.getTypeNames()[0], Transaction.AUTO_COMMIT);

			try
			{
				while (inputFeatures.hasNext())
				{
					SimpleFeature feature = inputFeatures.next();
					Geometry geometry = (Geometry) feature.getDefaultGeometry();

					boolean write = true;
					if (eraseBounds.contains(geometry.getEnvelopeInternal()))
					{
						for (Geometry eraseGeometry : eraseGeometries)
						{
							if (eraseGeometry.getEnvelopeInternal().contains(geometry.getEnvelopeInternal())
									&& eraseGeometry.contains(geometry))
							{
								write = false;
								break;
							}
						}
					}

					if (write)
					{
						SimpleFeature outputFeature = featureWriter.next();
						int n = 0;
						for (Object attribute : feature.getAttributes())
						{
							outputFeature.setAttribute(n++, attribute);
						}
						featureWriter.write();
					}
				}
			}
			finally
			{
				featureWriter.close();
				inputFeatures.close();
			}
		}
		finally
		{
			eraseStore.dispose();
			inputStore.dispose();
			outputStore.dispose();
		}
	}
}
