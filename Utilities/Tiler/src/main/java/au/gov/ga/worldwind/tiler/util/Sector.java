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
 * Represents a sector in geographic (latitude/longitude) space.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Sector
{
	public static final Sector FULL_SPHERE = new Sector(-90, -180, 90, 180);

	private final double minLatitude;
	private final double minLongitude;
	private final double maxLatitude;
	private final double maxLongitude;

	public Sector(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude)
	{
		this.minLatitude = minLatitude;
		this.minLongitude = minLongitude;
		this.maxLatitude = maxLatitude;
		this.maxLongitude = maxLongitude;
	}

	public double getMinLatitude()
	{
		return minLatitude;
	}

	public double getMinLongitude()
	{
		return minLongitude;
	}

	public double getMaxLatitude()
	{
		return maxLatitude;
	}

	public double getMaxLongitude()
	{
		return maxLongitude;
	}

	public double getDeltaLatitude()
	{
		return maxLatitude - minLatitude;
	}

	public double getDeltaLongitude()
	{
		return maxLongitude - minLongitude;
	}

	public double getCenterLatitude()
	{
		return 0.5 * (maxLatitude + minLatitude);
	}

	public double getCenterLongitude()
	{
		return 0.5 * (maxLongitude + minLongitude);
	}

	public boolean containsPoint(double latitude, double longitude)
	{
		return getMinLatitude() <= latitude && latitude <= getMaxLatitude() && getMinLongitude() <= longitude
				&& longitude <= getMaxLongitude();
	}

	@Override
	public String toString()
	{
		return "(" + minLatitude + "," + minLongitude + "," + maxLatitude + "," + maxLongitude + ")";
	}
}
