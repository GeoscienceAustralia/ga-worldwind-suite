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
package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.common.ui.FlatJButton;
import au.gov.ga.worldwind.common.ui.JDoubleField;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.Util;

/**
 * Editor used to edit {@link Place}s. Displayed in a {@link JDialog}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlaceEditor
{
	public static String IMPERIAL = "Imperial";
	public static String METRIC = "Metric";
	private static String UNITS = METRIC;

	private JDialog dialog;
	private PlacesPanel placesPanel;

	private Place place;
	private JCheckBox visibleCheck;
	private JTextArea labelText;
	private JTextField latlonField;
	private JLabel latlonLabel;
	private JDoubleField minZoomField;
	private JDoubleField maxZoomField;
	private JComboBox minZoomUnitsCombo;
	private JComboBox maxZoomUnitsCombo;
	private JCheckBox cameraInformationCheck;
	private JButton copyCameraButton;
	private JCheckBox excludeFromPlaylistCheck;
	private JLabel eyePositionLabel;
	private JTextField eyePositionField;
	private JLabel calculatedEyePositionLabel;
	private JLabel upVectorLabel;
	private JTextField upVectorField;
	private JLabel layersLabel;
	private JButton saveLayersButton;
	private JButton clearLayersButton;
	private JCheckBox verticalExaggerationCheck;
	private JButton copyExaggerationButton;
	private JLabel verticalExaggerationLabel;
	private JDoubleField verticalExaggerationField;

	private WorldWindow wwd;
	private int returnValue = JOptionPane.CANCEL_OPTION;
	private JButton okButton;

	private enum Units
	{
		Kilometres("km", 1d / 1000d),
		Metres("m", 1d),
		Miles("mi", Util.METER_TO_MILE),
		Feet("ft", Util.METER_TO_FEET);

		private final String label;
		private final double scale;

		private Units(String label, double scale)
		{
			this.label = label;
			this.scale = scale;
		}

		public double getScale()
		{
			return scale;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	/**
	 * Edit the given place with a new editor.
	 * 
	 * @param place
	 *            Place to edit
	 * @param panel
	 *            Parent panel
	 * @param title
	 *            Title of the dialog
	 * @return Return value from the dialog ({@link JOptionPane#OK_OPTION} or
	 *         {@link JOptionPane#CANCEL_OPTION})
	 */
	public static int edit(Place place, PlacesPanel panel, String title)
	{
		PlaceEditor pe = new PlaceEditor(place, panel, title);
		return pe.getOkCancel();
	}

	private int getOkCancel()
	{
		labelText.requestFocusInWindow();
		dialog.setVisible(true);
		checkValidity();
		dialog.dispose();
		return returnValue;
	}

	private PlaceEditor(final Place place, PlacesPanel placesPanel, String title)
	{
		this.placesPanel = placesPanel;
		this.wwd = placesPanel.getWwd();
		this.place = place;
		final JFrame frame = placesPanel.getFrame();

		//needs to be modal, so that getOkCancel blocks until disposed
		dialog = new JDialog(frame, title, true);
		dialog.setLayout(new BorderLayout());
		dialog.setIconImage(placesPanel.getIcon().getImage());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		Insets insets = new Insets(3, 1, 3, 1);
		GridBagConstraints c;
		JLabel label;
		JPanel panel, panel2, panel3;

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialog.add(panel, BorderLayout.CENTER);

		DocumentListener dl = new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				checkValidity();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				checkValidity();
			}
		};
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				checkValidity();
			}
		};

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Place"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(panel2, c);

		label = new JLabel("Label:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		labelText = new JTextArea(place.getLabel(), 3, 30);
		labelText.setFont(Font.decode(""));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = (Insets) insets.clone();
		JScrollPane scrollPane =
				new JScrollPane(labelText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel2.add(scrollPane, c);

		label = new JLabel("Lat/Lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(label, c);

		latlonField = new JTextField(textFormattedLatLon(place.getLatLon()));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(latlonField, c);

		latlonLabel = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		c.insets.top = 0;
		panel2.add(latlonLabel, c);

		panel3 = new JPanel(new GridLayout(0, 2));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(panel3, c);

		visibleCheck = new JCheckBox("Show on globe", place.isVisible());
		panel3.add(visibleCheck);
		visibleCheck.addActionListener(al);

		excludeFromPlaylistCheck = new JCheckBox("Exclude from playlist", place.isExcludeFromPlaylist());
		panel3.add(excludeFromPlaylistCheck);
		excludeFromPlaylistCheck.addActionListener(al);

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Fade"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		label = new JLabel("Fade in zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(panel3, c);

		Double minz = Double.valueOf(place.getMinZoom());
		if (minz < 0)
			minz = null;
		minZoomField = new JDoubleField(minz, 2);
		minZoomField.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel3.add(minZoomField, c);

		minZoomUnitsCombo = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			minZoomUnitsCombo.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(minZoomUnitsCombo, c);

		ActionListener mzal = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				minZoomField.setScale(((Units) minZoomUnitsCombo.getSelectedItem()).getScale());
			}
		};
		minZoomUnitsCombo.addActionListener(mzal);
		mzal.actionPerformed(null);

		FlatJButton flat = new FlatJButton(Icons.remove.getIcon());
		flat.restrictSize();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(flat, c);
		flat.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				minZoomField.setValue(null);
			}
		});

		label = new JLabel("Fade out zoom:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(label, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(panel3, c);

		Double maxz = Double.valueOf(place.getMaxZoom());
		if (maxz < 0)
			maxz = null;
		maxZoomField = new JDoubleField(maxz, 2);
		maxZoomField.setPositive(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel3.add(maxZoomField, c);

		maxZoomUnitsCombo = new JComboBox(Units.values());
		if (UNITS == IMPERIAL)
			maxZoomUnitsCombo.setSelectedItem(Units.Miles);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(maxZoomUnitsCombo, c);

		mzal = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				maxZoomField.setScale(((Units) maxZoomUnitsCombo.getSelectedItem()).getScale());
			}
		};
		maxZoomUnitsCombo.addActionListener(mzal);
		mzal.actionPerformed(null);

		flat = new FlatJButton(Icons.remove.getIcon());
		flat.restrictSize();
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		panel3.add(flat, c);
		flat.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				maxZoomField.setValue(null);
			}
		});

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Camera"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		panel3 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.gridwidth = 2;
		panel2.add(panel3, c);

		cameraInformationCheck = new JCheckBox("Save camera information", place.isSaveCamera());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5;
		panel3.add(cameraInformationCheck, c);
		cameraInformationCheck.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableCameraInformation();
				checkValidity();
			}
		});

		copyCameraButton = new JButton("Fill from current view");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		panel3.add(copyCameraButton, c);
		copyCameraButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				View view = wwd.getView();
				Vec4 eyePoint = view.getCurrentEyePoint();
				Vec4 up = view.getUpVector();
				Position eyePos = view.getGlobe().computePositionFromPoint(eyePoint);
				Position centerPosition = Util.computeViewClosestCenterPosition(view, eyePoint);

				eyePositionField.setText(textFormattedPosition(eyePos));
				upVectorField.setText(textFormattedVec4(up));

				if (LatLon.greatCircleDistance(centerPosition, place.getLatLon()).degrees > 0.1)
				{
					int value =
							JOptionPane.showConfirmDialog(dialog,
									"Do you want to change the Lat/Lon to match the current camera center?",
									"Move place", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (value == JOptionPane.YES_OPTION)
					{
						latlonField.setText(textFormattedLatLon(centerPosition));
					}
				}
				checkValidity();
			}
		});

		eyePositionLabel = new JLabel("Eye position:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(eyePositionLabel, c);

		eyePositionField = new JTextField(textFormattedPosition(place.getEyePosition()));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(eyePositionField, c);

		calculatedEyePositionLabel = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		c.insets.top = 0;
		panel2.add(calculatedEyePositionLabel, c);

		upVectorLabel = new JLabel("Up vector:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = (Insets) insets.clone();
		panel2.add(upVectorLabel, c);

		upVectorField = new JTextField(textFormattedVec4(place.getUpVector()));
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = (Insets) insets.clone();
		panel2.add(upVectorField, c);

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Layers"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		layersLabel = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridy = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(layersLabel, c);

		saveLayersButton = new JButton("Save current layers state");
		c = new GridBagConstraints();
		c.gridy = 1;
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(saveLayersButton, c);
		saveLayersButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveLayers();
			}
		});

		clearLayersButton = new JButton("Clear saved layers state");
		c = new GridBagConstraints();
		c.gridy = 2;
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		panel2.add(clearLayersButton, c);
		clearLayersButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				clearLayers();
			}
		});

		panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(BorderFactory.createTitledBorder("Vertical exaggeration"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		verticalExaggerationCheck = new JCheckBox("Save vertical exaggeration");
		verticalExaggerationCheck.setSelected(place.getVerticalExaggeration() != null);
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(verticalExaggerationCheck, c);
		verticalExaggerationCheck.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enableExaggerationComponents();
				checkValidity();
			}
		});

		copyExaggerationButton = new JButton("Fill from current value");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		panel2.add(copyExaggerationButton, c);
		copyExaggerationButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				verticalExaggerationField.setValue(wwd.getSceneController().getVerticalExaggeration());
			}
		});

		verticalExaggerationLabel = new JLabel("Exaggeration:");
		c = new GridBagConstraints();
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(verticalExaggerationLabel, c);

		verticalExaggerationField =
				new JDoubleField(place.getVerticalExaggeration() != null ? place.getVerticalExaggeration() : wwd
						.getSceneController().getVerticalExaggeration(), 3);
		verticalExaggerationField.setPositive(true);
		verticalExaggerationField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridy = 1;
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = (Insets) insets.clone();
		c.insets.bottom = 0;
		panel2.add(verticalExaggerationField, c);


		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		dialog.add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				returnValue = JOptionPane.OK_OPTION;
				dialog.dispose();
			}
		});
		okButton.setDefaultCapable(true);
		dialog.getRootPane().setDefaultButton(okButton);

		JButton button = new JButton("Cancel");
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		checkValidity();
		enableCameraInformation();
		enableLayersComponents();
		enableExaggerationComponents();

		labelText.getDocument().addDocumentListener(dl);
		latlonField.getDocument().addDocumentListener(dl);
		minZoomField.getDocument().addDocumentListener(dl);
		maxZoomField.getDocument().addDocumentListener(dl);
		eyePositionField.getDocument().addDocumentListener(dl);
		upVectorField.getDocument().addDocumentListener(dl);
	}

	private boolean checkValidity()
	{
		boolean valid = true;

		place.setVisible(visibleCheck.isSelected());

		String label = labelText.getText();
		if (label.length() == 0)
			valid = false;
		else
			place.setLabel(label);

		LatLon ll = parseLatLon(latlonField.getText());
		if (ll == null)
		{
			valid = false;
			latlonLabel.setText("Invalid coordinates");
		}
		else
		{
			place.setLatLon(ll);
			latlonLabel.setText(labelFormattedLatLon(ll));
		}

		Double minz = minZoomField.getValue();
		Double maxz = maxZoomField.getValue();
		if (minz != null && maxz != null && maxz > minz)
		{
			Double temp = maxz;
			maxz = minz;
			minz = temp;
		}
		if (minz == null)
			minz = -1d;
		if (maxz == null)
			maxz = -1d;

		place.setMinZoom(minz);
		place.setMaxZoom(maxz);

		Position pos = parsePosition(eyePositionField.getText());
		if (pos == null)
		{
			valid = false;
			calculatedEyePositionLabel.setText("Invalid position");
		}
		else
		{
			place.setEyePosition(pos);
			calculatedEyePositionLabel.setText(labelFormattedPosition(pos));
		}

		Vec4 v = parseVec4(upVectorField.getText());
		place.setUpVector(v);

		place.setSaveCamera(cameraInformationCheck.isSelected());
		place.setExcludeFromPlaylist(excludeFromPlaylistCheck.isSelected());

		Double exaggeration = verticalExaggerationField.getValue();
		if (verticalExaggerationCheck.isSelected() && (exaggeration == null || exaggeration < 0 || exaggeration > 100))
		{
			valid = false;
		}
		else
		{
			place.setVerticalExaggeration(verticalExaggerationCheck.isSelected() ? exaggeration : null);
		}

		okButton.setEnabled(valid);
		return valid;
	}

	private void enableCameraInformation()
	{
		boolean enabled = cameraInformationCheck.isSelected();
		copyCameraButton.setEnabled(enabled);
		calculatedEyePositionLabel.setEnabled(enabled);
		eyePositionField.setEnabled(enabled);
		eyePositionLabel.setEnabled(enabled);
		upVectorField.setEnabled(enabled);
		upVectorLabel.setEnabled(enabled);
	}

	private void enableExaggerationComponents()
	{
		boolean enabled = verticalExaggerationCheck.isSelected();
		copyExaggerationButton.setEnabled(enabled);
		verticalExaggerationField.setEnabled(enabled);
		verticalExaggerationLabel.setEnabled(enabled);
	}

	private String textFormattedLatLon(LatLon latlon)
	{
		if (latlon == null)
			return "";

		return String.format("%1.4f %1.4f", latlon.latitude.degrees, latlon.longitude.degrees);
	}

	private String labelFormattedLatLon(LatLon latlon)
	{
		if (latlon == null)
			return "";

		return String.format("Lat %1.4f\u00B0 Lon %1.4f\u00B0", latlon.latitude.degrees, latlon.longitude.degrees);
	}

	private LatLon parseLatLon(String text)
	{
		return Util.computeLatLonFromString(text, wwd.getModel().getGlobe());
	}

	private String textFormattedPosition(Position position)
	{
		if (position == null)
			return "";

		return String.format("%1.4f %1.4f %1.0f", position.latitude.degrees, position.longitude.degrees,
				position.elevation);
	}

	private String labelFormattedPosition(Position position)
	{
		if (position == null)
			return "";

		return String.format("Lat %1.4f\u00B0 Lon %1.4f\u00B0 Elev %1.0fm", position.latitude.degrees,
				position.longitude.degrees, position.elevation);
	}

	private Position parsePosition(String text)
	{
		return Util.computePositionFromString(text, wwd.getModel().getGlobe());
	}

	private String textFormattedVec4(Vec4 v)
	{
		if (v == null)
			return "";

		String text = String.format("%1.4f %1.4f %1.4f", v.x, v.y, v.z);
		if (v.w != 1)
		{
			text += String.format(" %1.4f", v.w);
		}
		return text;
	}

	private Vec4 parseVec4(String text)
	{
		return Util.computeVec4FromString(text);
	}

	private void enableLayersComponents()
	{
		boolean layersSaved = place.getLayers() != null;
		String label = layersSaved ? "Layers state saved" : "No layers state saved";
		layersLabel.setText(label);
		clearLayersButton.setEnabled(layersSaved);
	}

	private void saveLayers()
	{
		placesPanel.setLayersFromLayersPanel(place);
		enableLayersComponents();
	}

	private void clearLayers()
	{
		place.setLayers(null);
		enableLayersComponents();
	}

	public static void setUnits(String units)
	{
		UNITS = units;
	}
}
