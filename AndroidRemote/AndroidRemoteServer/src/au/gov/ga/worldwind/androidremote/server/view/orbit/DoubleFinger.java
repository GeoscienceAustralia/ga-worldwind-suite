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

import java.awt.geom.Point2D;

import au.gov.ga.worldwind.androidremote.shared.messages.finger.Finger;

/**
 * Used for storing data relating to two fingers (such as the distance and angle
 * between them).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DoubleFinger
{
	public final Finger finger1;
	public final Finger finger2;
	public final double distance;
	public final double angle;

	public DoubleFinger(Finger finger1, Finger finger2)
	{
		this.finger1 = finger1;
		this.finger2 = finger2;
		distance = Point2D.distance(finger1.x, finger1.y, finger2.x, finger2.y);
		angle = Math.atan2(finger2.y - finger1.y, finger2.x - finger1.x);
	}

	public float maxXDifference(DoubleFinger other)
	{
		float delta1 = finger1.x - other.finger1.x;
		float delta2 = finger2.x - other.finger2.x;
		return Math.abs(delta1) > Math.abs(delta2) ? delta1 : delta2;
	}

	public float maxYDifference(DoubleFinger other)
	{
		float delta1 = finger1.y - other.finger1.y;
		float delta2 = finger2.y - other.finger2.y;
		return Math.abs(delta1) > Math.abs(delta2) ? delta1 : delta2;
	}
}
