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
package au.gov.ga.worldwind.tiler.gdal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconstConstants;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.worldwind.tiler.util.Sector;

/**
 * Unit tests for the {@link GDALTile} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALTileTest
{

	@BeforeClass
	public static void init()
	{
		GDALUtil.init();
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithNullParams() throws Exception
	{
		GDALTileParameters params = null;
		new GDALTile(params);
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithNullDataset() throws Exception
	{
		Dataset dataset = null;
		Dimension size = new Dimension(512, 512);
		Sector sector = new Sector(50, 50, 100, 100);
		
		GDALTileParameters params = new GDALTileParameters(dataset, size, sector);
		new GDALTile(params);
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithNullSize() throws Exception
	{
		Dataset dataset = openTestDataset();
		Dimension size = null;
		Sector sector = new Sector(50, 50, 100, 100);
		
		GDALTileParameters params = new GDALTileParameters(dataset, size, sector);
		new GDALTile(params);
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithNullSectorAndNullRectangle() throws Exception
	{
		Dataset dataset = openTestDataset();
		Dimension size = new Dimension(512, 512);
		Sector sector = null;
		
		GDALTileParameters params = new GDALTileParameters(dataset, size, sector);
		new GDALTile(params);
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithInvalidSector() throws Exception
	{
		Dataset dataset = openTestDataset();
		Dimension size = new Dimension(512, 512);
		Sector sector = new Sector(100, 100, 100, 100);
		
		GDALTileParameters params = new GDALTileParameters(dataset, size, sector);
		new GDALTile(params);
	}
	
	@Test
	public void testCreateWithValidParams() throws Exception
	{
		Dataset dataset = openTestDataset();
		Dimension size = new Dimension(512, 512);
		Sector sector = new Sector(-27, 141, -25, 144);
		
		GDALTileParameters params = new GDALTileParameters(dataset, size, sector);
		GDALTile tile = new GDALTile(params);
		
		assertEquals(gdalconstConstants.GDT_Int16, tile.getBufferType());
		assertEquals(2, tile.getBufferTypeSize());
		assertNotNull(tile.getBuffer());
		assertEquals(512*512*2, tile.getBuffer().limit());
		assertFalse(tile.isBlank());
		assertFalse(tile.isFloatingPoint());
	}
	
	@Test
	public void testGetAsImage() throws Exception
	{
		Dataset dataset = openTestDataset();
		Dimension size = new Dimension(512, 512);
		Sector sector = new Sector(-27, 141, -25, 144);
		
		GDALTileParameters params = new GDALTileParameters(dataset, size, sector);
		GDALTile tile = new GDALTile(params);
		
		BufferedImage image = tile.getAsImage();
		
		assertNotNull(image);
		assertEquals(512, image.getWidth());
		assertEquals(512, image.getHeight());
		assertEquals(BufferedImage.TYPE_USHORT_GRAY, image.getType());
	}
	
	private static Dataset openTestDataset() throws Exception
	{
		File f = new File(GDALTileTest.class.getResource("testgrid.tif").toURI());
		return GDALUtil.open(f);
	}
}
