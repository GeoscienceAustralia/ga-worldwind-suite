package au.gov.ga.worldwind.viewer.components.sectorsaver;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.examples.util.SectorSelector;
import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;

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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import au.gov.ga.worldwind.common.ui.JIntegerField;
import au.gov.ga.worldwind.common.util.Util;

public class ImageSectorSaver
{
	public static void beginSelection(final Frame frame, String title, final WorldWindow wwd)
	{
		new ImageSectorSaver(frame, title, wwd);
	}

	private MySectorSelector selector;
	private JDialog dialog;
	private JButton okButton;

	private JTextField toprightField;
	private JTextField bottomleftField;
	private JIntegerField resolutionField;
	private JLabel dimensionsLabel;
	private JTextField outputField;

	private Sector sector;
	private File output;
	private Dimension size;

	private boolean settingSector = false;
	private JFileChooser chooser = new JFileChooser();

	private ImageSectorSaver(final Frame frame, String title, final WorldWindow wwd)
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

		DocumentListener dl = new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				validate();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				validate();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				validate();
			}
		};
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
		selector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY,
				new PropertyChangeListener()
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

		label = new JLabel("All active tiled imagery layers will be saved");
		c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(s, s, s, s);
		mainPanel.add(label, c);

		i = 0;
		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Extents"));
		c = new GridBagConstraints();
		c.gridy = 1;
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


		i = 0;
		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Output"));
		c = new GridBagConstraints();
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(panel, c);

		label = new JLabel("Resolution:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		resolutionField = new JIntegerField(2048);
		resolutionField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.weightx = 1;
		c.insets = new Insets(s, s, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(resolutionField, c);

		label = new JLabel("Dimensions:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		dimensionsLabel = new JLabel("");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.insets = new Insets(s, s, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(dimensionsLabel, c);

		label = new JLabel("Output:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = i;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, s, 0);
		panel.add(label, c);

		JPanel panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = i++;
		c.insets = new Insets(s, s, s, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		panel.add(panel2, c);

		outputField = new JTextField();
		outputField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(0, 0, 0, s);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel2.add(outputField, c);

		JButton browseButton = new JButton("...");
		browseButton.setToolTipText("Browse");
		browseButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				chooser.setDialogTitle("Select output file");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new GeoTIFFFileFilter());
				if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION)
				{
					outputField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		panel2.add(browseButton, c);


		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing)));
		dialog.add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("Save");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveSector(frame, wwd);
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

		return new Sector(bottomleft.getLatitude(), topright.getLatitude(),
				bottomleft.getLongitude(), topright.getLongitude());
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
						new Sector(sector.getMaxLatitude(), sector.getMinLatitude(),
								sector.getMinLongitude(), sector.getMaxLongitude());
				modified = true;
			}
			if (sector.getDeltaLonDegrees() < 0)
			{
				sector =
						new Sector(sector.getMinLatitude(), sector.getMaxLatitude(),
								sector.getMaxLongitude(), sector.getMinLongitude());
				modified = true;
			}

			this.sector = sector;

			if (fromGlobe | modified)
			{
				bottomleftField.setText(sector.getMinLatitude().degrees + " "
						+ sector.getMinLongitude().degrees);
				toprightField.setText(sector.getMaxLatitude().degrees + " "
						+ sector.getMaxLongitude().degrees);
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

		size = null;
		if (valid)
		{
			Integer resolution = resolutionField.getValue();
			if (resolution != null)
			{
				size = adjustSize(sector, resolution);
			}
		}

		valid &= size != null;
		if (size != null)
			dimensionsLabel.setText(size.width + " x " + size.height);

		output = null;
		if (outputField.getText().length() > 0)
		{
			output = new File(outputField.getText());
		}

		valid &= output != null;

		okButton.setEnabled(valid);
	}

	private void saveSector(final Frame frame, final WorldWindow wwd)
	{
		//create a local copy of the list of layers, so that it doesn't change
		final List<Layer> layers = new ArrayList<Layer>();
		for (Layer layer : wwd.getModel().getLayers())
		{
			if (layer.isEnabled() && layer instanceof TiledImageLayer)
				layers.add(layer);
		}

		final JDialog dialog = new JDialog(frame, "Please wait...", false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLayout(new BorderLayout());

		JPanel panel = new JPanel(new GridBagLayout());
		dialog.add(panel, BorderLayout.NORTH);

		final JLabel label = new JLabel("Generating layer list");
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 0, 5, 0);
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1;
		panel.add(label, c);

		final JProgressBar progressBar =
				new JProgressBar(JProgressBar.HORIZONTAL, 0, layers.size());
		dialog.add(progressBar, BorderLayout.CENTER);

		dialog.pack();
		Dimension dim = dialog.getSize();
		dim.width = 300;
		dialog.setSize(dim);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);

		/*Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (dialog.isVisible())
				{
					progressBar.setValue((progressBar.getValue() + 1) % progressBar.getMaximum());
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();*/

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					saveSector(frame, layers, sector, size, output.getAbsoluteFile(), label,
							progressBar);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame,
							"Error saving image:\n\n" + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				dialog.dispose();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void saveSector(Frame frame, List<Layer> layers, Sector sector, Dimension size,
			File output, JLabel label, JProgressBar progressBar) throws Exception
	{
		try
		{
			double texelSize = Math.abs(sector.getDeltaLonRadians()) / (double) size.width;

			BufferedImage image =
					new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);

			for (Layer l : layers)
			{
				label.setText("Saving layer: " + l.getName());

				if (l.isEnabled() && l instanceof TiledImageLayer)
				{
					TiledImageLayer layer = (TiledImageLayer) l;
					int level = layer.computeLevelForResolution(sector, texelSize);

					String mimeType = layer.getDefaultImageFormat();
					if (layer.isImageFormatAvailable("image/png"))
						mimeType = "image/png";
					else if (layer.isImageFormatAvailable("image/jpg"))
						mimeType = "image/jpg";

					try
					{
						image =
								layer.composeImageForSector(sector, size.width, size.height, 1d,
										level, mimeType, true, image, 30000);
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(frame, "Error composing image for layer "
								+ layer.getName() + ":\n\n" + e.getMessage(), "Warning",
								JOptionPane.WARNING_MESSAGE);
					}
				}

				progressBar.setValue(progressBar.getValue() + 1);
			}

			AVList params = new AVListImpl();

			params.setValue(AVKey.SECTOR, sector);
			params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
			params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
			params.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

			GeotiffWriter writer = new GeotiffWriter(output);
			writer.write(BufferedImageRaster.wrapAsGeoreferencedRaster(image, params));
		}
		catch (OutOfMemoryError e)
		{
			JOptionPane.showMessageDialog(frame,
					"Not enough memory. Try saving at a lower resolution.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static Dimension adjustSize(Sector sector, int maxSize)
	{
		Dimension size = new Dimension(maxSize, maxSize);

		if (null != sector && maxSize > 0)
		{
			double dLat = Math.abs(sector.getDeltaLatRadians());
			double dLon = Math.abs(sector.getDeltaLonRadians());
			double max = Math.max(dLat, dLon);
			double min = Math.min(dLat, dLon);

			int minSize = (int) ((min == 0d) ? maxSize : ((double) maxSize * min / max));

			if (dLon > dLat)
				size.height = minSize;
			else
				size.width = minSize;
		}

		return size;
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

	public class GeoTIFFFileFilter extends FileFilter
	{
		@Override
		public String getDescription()
		{
			return "GeoTIFF file (*.tif)";
		}

		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
				return true;
			return f.getName().toLowerCase().endsWith(".tif");
		}
	}
}
