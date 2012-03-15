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

import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getAnimationObjectFlavor;
import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getStringFlavor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import au.gov.ga.worldwind.animator.animation.AnimationObject;

/**
 * A transferable object used to transfer {@link AnimationObject}s.
 * <p/>
 * Supports the {@link String} flavor (will return the object's name) and the {@link AnimationObject} flavor (will return the object itself).
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimationObjectTransferable implements Transferable
{
	private AnimationObject animationObject;
	
	private static final DataFlavor[] SUPPORTED_FLAVORS = new DataFlavor[]{getAnimationObjectFlavor(), getStringFlavor()};
	
	public AnimationObjectTransferable(AnimationObject animationObject)
	{
		this.animationObject = animationObject;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return SUPPORTED_FLAVORS;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return isAnimationObjectFlavor(flavor) || isStringFlavor(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (isStringFlavor(flavor))
		{
			return animationObject.getName();
		}
		
		if (isAnimationObjectFlavor(flavor))
		{
			return animationObject;
		}
		
		return null;
	}

	private boolean isAnimationObjectFlavor(DataFlavor flavor)
	{
		return getAnimationObjectFlavor().equals(flavor);
	}
	
	private boolean isStringFlavor(DataFlavor flavor)
	{
		return getStringFlavor().equals(flavor);
	}

}
