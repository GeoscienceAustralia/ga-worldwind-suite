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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.util.Icons;

public class ExaggerationPanel extends AbstractThemePanel
{
	private WorldWindow wwd;

	private JSlider exaggerationSlider;
	private JLabel exaggerationLabel;
	private boolean ignoreExaggerationChange = false;

	public ExaggerationPanel()
	{
		super(new GridBagLayout());
		setResizable(false);
		GridBagConstraints c;
		int i = 0;

		double settingsExaggeration = Settings.get().getVerticalExaggeration();
		exaggerationSlider =
				new JSlider(0, 2000, Math.max(0, Math.min(2000,
						exaggerationToSlider(settingsExaggeration))));
		Dimension size = exaggerationSlider.getPreferredSize();
		size.width = 50;
		exaggerationSlider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		add(exaggerationSlider, c);

		exaggerationLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.anchor = GridBagConstraints.WEST;
		add(exaggerationLabel, c);

		set(settingsExaggeration, false);
		exaggerationSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				set(sliderToExaggeration(exaggerationSlider.getValue()), true);
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
		double x = Math.log10(y + (1000d - y) / 1000d);
		return (int) Math.round(x * 1000d);
	}

	private double sliderToExaggeration(int slider)
	{
		double x = slider / 1000d;
		double y = Math.pow(10d, x) - (2d - x) / 2d;
		return y;
	}

	private void set(double exaggeration, boolean valueFromSlider)
	{
		if (ignoreExaggerationChange)
			return;

		ignoreExaggerationChange = true;

		String format = "%1." + (exaggeration < 10 ? "2" : exaggeration < 100 ? "1" : "0") + "f";
		String text = String.format(format, exaggeration);
		if (text.indexOf('.') < 0)
			text += ".";
		exaggerationLabel.setText(text + " x");

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
			double currentSliderExaggeration = sliderToExaggeration(exaggerationSlider.getValue());
			if (currentSliderExaggeration != exaggeration)
				exaggerationSlider.setValue(exaggerationToSlider(exaggeration));
		}

		ignoreExaggerationChange = false;
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
