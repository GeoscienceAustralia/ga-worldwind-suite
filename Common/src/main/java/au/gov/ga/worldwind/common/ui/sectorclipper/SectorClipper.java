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
package au.gov.ga.worldwind.common.ui.sectorclipper;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.SurfaceSector;
import gov.nasa.worldwindx.examples.util.SectorSelector;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.common.render.ExtendedSceneController;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.util.Util;

/**
 * Dialog used to edit the sector clipping planes of the
 * {@link ExtendedSceneController}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SectorClipper
{
	//TODO fix sector clipping over the longitude == -180/180 line
	
	public static void beginSelection(Frame frame, String title, WorldWindow wwd, BasicAction clipAction,
			BasicAction clearAction)
	{
		new SectorClipper(frame, title, wwd, clipAction, clearAction);
	}

	private MySectorSelector selector;
	private JDialog dialog;
	private JButton okButton;

	private JTextField toprightField;
	private JTextField bottomleftField;

	private WorldWindow wwd;
	private Sector sector;
	private boolean settingSector = false;

	private SectorClipper(final Frame frame, String title, final WorldWindow wwd, final BasicAction clipAction,
			final BasicAction clearAction)
	{
		this.wwd = wwd;
		selector = new MySectorSelector(wwd);
		dialog = new JDialog(frame, title, false);

		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				close();
			}
		});

		DocumentListener sectorDl = new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateSector();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateSector();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateSector();
			}
		};
		selector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				setSector(selector.getSector(), true);
			}
		});

		dialog.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new GridBagLayout());
		dialog.add(mainPanel, BorderLayout.CENTER);


		final int s = 5;
		int i = 0;
		JPanel panel;
		GridBagConstraints c;
		JLabel label;

		i = 0;
		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Extents"));
		c = new GridBagConstraints();
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		mainPanel.add(panel, c);

		label = new JLabel("Please select sector in globe view");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i++;
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = 2;
		panel.add(label, c);


		label = new JLabel("Bottom left lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		bottomleftField = new JTextField();
		bottomleftField.getDocument().addDocumentListener(sectorDl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1;
		c.gridy = i++;
		c.insets = new Insets(s, s, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(bottomleftField, c);

		label = new JLabel("Top right lat/lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, s, 0);
		panel.add(label, c);

		toprightField = new JTextField();
		toprightField.getDocument().addDocumentListener(sectorDl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.insets = new Insets(s, s, s, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(toprightField, c);


		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		dialog.add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("Clip");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				clipSector();
				clearAction.setEnabled(true);
				clipAction.setEnabled(false);
				close();
			}
		});
		okButton.setDefaultCapable(true);
		dialog.getRootPane().setDefaultButton(okButton);
		okButton.setEnabled(false);

		JButton button = new JButton("Cancel");
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				sector = null;
				clipSector();
				close();
			}
		});

		selector.enable();
		dialog.pack();
		Dimension size = dialog.getSize();
		size.width = 400;
		dialog.setSize(size);
		dialog.setLocationRelativeTo(frame);
		Point location = dialog.getLocation();
		location.y = frame.getLocation().y;
		dialog.setLocation(location);
		dialog.setVisible(true);
	}

	private void close()
	{
		selector.disable();
		dialog.dispose();
	}

	private void updateSector()
	{
		setSector(fromFields(), false);
	}

	private Sector fromFields()
	{
		LatLon bottomleft = Util.computeLatLonFromString(bottomleftField.getText());
		LatLon topright = Util.computeLatLonFromString(toprightField.getText());

		if (bottomleft == null || topright == null)
		{
			return Sector.EMPTY_SECTOR;
		}

		return new Sector(bottomleft.getLatitude(), topright.getLatitude(), bottomleft.getLongitude(),
				topright.getLongitude());
	}

	private void setSector(Sector sector, boolean fromGlobe)
	{
		if (sector == null)
		{
			this.sector = sector;
			bottomleftField.setText("");
			toprightField.setText("");
			validate();
			selector.disable();
			selector.enable();
			return;
		}

		if (!settingSector)
		{
			settingSector = true;

			boolean modified = false;
			if (sector.getDeltaLatDegrees() < 0)
			{
				sector =
						new Sector(sector.getMaxLatitude(), sector.getMinLatitude(), sector.getMinLongitude(),
								sector.getMaxLongitude());
				modified = true;
			}
			if (sector.getDeltaLonDegrees() < 0)
			{
				sector =
						new Sector(sector.getMinLatitude(), sector.getMaxLatitude(), sector.getMaxLongitude(),
								sector.getMinLongitude());
				modified = true;
			}

			this.sector = sector;

			if (fromGlobe | modified)
			{
				bottomleftField.setText(sector.getMinLatitude().degrees + " " + sector.getMinLongitude().degrees);
				toprightField.setText(sector.getMaxLatitude().degrees + " " + sector.getMaxLongitude().degrees);
			}
			if (!fromGlobe | modified)
			{
				selector.setSector(sector);
			}
			validate();

			settingSector = false;
		}
	}

	private void validate()
	{
		boolean valid = sector != null && !sector.equals(Sector.EMPTY_SECTOR);
		okButton.setEnabled(valid);

		if (valid)
		{
			clipSector();
		}
	}

	private void clipSector()
	{
		if (!(wwd.getSceneController() instanceof ExtendedSceneController))
		{
			throw new IllegalStateException("SceneController not an instance of "
					+ ExtendedSceneController.class.getName());
		}

		final ExtendedSceneController sceneController = (ExtendedSceneController) wwd.getSceneController();
		if (sector == null)
		{
			sceneController.clearClipping();
		}
		else
		{
			sceneController.clipSector(sector);
		}
		wwd.redraw();
	}

	protected static class MySectorSelector extends SectorSelector
	{
		private WorldWindow wwd;

		public MySectorSelector(WorldWindow wwd)
		{
			super(wwd);
			this.wwd = wwd;
			getShape().setPathType(AVKey.GREAT_CIRCLE);
			getBorder().setPathType(AVKey.GREAT_CIRCLE);
		}

		public void setSector(Sector sector)
		{
			getShape().setSector(sector);
			wwd.redraw();
		}

		@Override
		public void disable()
		{
			super.disable();
			setCursor(null);
		}

		@Override
		protected int determineAdjustmentSide(Movable dragObject, double factor)
		{
			//overridden superclass' method to fix for great circle sectors

			if (dragObject instanceof SurfaceSector)
			{
				Position p = this.getWwd().getCurrentPosition();
				if (p == null)
				{
					return NONE;
				}

				Sector s = ((SurfaceSector) dragObject).getSector();

				double pLat = (p.getLatitude().degrees - s.getMinLatitude().degrees) / s.getDeltaLatDegrees();
				double pLon = (p.getLongitude().degrees - s.getMinLongitude().degrees) / s.getDeltaLonDegrees();

				LatLon[] corners = s.getCorners(); //SW, SE, NE, NW
				LatLon west = LatLon.interpolateGreatCircle(pLat, corners[0], corners[3]); //SW, NW
				LatLon east = LatLon.interpolateGreatCircle(pLat, corners[1], corners[2]); //SE, NE
				LatLon south = LatLon.interpolateGreatCircle(pLon, corners[0], corners[1]); //SW, SE
				LatLon north = LatLon.interpolateGreatCircle(pLon, corners[3], corners[2]); //NW, NE

				double dN = Math.abs(north.latitude.subtract(p.latitude).degrees);
				double dS = Math.abs(south.latitude.subtract(p.latitude).degrees);
				double dW = Math.abs(west.longitude.subtract(p.longitude).degrees);
				double dE = Math.abs(east.longitude.subtract(p.longitude).degrees);

				double sLat = factor * s.getDeltaLatDegrees();
				double sLon = factor * s.getDeltaLonDegrees();

				if (dN < sLat && dW < sLon)
					return NORTHWEST;
				if (dN < sLat && dE < sLon)
					return NORTHEAST;
				if (dS < sLat && dW < sLon)
					return SOUTHWEST;
				if (dS < sLat && dE < sLon)
					return SOUTHEAST;
				if (dN < sLat)
					return NORTH;
				if (dS < sLat)
					return SOUTH;
				if (dW < sLon)
					return WEST;
				if (dE < sLon)
					return EAST;
			}

			return NONE;
		}

		@Override
		protected void setCursor(int sideName)
		{
			if (sideName == NONE)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				return;
			}

			List<Integer> order = Arrays.asList(NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST);
			int index = order.indexOf(sideName);
			if (index >= 0)
			{
				Angle heading = wwd.getView().getHeading();
				int eighthRotation = getEighth(heading);
				index = (index + eighthRotation) % order.size();
				sideName = order.get(index);
			}

			super.setCursor(sideName);
		}

		/**
		 * Calculate which eighth of the circle the angle lies in, clockwise.
		 * Eg, if the angle is between -22.5 and 22.5, it lies within the top
		 * eighth (0).
		 * <p/>
		 * Values returned:
		 * <ul>
		 * <li>22.5 to -22.5 = 0</li>
		 * <li>-22.5 to -67.5 = 1</li>
		 * <li>-67.5 to -112.5 = 2</li>
		 * <li>-112.5 to -157.5 = 3</li>
		 * <li>-157.5 to 157.5 = 4</li>
		 * <li>157.5 to 112.5 = 5</li>
		 * <li>112.5 to 67.5 = 6</li>
		 * <li>67.5 to 22.5 = 7</li>
		 * </ul>
		 * 
		 * @param angle
		 * @return
		 */
		protected int getEighth(Angle angle)
		{
			double degrees = angle.degrees;
			degrees -= 22.5;
			while (degrees < 0)
			{
				degrees += 360.0;
			}
			degrees %= 360.0;
			return 7 - (int) (degrees / 45.0);
		}

		protected SurfaceSector getBorder()
		{
			//access the border object using reflection, as it's protected in the superclass
			try
			{
				Method method = SectorSelector.RegionShape.class.getDeclaredMethod("getBorder");
				method.setAccessible(true);
				return (SurfaceSector) method.invoke(getShape());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
}
