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
package au.gov.ga.worldwind.androidremote.server.view.orbit;

/**
 * Stores data about the difference between two {@link DoubleFinger} objects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DoubleFingerDelta
{
	public final DoubleFinger start;
	public final DoubleFinger end;

	public final double distanceDelta;
	public final double angleDelta;
	public final double xDelta;
	public final double yDelta;

	public DoubleFingerDelta(DoubleFinger start, DoubleFinger end)
	{
		this.start = start;
		this.end = end;

		distanceDelta = start.distance - end.distance;
		angleDelta = angleBetween(start.angle, end.angle);
		xDelta = start.maxXDifference(end);
		yDelta = -start.maxYDifference(end);
	}

	public static double angleBetween(double rad1, double rad2)
	{
		double diff = rad1 - rad2;
		return diff + (diff > Math.PI ? -Math.PI * 2 : diff < -Math.PI ? Math.PI * 2 : 0);
	}

	public static double absAngleBetween(double rad1, double rad2)
	{
		return Math.abs(angleBetween(rad1, rad2));
	}
}
