/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.worldwind.common.view.hmd.oculus;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import au.gov.ga.worldwind.common.view.hmd.HMDDistortion;
import au.gov.ga.worldwind.common.view.hmd.HMDView;
import de.fruitfly.ovr.HMDInfo;
import de.fruitfly.ovr.IOculusRift;
import de.fruitfly.ovr.OculusRift;

/**
 * {@link HMDView} implementation for the Oculus Rift. Uses the JRift library.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OculusView extends HMDView
{
	private final HMDDistortion distortion;
	private Matrix headRotation;

	public OculusView()
	{
		System.loadLibrary("JRiftLibrary");
		final IOculusRift oculus = new OculusRift();
		oculus.init();
		HMDInfo hmd = oculus.getHMDInfo();
		distortion = new HMDDistortion(new OculusHMDParameters(hmd), hmd.HResolution, hmd.VResolution);

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
					}
					oculus.poll();
					updateHeadRotation(Angle.fromDegrees(oculus.getYawDegrees_LH()),
							Angle.fromDegrees(oculus.getPitchDegrees_LH()),
							Angle.fromDegrees(oculus.getRollDegrees_LH()));
					firePropertyChange(AVKey.VIEW, null, OculusView.this);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void updateHeadRotation(Angle yaw, Angle pitch, Angle roll)
	{
		Matrix z = Matrix.fromRotationZ(roll);
		Matrix xy = Matrix.fromRotationXYZ(pitch, yaw, Angle.ZERO);
		headRotation = z.multiply(xy);
	}

	@Override
	public HMDDistortion getDistortion()
	{
		return distortion;
	}

	@Override
	protected Matrix transformModelView(Matrix modelView)
	{
		return headRotation.multiply(modelView);
	}
}
