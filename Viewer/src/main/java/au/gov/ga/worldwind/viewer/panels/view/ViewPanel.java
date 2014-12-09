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
package au.gov.ga.worldwind.viewer.panels.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.view.delegate.IDelegateView;
import au.gov.ga.worldwind.common.view.delegate.IViewDelegate;
import au.gov.ga.worldwind.common.view.oculus.RiftViewDistortionDelegate;
import au.gov.ga.worldwind.common.view.stereo.IStereoViewDelegate;
import au.gov.ga.worldwind.common.view.stereo.StereoViewDelegate;
import au.gov.ga.worldwind.common.view.target.ITargetView;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;

/**
 * {@link ThemePanel} used to switch between different view types.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ViewPanel extends AbstractThemePanel
{
	private WorldWindow wwd;

	private JRadioButton orbitRadio;
	private JRadioButton oculusRadio;
	private JCheckBox targetCheck;
	private JCheckBox farCheck;

	public ViewPanel()
	{
		super(new GridBagLayout());
		setResizable(false);
		GridBagConstraints c;
		JPanel panel;

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		ButtonGroup bg = new ButtonGroup();
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setupView();
			}
		};

		orbitRadio = new JRadioButton("Orbit");
		bg.add(orbitRadio);
		orbitRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1d / 2d;
		panel.add(orbitRadio, c);

		oculusRadio = new JRadioButton("Oculus");
		bg.add(oculusRadio);
		oculusRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1d / 2d;
		panel.add(oculusRadio, c);

		targetCheck = new JCheckBox("Lock rotation point on surface");
		targetCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		panel.add(targetCheck, c);
		
		farCheck = new JCheckBox("Prioritize far-clipping");
		farCheck.setSelected(true);
		farCheck.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		panel.add(farCheck, c);
	}

	@Override
	public Icon getIcon()
	{
		return Icons.view.getIcon();
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();
		wwd.getSceneController().addPropertyChangeListener(AVKey.VIEW, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				updateRadioButtons();
			}
		});
		updateRadioButtons();
	}

	@Override
	public void dispose()
	{
	}

	protected void updateRadioButtons()
	{
		View view = wwd.getView();
		if (view instanceof IDelegateView)
		{
			IViewDelegate delegate = ((IDelegateView) view).getDelegate();
			if (delegate instanceof IStereoViewDelegate)
			{
				orbitRadio.setSelected(true);
			}
			else if (delegate instanceof RiftViewDistortionDelegate)
			{
				oculusRadio.setSelected(true);
			}
		}
		if (view instanceof ITargetView)
		{
			targetCheck.setSelected(!((ITargetView) view).isTargetMode());
		}
	}

	protected void setupView()
	{
		if (wwd == null)
			return;

		if (!(wwd.getView() instanceof IDelegateView))
			return;

		IDelegateView view = (IDelegateView) wwd.getView();
		IViewDelegate oldDelegate = view.getDelegate();
		IViewDelegate delegate = null;

		if (orbitRadio.isSelected() && !(oldDelegate instanceof IStereoViewDelegate))
		{
			delegate = new StereoViewDelegate();
		}
		else if (oculusRadio.isSelected() && !(oldDelegate instanceof RiftViewDistortionDelegate))
		{
			delegate = new RiftViewDistortionDelegate();
		}

		if (view instanceof ITargetView)
		{
			ITargetView target = (ITargetView) view;
			target.setTargetMode(!targetCheck.isSelected());
			target.setPrioritizeFarClipping(farCheck.isSelected());
		}

		if (delegate == null)
			return;

		view.setDelegate(delegate);
		wwd.redraw();
	}
}
