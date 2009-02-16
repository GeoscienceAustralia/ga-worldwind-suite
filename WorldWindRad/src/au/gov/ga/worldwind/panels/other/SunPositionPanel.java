package au.gov.ga.worldwind.panels.other;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.Layer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.layers.shader.NightLightsLayer;
import au.gov.ga.worldwind.layers.shader.SunPositionProvider;
import au.gov.ga.worldwind.util.SunCalculator;


public class SunPositionPanel extends JPanel implements SunPositionProvider
{
	private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

	private Calendar currentTime = Calendar.getInstance();
	private Calendar lastTime = Calendar.getInstance();
	private DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
	private JSpinner spinner;
	private JRadioButton currentRadio, customRadio;
	private LatLon lastPosition;
	private WorldWindow wwd;

	public SunPositionPanel(WorldWindow wwd)
	{
		this.wwd = wwd;
		setLayout(new GridBagLayout());
		GridBagConstraints c;

		currentRadio = new JRadioButton();
		currentRadio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		add(currentRadio, c);

		customRadio = new JRadioButton();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		add(customRadio, c);

		ButtonGroup bg = new ButtonGroup();
		bg.add(currentRadio);
		bg.add(customRadio);

		Calendar calendar = Calendar.getInstance();
		Date initTime = calendar.getTime();
		calendar.add(Calendar.YEAR, -100);
		Date earliestTime = calendar.getTime();
		calendar.add(Calendar.YEAR, 200);
		Date latestTime = calendar.getTime();
		SpinnerDateModel spm = new SpinnerDateModel(initTime, earliestTime,
				latestTime, Calendar.YEAR);
		spinner = new JSpinner(spm);
		spinner.setEditor(new JSpinner.DateEditor(spinner, DATE_FORMAT));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		add(spinner, c);
		spinner.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				customRadio.setSelected(true);
			}
		});

		ChangeListener cl = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				updateCustom();
			}
		};
		currentRadio.addChangeListener(cl);
		customRadio.addChangeListener(cl);
		spinner.addChangeListener(cl);

		Thread updateThread = new Thread()
		{
			public void run()
			{
				while (true)
				{
					updateCurrent();

					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		updateThread.setDaemon(true);
		updateThread.start();
		
		Layer layer = new NightLightsLayer(this);
		wwd.getModel().getLayers().add(layer);
	}

	public void updateCustom()
	{
		spinner.setEnabled(customRadio.isSelected());
		if (customRadio.isSelected())
		{
			setTime((Date) spinner.getValue());
			wwd.redraw();
		}
	}

	public void updateCurrent()
	{
		currentTime.setTimeInMillis(System.currentTimeMillis());
		Date t = currentTime.getTime();
		currentRadio.setText("Current time: " + formatter.format(t));
		if (currentRadio.isSelected())
		{
			spinner.setValue(t);
			setTime(t);
		}
	}

	private void setTime(Date time)
	{
		synchronized (lastTime)
		{
			lastTime.setTime(time);
			lastPosition = SunCalculator.subsolarPoint(lastTime);
		}
	}

	public LatLon getPosition()
	{
		return lastPosition;
	}
}
