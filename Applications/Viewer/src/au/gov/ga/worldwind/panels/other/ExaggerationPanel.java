package au.gov.ga.worldwind.panels.other;

import gov.nasa.worldwind.WorldWindow;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.theme.AbstractThemePanel;
import au.gov.ga.worldwind.theme.Theme;

public class ExaggerationPanel extends AbstractThemePanel
{
	private WorldWindow wwd;

	public ExaggerationPanel()
	{
		super(new GridBagLayout());

		GridBagConstraints c;

		double settingsExaggeration = Settings.get().getVerticalExaggeration();
		final JSlider slider = new JSlider(0, 200, exaggerationToSlider(settingsExaggeration));
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

		final JLabel sliderLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		add(sliderLabel, c);

		class Setter
		{
			public void set(double exaggeration)
			{
				sliderLabel.setText(String.valueOf(Math.round(exaggeration * 10d) / 10d));
				Settings.get().setVerticalExaggeration(exaggeration);
				if (wwd != null)
				{
					wwd.getSceneController().setVerticalExaggeration(exaggeration);
					wwd.redraw();
				}
			}
		}
		final Setter setter = new Setter();
		setter.set(settingsExaggeration);

		final ChangeListener listener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				double exaggeration = sliderToExaggeration(slider.getValue());
				setter.set(exaggeration);
			}
		};
		slider.addChangeListener(listener);
	}

	//logarithmic vertical exaggeration slider

	private int exaggerationToSlider(double exaggeration)
	{
		double y = exaggeration;
		double x = Math.log10(y + (100 - y) / 100);
		return (int) (x * 100);
	}

	private double sliderToExaggeration(int slider)
	{
		double x = slider / 100d;
		double y = Math.pow(10d, x) - (2 - x) / 2;
		return y;
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();
	}

	@Override
	public void dispose()
	{
	}
}
