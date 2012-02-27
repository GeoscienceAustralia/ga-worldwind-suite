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

import java.io.InputStream;

/**
 * Helper class that creates a daemon thread which reads from an InputStream,
 * and passes the read data as a string to an abstract function.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class InputStreamHandler
{
	public InputStreamHandler(final InputStream is)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				byte[] buffer = new byte[10240];
				try
				{
					int len;
					while ((len = is.read(buffer)) > 0)
					{
						handle(new String(buffer, 0, len));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public abstract void handle(String string);
}
