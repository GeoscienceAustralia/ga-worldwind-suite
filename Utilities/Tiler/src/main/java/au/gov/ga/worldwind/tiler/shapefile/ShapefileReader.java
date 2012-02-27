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
package au.gov.ga.worldwind.tiler.shapefile;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.geotools.dbffile.DbfFile;
import org.geotools.dbffile.DbfFileException;
import org.geotools.shapefile.ShapeHandler;
import org.geotools.shapefile.ShapeTypeNotSupportedException;
import org.geotools.shapefile.Shapefile;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.EndianDataInputStream;

/**
 * Reads a shapefile. Provides access to shapefile features one-by-one.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShapefileReader
{
	private File shp;
	private File dbf;

	private InputStream shpIS;
	private EndianDataInputStream edis;
	private DbfFile dbfFile;

	private GeometryFactory factory;
	private FeatureSchema schema;
	private ShapeHandler handler;
	private int recordIndex;

	private int shapeType;
	private Envelope bounds;

	public ShapefileReader(File file)
	{
		String filename = file.getName();
		int indexOfDot = filename.lastIndexOf('.');
		String filenameNoExtension = indexOfDot >= 0 ? filename.substring(0, indexOfDot) : filename;

		shp = new File(file.getParentFile(), filenameNoExtension + ".shp");
		dbf = new File(file.getParentFile(), filenameNoExtension + ".dbf");
	}

	public void open() throws IOException, DbfFileException, ShapeTypeNotSupportedException
	{
		if (!shp.exists())
			throw new FileNotFoundException("File not found: " + shp);

		shpIS = new FileInputStream(shp);

		factory = new GeometryFactory();
		schema = new FeatureSchema();
		schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);

		edis = new EndianDataInputStream(shpIS);
		ShapefileHeader header = new ShapefileHeader(edis);

		if (header.getVersion() != ShapefileHeader.VERSION)
			System.err.println("Unknown shapefile version, attempting to read anyway");

		shapeType = header.getShapeType();
		bounds = header.getBounds();

		recordIndex = 0;
		handler = null;
		try
		{
			handler = Shapefile.getShapeHandler(shapeType);
		}
		catch (Exception e)
		{
		}

		if (handler == null)
			throw new ShapeTypeNotSupportedException("Unsupported shape type: " + shapeType);

		//initialise the DBF objects if the .dbf file exists
		if (dbf.exists())
		{
			dbfFile = new DbfFile(dbf.getAbsolutePath());

			int numfields = dbfFile.getNumFields();
			for (int j = 0; j < numfields; j++)
			{
				AttributeType at = AttributeType.toAttributeType(dbfFile.getFieldType(j));
				schema.addAttribute(dbfFile.getFieldName(j), at);
			}
		}
	}

	public Feature read() throws IOException
	{
		Geometry geometry = readGeometry();
		if (geometry == null)
			return null;

		Feature feature = new BasicFeature(schema);
		feature.setGeometry(geometry);

		if (dbfFile != null)
		{
			int numfields = dbfFile.getNumFields();
			//StringBuffer s = dbfFile.GetNextDbfRec(); //this function doesn't work, as it doesn't skip the initial offset
			StringBuffer s = dbfFile.GetDbfRec(recordIndex);

			for (int i = 0; i < numfields; i++)
			{
				try
				{
					feature.setAttribute(i + 1, dbfFile.ParseRecordColumn(s, i));
				}
				catch (Exception e)
				{
					throw new IOException(e);
				}
			}
		}
		recordIndex++;

		return feature;
	}

	private Geometry readGeometry() throws IOException
	{
		try
		{
			while (true)
			{
				//edis.setLittleEndianMode(false);
				/*int recordNumber = */edis.readIntBE();
				int contentLength = edis.readIntBE();
				try
				{
					return handler.read(edis, factory, contentLength);
				}
				catch (IOException e)
				{
					throw e;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (EOFException e)
		{
		}

		return null;
	}

	public void close() throws IOException
	{
		if (edis != null)
			edis.close(); //will close shpIS
		else if (shpIS != null)
			shpIS.close();

		if (dbfFile != null)
			dbfFile.close();
	}

	public GeometryFactory getFactory()
	{
		return factory;
	}

	public FeatureSchema getSchema()
	{
		return schema;
	}

	public int getShapeType()
	{
		return shapeType;
	}

	public Envelope getBounds()
	{
		return bounds;
	}
}
