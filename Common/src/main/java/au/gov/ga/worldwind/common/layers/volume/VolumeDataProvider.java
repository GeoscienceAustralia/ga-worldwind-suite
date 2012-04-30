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
package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Position;

import java.awt.Rectangle;

import au.gov.ga.worldwind.common.layers.data.DataProvider;
import au.gov.ga.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.worldwind.common.util.FastShape;

/**
 * {@link DataProvider} that provides data to a {@link VolumeLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface VolumeDataProvider extends DataProvider<VolumeLayer>
{
	/**
	 * @return The size of the volume's x-axis.
	 */
	int getXSize();

	/**
	 * @return The size of the volume's y-axis.
	 */
	int getYSize();

	/**
	 * @return The size of the volume's z-axis.
	 */
	int getZSize();

	/**
	 * @return The depth of the volume, in meters.
	 */
	double getDepth();

	/**
	 * @return The average elevation of the top slice.
	 */
	double getTop();

	/**
	 * The value of the volume at the given (x,y,z) point.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 * @param z
	 *            z-coordinate
	 * @return The (x,y,z) value of the volume.
	 */
	float getValue(int x, int y, int z);

	/**
	 * @return The minimum value in the volume data.
	 */
	float getMinValue();

	/**
	 * @return The maximum value in the volume data.
	 */
	float getMaxValue();

	/**
	 * The position of the top (z == 0) volume point at the given (x,y) point.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 * @return The position at the (x,y) point.
	 */
	Position getPosition(int x, int y);

	/**
	 * @return The value that identifies no-data.
	 */
	float getNoDataValue();

	/**
	 * Create a horizontal surface with elevation.
	 * 
	 * @param maxVariance
	 *            BTT variance (see {@link BinaryTriangleTree}).
	 * @param rectangle
	 *            Sub-rectangle with the x and y axes.
	 * @return A {@link FastShape} containing the horizontal surface mesh.
	 */
	FastShape createHorizontalSurface(float maxVariance, Rectangle rectangle);

	/**
	 * Create a curtain along a given longitude. The top and bottom of the
	 * curtain follows the volume's elevation at that longitude.
	 * 
	 * @param x
	 *            x-coordinate of the curtain
	 * @return A {@link TopBottomFastShape} containing a triangle mesh curtain.
	 */
	TopBottomFastShape createLongitudeCurtain(int x);

	/**
	 * Create a curtain along a given latitude. The top and bottom of the
	 * curtain follows the volume's elevation at that latitude.
	 * 
	 * @param y
	 *            y-coordinate of the curtain
	 * @return A {@link TopBottomFastShape} containing a triangle mesh curtain.
	 */
	TopBottomFastShape createLatitudeCurtain(int y);

	/**
	 * @return Shape that can be used to visualize the bounding box of the
	 *         volume.
	 */
	FastShape createBoundingBox();
	
	/**
	 * @return Whether this volume is the special case of a single slice in the x- y- or z-direction.
	 */
	boolean isSingleSliceVolume();
}
