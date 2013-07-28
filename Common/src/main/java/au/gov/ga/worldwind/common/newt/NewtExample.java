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
package au.gov.ga.worldwind.common.newt;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.Dimension;

/**
 * {@link ApplicationTemplate} subclass showing an example of using the
 * {@link WorldWindowNewtCanvas}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NewtExample extends ApplicationTemplate
{
	public static class AppFrame extends ApplicationTemplate.AppFrame
	{
		public AppFrame()
		{
			super(true, true, true);
		}

		@Override
		protected ApplicationTemplate.AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar)
		{
			return new AppPanel(canvasSize, includeStatusBar);
		}
	}

	public static class AppPanel extends ApplicationTemplate.AppPanel
	{
		public AppPanel(Dimension canvasSize, boolean includeStatusBar)
		{
			super(canvasSize, includeStatusBar);
		}

		@Override
		protected WorldWindow createWorldWindow()
		{
			Configuration.setValue(AVKey.WORLD_WINDOW_CLASS_NAME, WorldWindowNewtAutoDrawable.class.getName());
			Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, NewtInputHandler.class.getName());
			return new WorldWindowNewtCanvas();
		}
	}

	public static void main(String[] args)
	{
		ApplicationTemplate.start("World Wind NEWT", AppFrame.class);
	}
}
