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
package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

import javax.swing.ImageIcon;

import au.gov.ga.worldwind.common.ui.lazytree.ILoadingNode;

/**
 * Represents a tree node that has an icon. The icon can be loaded lazily.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IIconItem extends ILoadingNode
{
	/**
	 * @return Has the icon associated with this node been loaded?
	 */
	boolean isIconLoaded();

	/**
	 * Load this node's icon.
	 * 
	 * @param afterLoad
	 *            Runnable to run after the icon has been loaded.
	 */
	void loadIcon(Runnable afterLoad);

	/**
	 * @return This node's icon.
	 */
	ImageIcon getIcon();

	/**
	 * @return The URL pointing to the icon.
	 */
	URL getIconURL();

	/**
	 * Set the icon's URL.
	 * 
	 * @param iconURL
	 */
	void setIconURL(URL iconURL);
}
