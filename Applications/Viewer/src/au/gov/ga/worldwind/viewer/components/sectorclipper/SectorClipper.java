package au.gov.ga.worldwind.viewer.components.sectorclipper;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.examples.util.SectorSelector;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

import java.awt.BorderLayout;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.stereo.StereoSceneController;

public class SectorClipper
{
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

	private Sector sector;
	private boolean settingSector = false;

	private SectorClipper(final Frame frame, String title, final WorldWindow wwd, final BasicAction clipAction,
			final BasicAction clearAction)
	{
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


		int s = 5;
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
				clipSector(frame, wwd);
				clearAction.setEnabled(true);
				clipAction.setEnabled(false);
				close();
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
			return Sector.EMPTY_SECTOR;

		return new Sector(bottomleft.getLatitude(), topright.getLatitude(), bottomleft.getLongitude(),
				topright.getLongitude());
	}

	private void setSector(Sector sector, boolean fromGlobe)
	{
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
	}

	private void clipSector(final Frame frame, final WorldWindow wwd)
	{
		if (!(wwd.getSceneController() instanceof StereoSceneController))
		{
			throw new IllegalStateException("SceneController not an instance of "
					+ StereoSceneController.class.getName());
		}

		final StereoSceneController sceneController = (StereoSceneController) wwd.getSceneController();
		sceneController.clipSector(sector);
		wwd.redraw();
	}

	private static class MySectorSelector extends SectorSelector
	{
		private WorldWindow wwd;

		public MySectorSelector(WorldWindow wwd)
		{
			super(wwd);
			this.wwd = wwd;
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
	}
}
