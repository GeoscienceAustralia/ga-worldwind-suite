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
package au.gov.ga.worldwind.tiler.application;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Main class of the Tiler. Runs the {@link Application} class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Executable
{
	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
	}

	public static void main(String[] args)
	{
		try
		{
			start();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void start() throws Exception
	{
		// start the application
		Application.start();
	}
}
