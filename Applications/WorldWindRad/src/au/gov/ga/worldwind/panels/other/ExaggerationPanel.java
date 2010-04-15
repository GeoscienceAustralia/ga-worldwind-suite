package au.gov.ga.worldwind.panels.other;

import gov.nasa.worldwind.WorldWindow;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.settings.Settings;


public class ExaggerationPanel extends JPanel
{
	public ExaggerationPanel(final WorldWindow wwd)
	{
		super(new GridBagLayout());

		GridBagConstraints c;
		
		JLabel label = new JLabel();
		label.setText("Vertical scale:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		add(label, c);

		double settingsExaggeration = Settings.get().getVerticalExaggeration();
		final JSlider slider = new JSlider(0, 200,
				exaggerationToSlider(settingsExaggeration));
		Dimension size = slider.getPreferredSize();
		size.width = 50;
		slider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		add(slider, c);

		final JLabel sliderLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		add(sliderLabel, c);

		class Setter
		{
			public void set(double exaggeration)
			{
				sliderLabel.setText(String
						.valueOf(Math.round(exaggeration * 10d) / 10d));
				Settings.get().setVerticalExaggeration(exaggeration);
				wwd.getSceneController().setVerticalExaggeration(exaggeration);
				wwd.redraw();
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

		JPanel buttons = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		add(buttons, c);

		class ScaleListener implements ActionListener
		{
			private double exaggeration;

			public ScaleListener(double exaggeration)
			{
				this.exaggeration = exaggeration;
			}

			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(exaggerationToSlider(exaggeration));
				setter.set(exaggeration);
			}
		}

		JButton button = new JButton("1");
		button.addActionListener(new ScaleListener(1d));
		size = button.getMinimumSize();
		size.width = 0;
		button.setMinimumSize(size);
		button.setToolTipText("1:1");
		buttons.add(button);
		button = new JButton("2");
		button.addActionListener(new ScaleListener(2d));
		button.setMinimumSize(size);
		button.setToolTipText("2:1");
		buttons.add(button);
		button = new JButton("3");
		button.addActionListener(new ScaleListener(3d));
		button.setMinimumSize(size);
		button.setToolTipText("3:1");
		buttons.add(button);
		button = new JButton("5");
		button.addActionListener(new ScaleListener(5d));
		button.setMinimumSize(size);
		button.setToolTipText("5:1");
		buttons.add(button);
		button = new JButton("10");
		button.addActionListener(new ScaleListener(10d));
		button.setMinimumSize(size);
		button.setToolTipText("10:1");
		buttons.add(button);
		/*button = new JButton("100:1");
		button.addActionListener(new ScaleListener(100d));
		button.setMinimumSize(size);
		button.setToolTipText("100:1");
		buttons.add(button);*/

		/*final JCheckBox useTerrain = new JCheckBox("Use GA terrain");
		useTerrain.setSelected(Settings.get().isUseTerrain());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		add(useTerrain, c);

		useTerrain.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.get().setUseTerrain(useTerrain.isSelected());
				LayerList layers = wwd.getModel().getLayers();
				Model model = new BasicModel();
				model.setLayers(layers);
				wwd.setModel(model);
				listener.stateChanged(null);
				listener.stateChanged(null);
			}
		});*/
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
}
