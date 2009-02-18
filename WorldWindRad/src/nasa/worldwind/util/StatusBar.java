/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.util;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * @author tag
 * @version $Id: StatusBar.java 4787 2008-03-21 22:11:20Z dcollins $
 */
public class StatusBar extends JPanel implements PositionListener,
		RenderingListener
{
	// Units constants
	public final static String UNIT_METRIC = "gov.nasa.worldwind.StatusBar.Metric";
	public final static String UNIT_IMPERIAL = "gov.nasa.worldwind.StatusBar.Imperial";
	private final static double METER_TO_FEET = 3.280839895;
	private final static double METER_TO_MILE = 0.000621371192;

	private static final int MAX_ALPHA = 254;

	private WorldWindow eventSource;
	protected final JLabel latDisplay = new JLabel("");
	protected final JLabel lonDisplay = new JLabel("Off globe");
	protected final JLabel altDisplay = new JLabel("");
	protected final JLabel eleDisplay = new JLabel("");
	private boolean showNetworkStatus = true;
	private String elevationUnit = UNIT_METRIC;
	private AtomicBoolean isNetworkAvailable = new AtomicBoolean(true);

	public StatusBar()
	{
		super(new GridLayout(1, 0));

		final JLabel heartBeat = new JLabel("Downloading");

		altDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		latDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		lonDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		eleDisplay.setHorizontalAlignment(SwingConstants.CENTER);

		//        this.add(new JLabel("")); // dummy label to visually balance with heartbeat
		this.add(altDisplay);
		this.add(latDisplay);
		this.add(lonDisplay);
		this.add(eleDisplay);
		this.add(heartBeat);

		heartBeat.setHorizontalAlignment(SwingConstants.CENTER);
		heartBeat.setForeground(new java.awt.Color(255, 0, 0, 0));

		Timer downloadTimer = new Timer(100, new ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent actionEvent)
			{
				if (!showNetworkStatus)
				{
					if (heartBeat.getText().length() > 0)
						heartBeat.setText("");
					return;
				}

				if (!isNetworkAvailable.get())
				{
					heartBeat.setText(Logging.getMessage("term.NoNetwork"));
					heartBeat.setForeground(new Color(255, 0, 0, MAX_ALPHA));
					return;
				}

				Color color = heartBeat.getForeground();
				int alpha = color.getAlpha();
				if (isNetworkAvailable.get()
						&& WorldWind.getRetrievalService().hasActiveTasks())
				{
					heartBeat.setText(Logging.getMessage("term.Downloading"));
					if (alpha >= MAX_ALPHA)
						alpha = MAX_ALPHA;
					else
						alpha = alpha < 16 ? 16 : Math.min(MAX_ALPHA,
								alpha + 20);
				}
				else
				{
					alpha = Math.max(0, alpha - 20);
				}
				heartBeat.setForeground(new Color(255, 0, 0, alpha));
			}
		});
		downloadTimer.start();

		Timer netCheckTimer = new Timer(10000, new ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent actionEvent)
			{
				if (!showNetworkStatus)
					return;

				Thread t = new Thread(new Runnable()
				{
					public void run()
					{
						isNetworkAvailable.set(!WorldWind.getNetworkStatus()
								.isNetworkUnavailable());
					}
				});
				t.start();
			}
		});
		netCheckTimer.start();
	}

	public void setEventSource(WorldWindow newEventSource)
	{
		if (this.eventSource != null)
		{
			this.eventSource.removePositionListener(this);
			this.eventSource.removeRenderingListener(this);
		}

		if (newEventSource != null)
		{
			newEventSource.addPositionListener(this);
			newEventSource.addRenderingListener(this);
		}

		this.eventSource = newEventSource;
	}

	public boolean isShowNetworkStatus()
	{
		return showNetworkStatus;
	}

	public void setShowNetworkStatus(boolean showNetworkStatus)
	{
		this.showNetworkStatus = showNetworkStatus;
	}

	public void moved(PositionEvent event)
	{
		this.handleCursorPositionChange(event);
	}

	public WorldWindow getEventSource()
	{
		return this.eventSource;
	}

	public String getElevationUnit()
	{
		return this.elevationUnit;
	}

	public void setElevationUnit(String unit)
	{
		if (unit == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.elevationUnit = unit;
	}

	protected String makeCursorElevationDescription(double metersElevation)
	{
		String s;
		if (UNIT_IMPERIAL.equals(elevationUnit))
			s = String.format("Elev %,7d feet",
					(int) (metersElevation * METER_TO_FEET));
		else
			// Default to metric units.
			s = String.format("Elev %,7d meters", (int) metersElevation);
		return s;
	}

	protected String makeEyeAltitudeDescription(double metersAltitude)
	{
		String s;
		if (UNIT_IMPERIAL.equals(elevationUnit))
			s = String.format("Altitude %,7d mi", (int) Math
					.round(metersAltitude * METER_TO_MILE));
		else
			// Default to metric units.
			s = String.format("Altitude %,7d km", (int) Math
					.round(metersAltitude / 1e3));
		return s;
	}

	private void handleCursorPositionChange(PositionEvent event)
	{
		Position newPos = event.getPosition();
		if (newPos != null)
		{
			String las = String.format("Lat %7.4f\u00B0", newPos.getLatitude()
					.getDegrees());
			String los = String.format("Lon %7.4f\u00B0", newPos.getLongitude()
					.getDegrees());
			String els = makeCursorElevationDescription(eventSource.getModel()
					.getGlobe().getElevation(newPos.getLatitude(),
							newPos.getLongitude()));
			latDisplay.setText(las);
			lonDisplay.setText(los);
			eleDisplay.setText(els);
		}
		else
		{
			latDisplay.setText("");
			lonDisplay.setText("Off globe");
			eleDisplay.setText("");
		}
	}

	public void stageChanged(RenderingEvent event)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				if (eventSource.getView() != null
						&& eventSource.getView().getEyePosition() != null)
					altDisplay.setText(makeEyeAltitudeDescription(eventSource
							.getView().getEyePosition().getElevation()));
				else
					altDisplay.setText("Altitude");
			}
		});
	}
}
