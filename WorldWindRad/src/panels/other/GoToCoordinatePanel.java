/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package panels.other;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import util.FlatJButton;
import util.Icons;

/**
 * This panel let the user input different coordinates and displays the
 * corresponding latitude and longitude in decimal degrees.
 * <p>
 * Supported format are:
 * <ul>
 * <li>MGRS strings with or without separting spaces.</li>
 * <li>Decimal degrees with sign prefix or N, S, E, W suffix.</li>
 * <li>Degrees, minutes and seconds with sign prefix or N, S, E, W suffix.</li>
 * </ul>
 * The separator between lat/lon pairs can be ',', ', ' or any number of spaces.
 * </p>
 * <p>
 * Examples:
 * 
 * <pre>
 * 11sku528111
 * 11S KU 528 111
 * 
 * 45N 123W
 * +45.1234, -123.12
 * 45.1234N 123.12W
 * 
 * 45° 30' 00&quot;N, 50° 30'W
 * 45°30' -50°30'
 * 45 30 N 50 30 W
 * </pre>
 * 
 * </p>
 * 
 * @author Patrick Murris
 * @version $Id: GoToCoordinatePanel.java 5175 2008-04-25 21:12:21Z
 *          patrickmurris $
 */

public class GoToCoordinatePanel extends JPanel
{
	private WorldWindow wwd;
	private JTextField coordInput;
	private JLabel resultLabel;

	public GoToCoordinatePanel(WorldWindow wwd)
	{
		super(new GridBagLayout());
		this.wwd = wwd;
		makePanel();
	}

