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
package au.gov.ga.worldwind.tiler.application;

import au.gov.ga.worldwind.tiler.util.Sector;

/**
 * Utility that takes a sector and number of levels and outputs the raster size
 * and sector to use for gdal_rasterize. This is useful when needing to convert
 * a shapefile to a raster that is suitably sized for use with
 * ga-worldwind-suite's Tiler application.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtentCalculator
{
	public static void main(String[] args)
	{
		//for world: -180 -90 180 90 in 524288x262144

		Sector sector = new Sector(-43.628666, 113.240620, -8.933329, 153.593161);
		//Sector sector = Sector.FULL;
		int fitIntoPixelsPower = 10; //dimensions will be divisible by 2^10
		int pixelsAt360Power = 22; //number of pixels at 360 degrees = 2^19 = 524288

		double pixelFactor = Math.pow(0.5, pixelsAt360Power - fitIntoPixelsPower) * 360;
		int startX = (int) Math.floor(sector.getMinLongitude() / pixelFactor);
		int startY = (int) Math.floor(sector.getMinLatitude() / pixelFactor);
		int endX = (int) Math.ceil(sector.getMaxLongitude() / pixelFactor);
		int endY = (int) Math.ceil(sector.getMaxLatitude() / pixelFactor);
		int pixelsPerTile = (int) Math.pow(2, fitIntoPixelsPower);
		int width = (endX - startX) * pixelsPerTile;
		int height = (endY - startY) * pixelsPerTile;
		Sector factorSector =
				new Sector(startY * pixelFactor, startX * pixelFactor, endY * pixelFactor, endX * pixelFactor);

		System.out.println(factorSector);
		System.out.println(width + " x " + height);
	}
}
