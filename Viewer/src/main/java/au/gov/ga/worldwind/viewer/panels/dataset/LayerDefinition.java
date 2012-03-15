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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;

/**
 * Basic implementation of the {@link ILayerDefinition} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerDefinition extends AbstractData implements ILayerDefinition
{
	private URL layerURL;
	private boolean enabled;
	private boolean def;

	public LayerDefinition(String name, URL layerURL, URL infoURL, URL iconURL, boolean base, boolean def)
	{
		this(name, layerURL, infoURL, iconURL, base, def, true);
	}

	public LayerDefinition(String name, URL layerURL, URL infoURL, URL iconURL, boolean base, boolean def,
			boolean enabled)
	{
		super(name, infoURL, iconURL, base);
		this.layerURL = layerURL;
		this.def = def;
		this.enabled = enabled;
	}

	@Override
	public URL getLayerURL()
	{
		return layerURL;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public boolean isDefault()
	{
		return def;
	}

	@Override
	public MutableTreeNode createMutableTreeNode(LazyTreeModel model)
	{
		return new DefaultMutableTreeNode(this, false);
	}
}
