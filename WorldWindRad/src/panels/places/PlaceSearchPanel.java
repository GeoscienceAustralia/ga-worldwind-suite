package panels.places;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import panels.places.GeoNamesSearch.Results;
import panels.places.GeoNamesSearch.SearchType;

public class PlaceSearchPanel extends JPanel
{
	private WorldWindow wwd;
	private static PlaceLayer placeLayer = new PlaceLayer();
	private DefaultListModel listModel;
	private Thread currentSearch;
	private Object lock = new Object();

	public PlaceSearchPanel(final WorldWindow wwd)
	{
		super(new GridBagLayout());
		GridBagConstraints c;

		this.wwd = wwd;
		LayerList layers = wwd.getModel().getLayers();
		if (!layers.contains(placeLayer))
		{
			layers.add(placeLayer);
		}

		final JTextField text = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(text, c);

		final JButton button = new JButton("Search");
		button.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		add(button, c);

		text.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				button.setEnabled(text.getText().length() > 0);
			}

			public void insertUpdate(DocumentEvent e)
			{
				button.setEnabled(text.getText().length() > 0);
			}

			public void removeUpdate(DocumentEvent e)
			{
				button.setEnabled(text.getText().length() > 0);
			}
		});

		JPanel panel = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		final JRadioButton fuzzy = new JRadioButton("Fuzzy");
		fuzzy.setSelected(true);
		panel.add(fuzzy);

		final JRadioButton place = new JRadioButton("Place");
		panel.add(place);

		final JRadioButton exact = new JRadioButton("Exact");
		panel.add(exact);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(fuzzy);
		buttonGroup.add(place);
		buttonGroup.add(exact);

		listModel = new DefaultListModel();
		final JList list = new JList(listModel);
		list.setCellRenderer(new CustomRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		add(scrollPane, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.setBorder(new TitledBorder("Description"));
		add(panel, c);

		JLabel label = new JLabel("Name:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);

		final JTextField name = new JTextField("");
		name.setEditable(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(name, c);

		label = new JLabel("Lat/Lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);

		final JTextField latlon = new JTextField("");
		latlon.setEditable(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(latlon, c);

		label = new JLabel("Type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);

		final JTextField fcode = new JTextField("");
		fcode.setEditable(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(fcode, c);

		list.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				Object object = list.getSelectedValue();

				if (object instanceof Place)
				{
					Place place = (Place) object;

					View view = wwd.getView();
					if (view instanceof OrbitView)
					{
						OrbitView orbitView = (OrbitView) view;
						ViewStateIterator vsi = FlyToOrbitViewStateIterator
								.createPanToIterator(orbitView, wwd.getModel()
										.getGlobe(), place.getPosition(),
										orbitView.getHeading(), orbitView
												.getPitch(), orbitView
												.getZoom());

						view.applyStateIterator(vsi);
					}

					name.setText(place.name);
					latlon.setText(String.format(
							"%7.3f\u00B0, %7.3f\u00B0", place.latlon
									.getLatitude().degrees, place.latlon
									.getLongitude().degrees));
					fcode.setText(place.featureCode.name);
				}
			}
		});

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String str = text.getText();
				if (str.length() > 0)
				{
					clear();
					listModel.addElement("Searching...");
					SearchType type = SearchType.FUZZY;
					if (place.isSelected())
					{
						type = SearchType.PLACE;
					}
					else if (exact.isSelected())
					{
						type = SearchType.EXACT;
					}
					search(str, type);
				}
			}
		};
		button.addActionListener(al);
		text.addActionListener(al);
	}

	private void clear()
	{
		placeLayer.clearText();
		listModel.clear();
		wwd.redraw();
	}

	private void showResults(final Results results)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				placeLayer.clearText();
				listModel.clear();

				if (results.error != null)
				{
					listModel.addElement(results.error);
				}
				else
				{
					for (Place place : results.places)
					{
						placeLayer.addText(place);
						listModel.addElement(place);
					}
				}

				wwd.redraw();
			}
		});
	}

	private void search(final String text, final SearchType type)
	{
		synchronized (lock)
		{
			currentSearch = new Thread(new Runnable()
			{
				public void run()
				{
					Results results = GeoNamesSearch.search(text, type);
					synchronized (lock)
					{
						if (Thread.currentThread() == currentSearch)
						{
							showResults(results);
						}
					}
				}
			});
			currentSearch.setDaemon(true);
			currentSearch.start();
		}
	}

	private class CustomRenderer implements ListCellRenderer
	{
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			if (value instanceof Place)
			{
				Place place = (Place) value;
				String text = place.name;
				if (place.country != null
						&& !place.featureCode.code.equals("PCLI"))
				{
					text += " (" + place.country + ")";
				}

				JLabel label = new JLabel(text);
				label.setOpaque(true);

				if (isSelected)
				{
					label.setBackground(list.getSelectionBackground());
				}
				else
				{
					label.setBackground(list.getBackground());
				}

				BufferedImage image = new BufferedImage(16, 16,
						BufferedImage.TYPE_INT_RGB);
				Graphics g = image.getGraphics();
				g.setColor(list.getBackground());
				g.fillRect(0, 0, image.getWidth(), image.getHeight());
				g.setColor(place.getColor());
				g.fillRect(0, 0, image.getWidth(), image.getHeight() - 1);
				g.setColor(Color.black);
				g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 2);
				g.dispose();

				ImageIcon icon = new ImageIcon(image);
				label.setIcon(icon);

				return label;
			}
			else
			{
				return new JLabel(value.toString());
			}
		}
	}
}
