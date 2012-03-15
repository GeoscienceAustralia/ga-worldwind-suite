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
package au.gov.ga.worldwind.common.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * A base implementation of the {@link CollapsiblePanel} interface that extends
 * {@link JPanel}.
 * <p/>
 * Provides convenience implementations of the {@link CollapsiblePanel} methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class CollapsiblePanelBase extends JPanel implements CollapsiblePanel
{
	private static final long serialVersionUID = 20100906L;

	private boolean resizable = true;
	private boolean expanded = true;
	private boolean activated = true;
	private float weight = 1.0f;

	public CollapsiblePanelBase()
	{
		super(new BorderLayout());
	}

	@Override
	public JPanel getPanel()
	{
		return this;
	}

	@Override
	public boolean isResizable()
	{
		return resizable;
	}

	@Override
	public void setResizable(boolean resizable)
	{
		this.resizable = resizable;
	}

	@Override
	public float getWeight()
	{
		return weight;
	}

	@Override
	public void setWeight(float weight)
	{
		this.weight = weight;
	}

	@Override
	public boolean isExpanded()
	{
		return expanded;
	}

	@Override
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	@Override
	public boolean isOn()
	{
		return activated;
	}

	@Override
	public void setOn(boolean on)
	{
		this.activated = on;
	}

	@Override
	public Icon getIcon()
	{
		return null;
	}

	protected Window getParentWindow()
	{
		Component comp = this;
		while (comp != null && !(comp instanceof Window))
		{
			comp = comp.getParent();
		}

		return (Window) comp;
	}

	protected Frame getParentFrame()
	{
		Component comp = this;
		while (comp != null && !(comp instanceof Frame))
		{
			comp = comp.getParent();
		}

		return (Frame) comp;
	}
}
