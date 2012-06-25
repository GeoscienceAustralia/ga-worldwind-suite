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
package au.gov.ga.worldwind.common.ui;

/**
 * Shadow copy of SwingUtil that ignores the EDT and runs tasks in the current
 * Thread.
 */
public class SwingUtil
{
	public static void invokeTaskOnEDT(Runnable task) throws SwingEDTException
	{
		task.run();
	}

	public static void invokeLaterTaskOnEDT(Runnable task)
	{
		task.run();
	}
}
