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
package au.gov.ga.worldwind.tiler.util;

/**
 * Represents a latitude/longitude pair.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LatLon
{
	public final static LatLon DEFAULT_ORIGIN = new LatLon(-90, -180);

	private final double latitude;
	private final double longitude;

	public LatLon(double latitude, double longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	@Override
	public String toString()
	{
		return "(" + latitude + "," + longitude + ")";
	}
}
