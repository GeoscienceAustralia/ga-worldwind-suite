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
package au.gov.ga.worldwind.animator.util;

import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.util.Logging;
import au.gov.ga.worldwind.animator.terrain.exaggeration.VerticalExaggerationElevationModel;
import au.gov.ga.worldwind.common.downloader.DownloaderStatusBar;

/**
 * A status bar that corrects for vertical exaggeration applied via a
 * {@link VerticalExaggerationElevationModel}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ExaggerationAwareStatusBar extends DownloaderStatusBar
{
	private static final long serialVersionUID = 20101006L;
	
	@Override
	protected void handleCursorPositionChange(PositionEvent event)
	{
		Position newPos = event.getPosition();
		if (newPos != null)
		{
			String las = makeAngleDescription("Lat", newPos.getLatitude());
			String los = makeAngleDescription("Lon", newPos.getLongitude());
			String els = makeCursorElevationDescription(getRealElevationAtPosition(newPos.getLatitude(), newPos.getLongitude()));
			latDisplay.setText(las);
			lonDisplay.setText(los);
			eleDisplay.setText(els);
		}
		else
		{
			latDisplay.setText("");
			lonDisplay.setText(Logging.getMessage("term.OffGlobe"));
			eleDisplay.setText("");
		}
	}

	private double getRealElevationAtPosition(Angle latitude, Angle longitude)
	{
		ElevationModel elevationModel = getEventSource().getModel().getGlobe().getElevationModel();
		if (elevationModel instanceof VerticalExaggerationElevationModel)
		{
			return ((VerticalExaggerationElevationModel)elevationModel).getUnexaggeratedElevation(latitude, longitude);
		}
		return elevationModel.getElevation(latitude, longitude);
	}

}
