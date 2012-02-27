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
package au.gov.ga.worldwind.animator.panels;

import java.awt.datatransfer.DataFlavor;
import java.net.URL;

import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;

/**
 * A class containing supported data transfer flavors in the Animator application.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DataTransferFlavors
{
	private static final DataFlavor ANIMATION_OBJECT_FLAVOR = new DataFlavor(AnimationObject.class, "AnimationObject");
	public static DataFlavor getAnimationObjectFlavor() { return ANIMATION_OBJECT_FLAVOR; }
	
	public static DataFlavor getStringFlavor() { return DataFlavor.stringFlavor; }
	
	public static DataFlavor getFileListFlavor() { return DataFlavor.javaFileListFlavor; }
	
	private static final DataFlavor URL_FLAVOR = new DataFlavor(URL.class, "URL");
	public static DataFlavor getURLFlavor() { return URL_FLAVOR; }
	
	private static final DataFlavor LAYER_IDENTIFIER_FLAVOR = new DataFlavor(LayerIdentifier.class, "LayerIdentifier");
	public static DataFlavor getLayerIdentifierFlavor() { return LAYER_IDENTIFIER_FLAVOR; }
}
