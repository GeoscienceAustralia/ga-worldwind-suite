/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package panels.other;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import util.FlatJButton;
import util.Icons;
import util.Util;

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
		
		JLabel label = new JLabel();
		label.setText("Enter lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		add(label, c);

		coordInput = new JTextField(10);
		coordInput.setToolTipText("Type coordinates and press Enter");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		add(coordInput, c);

		FlatJButton go = new FlatJButton(Icons.run);
		go.restrictSize();
		go.setToolTipText("Go");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		add(go, c);

		resultLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
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
		LatLon latLon = Util.computeLatLonFromString(coordInput.getText(), wwd
				.getModel().getGlobe());
		updateResult(latLon, false);
	}

	private void updateResult(LatLon latLon, boolean showInvalid)
	{
		if (latLon != null)
		{
			//coordInput.setText(coordInput.getText().toUpperCase());
			resultLabel.setText(String
					.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0", latLon
							.getLatitude().degrees,
							latLon.getLongitude().degrees));
		}
		else if(showInvalid)
			resultLabel.setText("Invalid coordinates");
		else
			resultLabel.setText(" ");
	}

	private void gotoCoords()
	{
		LatLon latLon = Util.computeLatLonFromString(coordInput.getText(), wwd
				.getModel().getGlobe());
		updateResult(latLon, true);
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
}