	private void makePanel()
	{
		GridBagConstraints c;

		coordInput = new JTextField(10);
		coordInput.setToolTipText("Type coordinates and press Enter");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		add(coordInput, c);

		FlatJButton go = new FlatJButton(Icons.run);
		go.restrictSize();
		go.setToolTipText("Go");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		add(go, c);

		resultLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		add(resultLabel, c);

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				gotoCoords();
			}
		};
		go.addActionListener(al);
		coordInput.addActionListener(al);

		coordInput.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				updateResult();
			}

			public void insertUpdate(DocumentEvent e)
			{
				updateResult();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateResult();
			}
		});
		
		updateResult();
	}

	private void updateResult()
	{
		LatLon latLon = computeLatLonFromString(coordInput.getText(), wwd
				.getModel().getGlobe());
		updateResult(latLon);
	}

	private void updateResult(LatLon latLon)
	{
		if (latLon != null)
		{
			//coordInput.setText(coordInput.getText().toUpperCase());
			resultLabel.setText(String
					.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0", latLon
							.getLatitude().degrees,
							latLon.getLongitude().degrees));
		}
		else
			resultLabel.setText("Invalid coordinates");
	}

	private void gotoCoords()
	{
		LatLon latLon = computeLatLonFromString(coordInput.getText(), wwd
				.getModel().getGlobe());
		updateResult(latLon);
		if (latLon != null)
		{
			OrbitView view = (OrbitView) wwd.getView();
			Globe globe = wwd.getModel().getGlobe();
			view
					.applyStateIterator(FlyToOrbitViewStateIterator
							.createPanToIterator(view, globe, new Position(
									latLon, 0), view.getHeading(), view
									.getPitch(), view.getZoom()));
		}
	}

	/**
	 * Tries to extract a latitude and a longitude from the given text string.
	 * 
	 * @param coordString
	 *            the input string.
	 * @param globe
	 *            the current <code>Globe</code>.
	 * @return the corresponding <code>LatLon</code> or <code>null</code>.
	 */
	private static LatLon computeLatLonFromString(String coordString,
			Globe globe)
	{
		if (coordString == null)
		{
			String msg = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Angle lat = null;
		Angle lon = null;
		coordString = coordString.trim();
		String regex;
		String separators = "(\\s*|,|,\\s*)";
		Pattern pattern;
		Matcher matcher;

		// Try MGRS - allow spaces
		regex = "\\d{1,2}[A-Za-z]\\s*[A-Za-z]{2}\\s*\\d{1,5}\\s*\\d{1,5}";
		if (coordString.matches(regex))
		{
			try
			{
				MGRSCoord MGRS = MGRSCoord.fromString(coordString, globe);
				// NOTE: the MGRSCoord does not always report errors with invalide strings,
				// but will have lat and lon set to zero
				if (MGRS.getLatitude().degrees != 0
						|| MGRS.getLatitude().degrees != 0)
				{
					lat = MGRS.getLatitude();
					lon = MGRS.getLongitude();
				}
				else
					return null;
			}
			catch (IllegalArgumentException e)
			{
				return null;
			}
		}

		// Try to extract a pair of signed decimal values separated by a space, ',' or ', '
		// Allow E, W, S, N sufixes
		if (lat == null || lon == null)
		{
			regex = "([-|\\+]?\\d+?(\\.\\d+?)??\\s*[N|n|S|s]??)";
			regex += separators;
			regex += "([-|\\+]?\\d+?(\\.\\d+?)??\\s*[E|e|W|w]??)";
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(coordString);
			if (matcher.matches())
			{
				String sLat = matcher.group(1).trim(); // Latitude
				int signLat = 1;
				char suffix = sLat.toUpperCase().charAt(sLat.length() - 1);
				if (!Character.isDigit(suffix))
				{
					signLat = suffix == 'N' ? 1 : -1;
					sLat = sLat.substring(0, sLat.length() - 1);
					sLat = sLat.trim();
				}

				String sLon = matcher.group(4).trim(); // Longitude
				int signLon = 1;
				suffix = sLon.toUpperCase().charAt(sLon.length() - 1);
				if (!Character.isDigit(suffix))
				{
					signLon = suffix == 'E' ? 1 : -1;
					sLon = sLon.substring(0, sLon.length() - 1);
					sLon = sLon.trim();
				}

				lat = Angle.fromDegrees(Double.parseDouble(sLat) * signLat);
				lon = Angle.fromDegrees(Double.parseDouble(sLon) * signLon);
			}
		}

		// Try to extract two degrees minute seconds blocks separated by a space, ',' or ', '
		// Allow S, N, W, E suffixes and signs.
		// eg: -123° 34' 42" +45° 12' 30"
		// eg: 123° 34' 42"S 45° 12' 30"W
		if (lat == null || lon == null)
		{
			regex = "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}['|\u2019|\\s])?(\\s*\\d{1,2}[\"|\u201d])?\\s*[N|n|S|s]?)";
			regex += separators;
			regex += "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}['|\u2019|\\s])?(\\s*\\d{1,2}[\"|\u201d])?\\s*[E|e|W|w]?)";
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(coordString);
			if (matcher.matches())
			{
				lat = parseDMSString(matcher.group(1));
				lon = parseDMSString(matcher.group(5));
			}
		}

		if (lat == null || lon == null)
			return null;

		if (lat.degrees >= -90 && lat.degrees <= 90 && lon.degrees >= -180
				&& lon.degrees <= 180)
			return new LatLon(lat, lon);

		return null;
	}

	/**
	 * Parse a Degrees, Minute, Second coordinate string.
	 * 
	 * @param dmsString
	 *            the string to parse.
	 * @return the corresponding <code>Angle</code> or null.
	 */
	private static Angle parseDMSString(String dmsString)
	{
		// Replace degree, min and sec signs with space
		dmsString = dmsString
				.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", " ");
		// Replace multiple spaces with single ones
		dmsString = dmsString.replaceAll("\\s+", " ");
		dmsString = dmsString.trim();

		// Check for sign prefix and suffix
		int sign = 1;
		char suffix = dmsString.toUpperCase().charAt(dmsString.length() - 1);
		if (!Character.isDigit(suffix))
		{
			sign = (suffix == 'N' || suffix == 'E') ? 1 : -1;
			dmsString = dmsString.substring(0, dmsString.length() - 1);
			dmsString = dmsString.trim();
		}
		char prefix = dmsString.charAt(0);
		if (!Character.isDigit(prefix))
		{
			sign *= (prefix == '-') ? -1 : 1;
			dmsString = dmsString.substring(1, dmsString.length());
		}

		// Process degrees, minutes and seconds
		String[] DMS = dmsString.split(" ");
		double d = Integer.parseInt(DMS[0]);
		double m = DMS.length > 1 ? Integer.parseInt(DMS[1]) : 0;
		double s = DMS.length > 2 ? Integer.parseInt(DMS[2]) : 0;

		if (m >= 0 && m <= 60 && s >= 0 && s <= 60)
			return Angle
					.fromDegrees(d * sign + m / 60 * sign + s / 3600 * sign);

		return null;
	}


}
