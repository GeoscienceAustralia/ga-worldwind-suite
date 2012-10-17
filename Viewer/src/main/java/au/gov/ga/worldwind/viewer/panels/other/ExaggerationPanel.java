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
package au.gov.ga.worldwind.viewer.panels.other;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.common.ui.JDoubleField;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;

/**
 * {@link ThemePanel} for controlling vertical exaggeration. Implements a
 * logarithmically-scaled slider.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExaggerationPanel extends AbstractThemePanel
{
	private WorldWindow wwd;

	private JSlider exaggerationSlider;
	private JDoubleField exaggerationField;
	private JLabel exaggerationLabel;
	private boolean ignoreExaggerationChange = false;

	public ExaggerationPanel()
	{
		super(new GridBagLayout());
		setResizable(false);
		GridBagConstraints c;

		double settingsExaggeration = Settings.get().getVerticalExaggeration();
		exaggerationSlider =
				new JSlider(0, 2000, Math.max(0, Math.min(2000, exaggerationToSlider(settingsExaggeration))));
		Dimension size = exaggerationSlider.getPreferredSize();
		size.width = 50;
		exaggerationSlider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		add(exaggerationSlider, c);

		exaggerationField = new JDoubleField(null, 2);
		exaggerationField.setPositive(true);
		exaggerationField.setHorizontalAlignment(JTextField.RIGHT);
		size = exaggerationField.getPreferredSize();
		size.width = 34;
		exaggerationField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		add(exaggerationField, c);

		exaggerationLabel = new JLabel("x");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.anchor = GridBagConstraints.WEST;
		add(exaggerationLabel, c);

		set(settingsExaggeration, false, false);
		exaggerationSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				set(sliderToExaggeration(exaggerationSlider.getValue()), true, false);
			}
		});

		exaggerationField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				textChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				textChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				textChanged();
			}
		});
	}

	protected void textChanged()
	{
		Double value = exaggerationField.getValue();
		if (value != null)
		{
			if (value > 100)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						exaggerationField.setValue(100.0);
						exaggerationField.setSelectionStart(0);
						exaggerationField.setSelectionEnd(0);
					}
				});
			}
			else if (value < 0)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						exaggerationField.setValue(0.0);
						exaggerationField.setSelectionStart(0);
						exaggerationField.setSelectionEnd(0);
					}
				});
			}
			else
			{
				set(value, false, true);
			}
		}
	}

	@Override
	public Icon getIcon()
	{
		return Icons.exaggeration.getIcon();
	}

	//logarithmic vertical exaggeration slider
	
	private static int exaggerationToSlider(double exaggeration)
	{
		double y = exaggeration;
		double x = Math.log10(y + (100d - y) / 100d);
		return (int) Math.round(x * 1000d);
	}

	private static double sliderToExaggeration(int slider)
	{
		double x = slider / 1000d;
		double y = (Math.pow(10d, x + 2) - 100d) / 99d;
		return y;
	}

	private void set(double exaggeration, boolean valueFromSlider, boolean valueFromField)
	{
		if (ignoreExaggerationChange)
		{
			return;
		}

		ignoreExaggerationChange = true;

		if (!valueFromField)
		{
			exaggerationField.setValue(exaggeration);
			exaggerationField.setSelectionStart(0);
			exaggerationField.setSelectionEnd(0);
		}

		if (!valueFromSlider)
		{
			//only change slider if current value doesn't resolve to current exaggeration
			double currentSliderExaggeration = sliderToExaggeration(exaggerationSlider.getValue());
			if (currentSliderExaggeration != exaggeration)
			{
				exaggerationSlider.setValue(exaggerationToSlider(exaggeration));
			}
		}

		if (valueFromSlider || valueFromField)
		{
			Settings.get().setVerticalExaggeration(exaggeration);
			if (wwd != null)
			{
				wwd.getSceneController().setVerticalExaggeration(exaggeration);
				wwd.redraw();
			}
		}

		ignoreExaggerationChange = false;
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();

		wwd.getSceneController().addPropertyChangeListener(AVKey.VERTICAL_EXAGGERATION, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				set(Settings.get().getVerticalExaggeration(), false, false);
			}
		});
	}

	@Override
	public void dispose()
	{
	}
}
