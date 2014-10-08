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

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Container class which stores a single record for the {@link ShapefileTile}.
 * This is used to store geometry when building the tiles, and are converted to
 * shapefile features at the end of the process.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TileRecord
{
	/**
	 * Shapefile id for the shapefile feature which this record contains geomtry
	 * from.
	 */
	public final int shapeId;
	/**
	 * Shapefile feature attributes associated with this record.
	 */
	public final Attributes attributes;
	/**
	 * List of coordinates associated with this record (contains the actual
	 * geometry).
	 */
	public final List<Coordinate> coordinates;
	/**
	 * Any sub-holes within this record.
	 */
	public final List<TileRecord> holes = new ArrayList<TileRecord>();

	/**
	 * Has this record exited the {@link ShapefileTile} it is associated with?
	 */
	public boolean exited = false;
	/**
	 * Has this record entered the {@link ShapefileTile} it is associated with?
	 */
	public boolean entered = false;

	public TileRecord(int shapeId, boolean entered, Attributes attributes)
	{
		this(shapeId, entered, attributes, new ArrayList<Coordinate>());
	}

	public TileRecord(int shapeId, boolean entered, Attributes attributes, List<Coordinate> coordinates)
	{
		this.shapeId = shapeId;
		this.entered = entered;
		this.attributes = attributes;
		this.coordinates = coordinates;
	}

	public double area()
	{
		double area = 0;
		Coordinate lastCoordinate = coordinates.get(coordinates.size() - 1);
		for (Coordinate coordinate : coordinates)
		{
			area += (lastCoordinate.x + coordinate.x) * (lastCoordinate.y - coordinate.y);
			lastCoordinate = coordinate;
		}
		area /= 2;
		for (TileRecord hole : holes)
		{
			area += hole.area();
		}
		return area;
	}
}
