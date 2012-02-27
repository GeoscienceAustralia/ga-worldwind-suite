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

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

/**
 * JDialog that allows listening for visibility events.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JVisibleDialog extends JDialog
{
	public static interface VisibilityListener
	{
		public void visibleChanged(boolean visible);
	}

	private List<VisibilityListener> listeners = new ArrayList<VisibilityListener>();
	private boolean centerInOwner = false;
	private Frame owner;

	public JVisibleDialog(Frame owner, String title)
	{
		super(owner, title);
		this.owner = owner;
	}

	@Override
	public void setVisible(boolean b)
	{
		if (b && centerInOwner)
		{
			setLocationRelativeTo(owner);
			centerInOwner = false;
		}
		super.setVisible(b);
		notifyVisibilityListeners();
	}

	public void addVisibilityListener(VisibilityListener listener)
	{
		listeners.add(listener);
	}

	public void removeVisibilityListener(VisibilityListener listener)
	{
		listeners.remove(listener);
	}

	protected void notifyVisibilityListeners()
	{
		for (VisibilityListener listener : listeners)
		{
			listener.visibleChanged(isVisible());
		}
	}

	public void centerInOwnerWhenShown()
	{
		centerInOwner = true;
	}
}
