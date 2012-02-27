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
 * A wrapper exception that allows checked exceptions from the EDT thread to be
 * wrapped in an unchecked exception so clients can decide if they want to deal
 * with them or not.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SwingEDTException extends RuntimeException
{
	private static final long serialVersionUID = 20101126L;

	public SwingEDTException(Throwable cause)
	{
		super(cause);
	}
}
