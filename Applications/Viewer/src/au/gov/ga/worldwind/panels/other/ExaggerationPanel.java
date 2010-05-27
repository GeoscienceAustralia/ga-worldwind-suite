package au.gov.ga.worldwind.panels.other;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.theme.AbstractThemePanel;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.util.Icons;

public class ExaggerationPanel extends AbstractThemePanel
{
	private WorldWindow wwd;
	private JSlider slider;
	private JLabel sliderLabel;
	private boolean ignoreChange = false;

	public ExaggerationPanel()
	{
		super(new GridBagLayout());

		setResizable(false);

		GridBagConstraints c;

		double settingsExaggeration = Settings.get().getVerticalExaggeration();
		slider = new JSlider(0, 200, exaggerationToSlider(settingsExaggeration));
		Dimension size = slider.getPreferredSize();
		size.width = 50;
		slider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		add(slider, c);

		sliderLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		add(sliderLabel, c);

		set(settingsExaggeration, false);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				set(sliderToExaggeration(slider.getValue()), true);
			}
		});
	}

	@Override
	public Icon getIcon()
	{
		return Icons.exaggeration.getIcon();
	}

	//logarithmic vertical exaggeration slider

	private int exaggerationToSlider(double exaggeration)
	{
		double y = exaggeration;
		double x = Math.log10(y + (100d - y) / 100d);
		return (int) Math.round(x * 100d);
	}

	private double sliderToExaggeration(int slider)
	{
		double x = slider / 100d;
		double y = Math.pow(10d, x) - (2d - x) / 2d;
		return y;
	}

	private void set(double exaggeration, boolean valueFromSlider)
	{
		if (ignoreChange)
			return;

		ignoreChange = true;

		sliderLabel.setText(String.valueOf(Math.round(exaggeration * 10d) / 10d));
		if (valueFromSlider)
		{
			Settings.get().setVerticalExaggeration(exaggeration);
			if (wwd != null)
			{
				wwd.getSceneController().setVerticalExaggeration(exaggeration);
				wwd.redraw();
			}
		}
		else
		{
			//only change slider if current value doesn't resolve to current exaggeration
			double currentSliderExaggeration = sliderToExaggeration(slider.getValue());
			if (currentSliderExaggeration != exaggeration)
				slider.setValue(exaggerationToSlider(exaggeration));
		}

		ignoreChange = false;
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();

		wwd.getSceneController().addPropertyChangeListener(AVKey.VERTICAL_EXAGGERATION,
				new PropertyChangeListener()
				{
					@Override
					public void propertyChange(PropertyChangeEvent evt)
					{
						set(Settings.get().getVerticalExaggeration(), false);
					}
				});
	}

	@Override
	public void dispose()
	{
	}
}
