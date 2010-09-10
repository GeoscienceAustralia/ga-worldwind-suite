/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */
package au.gov.ga.worldwind.viewer.panels.other;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.common.ui.FlatJButton;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

public class GoToCoordinatePanel extends JPanel
{
	private WorldWindow wwd;
	private JTextField coordInput;
	private JLabel resultLabel;
	private boolean inputValid = false;
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public GoToCoordinatePanel(WorldWindow wwd)
	{
		this(wwd, false);
	}

	private GoToCoordinatePanel(WorldWindow wwd, boolean inDialog)
	{
		super(new GridBagLayout());
		this.wwd = wwd;
		makePanel(inDialog);
	}

	private void makePanel(boolean inDialog)
	{
		GridBagConstraints c;

		JLabel label = new JLabel();
		label.setText("Enter lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		add(label, c);

		coordInput = new JTextField(30);
		coordInput.setToolTipText("Type coordinates and press Enter");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		add(coordInput, c);

		if (!inDialog)
		{
			FlatJButton go = new FlatJButton(Icons.run.getIcon());
			go.restrictSize();
			go.setToolTipText("Go");
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			add(go, c);

			ActionListener al = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					gotoCoords();
				}
			};
			go.addActionListener(al);
			coordInput.addActionListener(al);
		}

		resultLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		add(resultLabel, c);

		coordInput.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateResult();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateResult();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateResult();
			}
		});

		updateResult();
	}

	private void updateResult()
	{
		LatLon latLon =
				Util.computeLatLonFromString(coordInput.getText(), wwd.getModel().getGlobe());
		updateResult(latLon, false);
	}

	private void updateResult(LatLon latlon, boolean showInvalid)
	{
		inputValid = latlon != null;
		if (latlon != null)
		{
			// coordInput.setText(coordInput.getText().toUpperCase());
			resultLabel.setText(String.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0",
					latlon.getLatitude().degrees, latlon.getLongitude().degrees));
		}
		else if (showInvalid)
			resultLabel.setText("Invalid coordinates");
		else
			resultLabel.setText(" ");

		notifyChangeListeners();
	}

	private void gotoCoords()
	{
		LatLon latLon =
				Util.computeLatLonFromString(coordInput.getText(), wwd.getModel().getGlobe());
		updateResult(latLon, true);
		if (latLon != null)
		{
			OrbitView view = (OrbitView) wwd.getView();

			Position beginCenter = view.getCenterPosition();
			Position center = new Position(latLon, 0);
			long lengthMillis = SettingsUtil.getScaledLengthMillis(beginCenter, center);

			view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(view, beginCenter,
					center, view.getHeading(), view.getHeading(), view.getPitch(), view.getPitch(),
					view.getZoom(), view.getZoom(), lengthMillis, true));
			wwd.redraw();
		}
	}

	private void addChangeListener(ChangeListener listener)
	{
		listeners.add(listener);
	}

	private void notifyChangeListeners()
	{
		for (ChangeListener listener : listeners)
		{
			listener.stateChanged(null);
		}
	}

	public static void showGotoDialog(Frame owner, WorldWindow wwd, String title, ImageIcon icon)
	{
		final JDialog dialog = new JDialog(owner, title, true);
		dialog.setIconImage(icon.getImage());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel(new GridBagLayout());
		dialog.add(panel);
		GridBagConstraints c;

		int SPACING = 10;

		final GoToCoordinatePanel gtp = new GoToCoordinatePanel(wwd, true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(gtp, c);

		final JButton ok = new JButton("OK");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 1;
		c.insets = new Insets(SPACING, SPACING, SPACING, 0);
		panel.add(ok, c);
		ok.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (gtp.inputValid)
				{
					gtp.gotoCoords();
					dialog.dispose();
				}
			}
		});

		JButton cancel = new JButton("Cancel");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		panel.add(cancel, c);
		Action cancelAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		};
		cancel.addActionListener(cancelAction);

		ChangeListener cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				ok.setEnabled(gtp.inputValid);
			}
		};
		gtp.addChangeListener(cl);
		cl.stateChanged(null);

		JRootPane rootPane = dialog.getRootPane();
		ok.setDefaultCapable(true);
		rootPane.setDefaultButton(ok);

		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", cancelAction);

		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
	}
}
