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
package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

import au.gov.ga.worldwind.animator.layers.immediate.ImmediateCurtainLocalRequesterDelegate;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateCurtainURLRequesterDelegate;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateImageLocalRequesterDelegate;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateImageURLRequesterDelegate;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMercatorImageLocalRequesterDelegate;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMercatorImageURLRequesterDelegate;
import au.gov.ga.worldwind.common.layers.curtain.delegate.CurtainDelegateFactory;
import au.gov.ga.worldwind.common.layers.curtain.delegate.CurtainLocalRequesterDelegate;
import au.gov.ga.worldwind.common.layers.curtain.delegate.CurtainURLRequesterDelegate;
import au.gov.ga.worldwind.common.layers.mercator.delegate.MercatorImageDelegateFactory;
import au.gov.ga.worldwind.common.layers.mercator.delegate.MercatorImageLocalRequesterDelegate;
import au.gov.ga.worldwind.common.layers.mercator.delegate.MercatorImageURLRequesterDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageDelegateFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageLocalRequesterDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageURLRequesterDelegate;

/**
 * A factory class that can load a {@link Layer} from a provided {@link URL}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 */
public class AnimationLayerLoaderFactory
{
	/**
	 * The class to delegate layer loading to
	 */
	private static AnimationLayerLoader delegate = new DefaultAnimationLayerLoader();
	
	static
	{
		ImageDelegateFactory.get().registerDelegate(ImmediateImageURLRequesterDelegate.class);
		ImageDelegateFactory.get().registerDelegate(ImmediateImageLocalRequesterDelegate.class);
		MercatorImageDelegateFactory.get().registerDelegate(ImmediateMercatorImageURLRequesterDelegate.class);
		MercatorImageDelegateFactory.get().registerDelegate(ImmediateMercatorImageLocalRequesterDelegate.class);
		CurtainDelegateFactory.get().registerDelegate(ImmediateCurtainURLRequesterDelegate.class);
		CurtainDelegateFactory.get().registerDelegate(ImmediateCurtainLocalRequesterDelegate.class);
		//Whenever the ImageURLRequesterDelegate and ImageLocalRequesterDelegate is requested,
		//replace it with the immediate mode version, which will request the textures immediately
		//when immediate mode is active.
		ImageDelegateFactory.get().registerReplacementClass(ImageURLRequesterDelegate.class, ImmediateImageURLRequesterDelegate.class);
		ImageDelegateFactory.get().registerReplacementClass(ImageLocalRequesterDelegate.class, ImmediateImageLocalRequesterDelegate.class);
		MercatorImageDelegateFactory.get().registerReplacementClass(MercatorImageURLRequesterDelegate.class, ImmediateMercatorImageURLRequesterDelegate.class);
		MercatorImageDelegateFactory.get().registerReplacementClass(MercatorImageLocalRequesterDelegate.class, ImmediateMercatorImageLocalRequesterDelegate.class);
		CurtainDelegateFactory.get().registerReplacementClass(CurtainURLRequesterDelegate.class, ImmediateCurtainURLRequesterDelegate.class);
		CurtainDelegateFactory.get().registerReplacementClass(CurtainLocalRequesterDelegate.class, ImmediateCurtainLocalRequesterDelegate.class);
	}
	
	/**
	 * Load the layer identified by the provided identifier.
	 * 
	 * @param identifier The identifier that specifies the layer to open
	 * 
	 * @return The layer identified by the provided identifier, or <code>null</code> if an error occurred during layer load
	 */
	public static Layer loadLayer(LayerIdentifier identifier)
	{
		return getDelegate().loadLayer(identifier);
	}
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL.
	 */
	public static Layer loadLayer(String url)
	{
		return getDelegate().loadLayer(url);
	}
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL, or <code>null</code> if an error occurred during layer loading.
	 */
	public static Layer loadLayer(URL url)
	{
		return getDelegate().loadLayer(url);
	}

	/**
	 * @return the delegate to use for performing layer load operations
	 */
	public static AnimationLayerLoader getDelegate()
	{
		return delegate;
	}

	/**
	 * @param delegate the delegate to set
	 */
	public static void setDelegate(AnimationLayerLoader delegate)
	{
		AnimationLayerLoaderFactory.delegate = delegate;
	}

}
