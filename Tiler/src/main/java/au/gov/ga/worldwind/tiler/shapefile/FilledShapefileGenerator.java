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

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import au.gov.ga.worldwind.tiler.util.Sector;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FilledShapefileGenerator
{
	public static void main(String[] args) throws IOException
	{
		File schemaFile = new File("E:/World/water/worldwaterbodies/worldwaterbodies.shp");
		File outputFile = new File("E:/World/water/worldwaterbodies/80to90.shp");

		ShapefileDataStore schemaStore = null, outputStore = null;
		try
		{
			ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
			schemaStore = (ShapefileDataStore) factory.createDataStore(schemaFile.toURI().toURL());
			SimpleFeatureType schema = schemaStore.getSchema();

			GeometryFactory geometryFactory = new GeometryFactory();

			Sector sector = new Sector(80, -180, 90, 180);

			ShapefileTile tile = new ShapefileTile(sector, 0, 0);
			Attributes attributes = new Attributes(schema);
			attributes.values[0] = -1;
			tile.markFilled(attributes);
			tile.completePolygons(0);

			try
			{
				outputStore = new ShapefileDataStore(outputFile.toURI().toURL());
				outputStore.createSchema(schema);
				FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter =
						outputStore.getFeatureWriter(outputStore.getTypeNames()[0], Transaction.AUTO_COMMIT);
				try
				{
					tile.writeFeatures(featureWriter, schema, geometryFactory, true);
				}
				finally
				{
					featureWriter.close();
				}
			}
			finally
			{
				outputStore.dispose();
			}
		}
		finally
		{
			schemaStore.dispose();
		}
	}
}
