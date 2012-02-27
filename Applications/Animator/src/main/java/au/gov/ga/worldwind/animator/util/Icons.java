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

/**
 * Extension of the common {@link au.gov.ga.worldwind.common.util.Icons}
 * accessor class with additional icons specific to the Animator project.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Icons extends au.gov.ga.worldwind.common.util.Icons
{
	public static final Icons armed = new Icons("armed.gif");
	public static final Icons disarmed = new Icons("disarmed.gif");
	public static final Icons elevation = new Icons("elevation.gif");
	public static final Icons key = new Icons("key.gif");
	public static final Icons animatableLayer = new Icons("layer_object.gif");
	public static final Icons lock = new Icons("lock.gif");
	public static final Icons animatableObject = new Icons("object.gif");
	public static final Icons parameter = new Icons("parameter.gif");
	public static final Icons partialArmed = new Icons("partial_armed.gif");
	public static final Icons render = new Icons("render.gif");
	public static final Icons unlock = new Icons("unlock.gif");
	public static final Icons zoomx = new Icons("zoomx.gif");
	public static final Icons zoomxy = new Icons("zoomxy.gif");
	public static final Icons zoomy = new Icons("zoomy.gif");

	protected Icons(String string)
	{
		super(string);
	}
}
