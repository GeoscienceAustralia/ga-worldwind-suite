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
package au.gov.ga.worldwind.common.layers.point;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.util.ArrayList;

import org.junit.Test;

import au.gov.ga.worldwind.common.layers.point.providers.ShapefilePointProvider;
import au.gov.ga.worldwind.common.layers.point.providers.XMLPointProvider;
import au.gov.ga.worldwind.common.layers.styled.Attribute;
import au.gov.ga.worldwind.common.layers.styled.Style;
import au.gov.ga.worldwind.common.util.AVKeyMore;

/**
 * Unit tests for the {@link PointLayerHelper}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PointLayerHelperTest
{

	@Test (expected = IllegalArgumentException.class)
	public void testCreateWithNull()
	{
		AVList params = null;
		
		new PointLayerHelper(params);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testCreateWithEmpty()
	{
		AVList params = new AVListImpl();
		
		new PointLayerHelper(params);
	}
	
	@Test
	public void testCreateWithShapefileProvider()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new ShapefilePointProvider());
		params.setValue(AVKeyMore.URL, "file:/some/url");
		params.setValue(AVKeyMore.DATA_CACHE_NAME, "test");
		params.setValue(AVKeyMore.DATA_LAYER_STYLES, new ArrayList<Style>());
		params.setValue(AVKeyMore.DATA_LAYER_ATTRIBUTES, new ArrayList<Attribute>());
		
		new PointLayerHelper(params);
	}
	
	@Test
	public void testCreateWithXMLProvider()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new XMLPointProvider(null));
		params.setValue(AVKeyMore.URL, "file:/some/url");
		params.setValue(AVKeyMore.DATA_CACHE_NAME, "test");
		params.setValue(AVKeyMore.DATA_LAYER_STYLES, new ArrayList<Style>());
		params.setValue(AVKeyMore.DATA_LAYER_ATTRIBUTES, new ArrayList<Attribute>());
		
		new PointLayerHelper(params);
	}
	
	@Test
	public void testCreateWithXMLProviderNoURL()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new XMLPointProvider(null));
		params.setValue(AVKeyMore.URL, "");
		params.setValue(AVKeyMore.DATA_CACHE_NAME, "test");
		params.setValue(AVKeyMore.DATA_LAYER_STYLES, new ArrayList<Style>());
		params.setValue(AVKeyMore.DATA_LAYER_ATTRIBUTES, new ArrayList<Attribute>());
		
		new PointLayerHelper(params);
	}
	
	@Test
	public void testCreateWithXMLProviderNoDataCache()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new XMLPointProvider(null));
		params.setValue(AVKeyMore.URL, "file:/some/url");
		params.setValue(AVKeyMore.DATA_CACHE_NAME, "");
		params.setValue(AVKeyMore.DATA_LAYER_STYLES, new ArrayList<Style>());
		params.setValue(AVKeyMore.DATA_LAYER_ATTRIBUTES, new ArrayList<Attribute>());
		
		new PointLayerHelper(params);
	}
}
