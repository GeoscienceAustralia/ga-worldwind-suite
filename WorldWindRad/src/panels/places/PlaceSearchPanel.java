package panels.places;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nasa.worldwind.view.FlyToOrbitViewStateIterator;

import panels.places.GeoNamesSearch.Results;
import panels.places.GeoNamesSearch.SearchType;
import util.FlatJButton;
import util.Icons;
import util.Util;

public class PlaceSearchPanel extends JPanel
{
	private WorldWindow wwd;
	private static PlaceLayer placeLayer = new PlaceLayer();
	private DefaultListModel listModel;
	private Thread currentSearch;
	private Object lock = new Object();

	private JTextField searchText;
	private JTextField nameText;
	private JTextField latlonText;
	private JTextField typeText;
	private JList list;
	private FlatJButton searchButton;
	private FlatJButton clearButton;

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

		searchText = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(searchText, c);

		searchButton = new FlatJButton(Icons.search);
		searchButton.restrictSize();
		searchButton.setToolTipText("Search");
		searchButton.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		add(searchButton, c);

		clearButton = new FlatJButton(Icons.remove);
		clearButton.restrictSize();
		clearButton.setToolTipText("Clear results");
		clearButton.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		add(clearButton, c);

		JPanel panel = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
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

		final JCheckBox showResults = new JCheckBox("Show results on globe");
		showResults.setSelected(placeLayer.isEnabled());
		showResults.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				placeLayer.setEnabled(showResults.isSelected());
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.WEST;
		add(showResults, c);

		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setCellRenderer(new CustomRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		add(scrollPane, c);

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.setBorder(new TitledBorder("Description"));
		add(panel, c);

		JLabel label = new JLabel("Name:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		panel.add(label, c);

		nameText = new JTextField("");
		nameText.setEditable(false);
		//nameText.setBackground(list.getBackground());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 4, 0, 0);
		panel.add(nameText, c);

		label = new JLabel("Lat/Lon:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(4, 0, 0, 0);
		panel.add(label, c);

		latlonText = new JTextField("");
		latlonText.setEditable(false);
		//latlonText.setBackground(list.getBackground());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4, 4, 0, 0);
		panel.add(latlonText, c);

		label = new JLabel("Type:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(4, 0, 0, 0);
		panel.add(label, c);

		typeText = new JTextField("");
		typeText.setEditable(false);
		//typeText.setBackground(list.getBackground());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4, 4, 0, 0);
		panel.add(typeText, c);

		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					flyToSelection();
				}
			}
		});

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String str = searchText.getText();
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
		searchButton.addActionListener(al);
		searchText.addActionListener(al);

		searchText.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				searchTextChanged();
			}

			public void insertUpdate(DocumentEvent e)
			{
				searchTextChanged();
			}

			public void removeUpdate(DocumentEvent e)
			{
				searchTextChanged();
			}
		});

		clearButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				searchText.setText("");
				clear();
			}
		});
	}

	private void clear()
	{
		currentSearch = null;
		placeLayer.clearText();
		listModel.clear();
		nameText.setText("");
		latlonText.setText("");
		typeText.setText("");
		clearButton.setEnabled(false);
		wwd.redraw();
	}

	private void searchTextChanged()
	{
		searchButton.setEnabled(searchText.getText().length() > 0);
	}

	private void flyToSelection()
	{
		Object object = list.getSelectedValue();

		if (object instanceof Place)
		{
			Place place = (Place) object;

			View view = wwd.getView();
			if (view instanceof OrbitView)
			{
				OrbitView orbitView = (OrbitView) view;
				Position center = orbitView.getCenterPosition();
				Position newCenter = place.getPosition();
				long lengthMillis = Util.getScaledLengthMillis(center
						.getLatLon(), newCenter.getLatLon());

				double zoom = Math.max(100000, Math.min(1000000, orbitView
						.getZoom()));
				ViewStateIterator vsi = FlyToOrbitViewStateIterator
						.createPanToIterator(wwd.getModel().getGlobe(), center,
								newCenter, orbitView.getHeading(), Angle.ZERO,
								orbitView.getPitch(), Angle.ZERO, orbitView
										.getZoom(), zoom, lengthMillis, true);

				view.applyStateIterator(vsi);
			}

			nameText.setText(place.name);
			latlonText.setText(String.format("%7.3f\u00B0, %7.3f\u00B0",
					place.latlon.getLatitude().degrees, place.latlon
							.getLongitude().degrees));
			typeText.setText(place.fcodename);

			nameText.setSelectionStart(0);
			nameText.setSelectionEnd(0);
			latlonText.setSelectionStart(0);
			latlonText.setSelectionEnd(0);
			typeText.setSelectionStart(0);
			typeText.setSelectionEnd(0);
		}
	}

	private void showResults(final Results results)
	{
		if (EventQueue.isDispatchThread())
		{
			showResultsImpl(results);
		}
		else
		{
			try
			{
				EventQueue.invokeAndWait(new Runnable()
				{
					public void run()
					{
						showResultsImpl(results);
					}
				});
			}
			catch (Exception e)
			{
			}
		}
	}

	private void showResultsImpl(Results results)
	{
		placeLayer.clearText();
		listModel.clear();

		if (results.error != null)
		{
			listModel.addElement(results.error);
		}
		else
		{
			if (results.places.size() == 0)
			{
				listModel.addElement("0 matches found");
			}
			else
			{
				for (Place place : results.places)
				{
					placeLayer.addText(place);
					listModel.addElement(place);
				}
				clearButton.setEnabled(true);
			}
		}

		wwd.redraw();
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

	private class CustomRenderer extends JLabel implements ListCellRenderer
	{
		public CustomRenderer()
		{
			super();
			setOpaque(true);
			setBorder(new EmptyBorder(1, 1, 1, 1));
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			if (value instanceof Place)
			{
				Place place = (Place) value;
				String text = place.name;
				if (place.country != null && !place.fclass.equals("PCLI"))
				{
					text += " (" + place.country + ")";
				}
				setText(text);

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
				setIcon(icon);
			}
			else
			{
				setText(value.toString());
				setIcon(null);
			}
			if (isSelected)
				setBackground(list.getSelectionBackground());
			else
				setBackground(list.getBackground());
			return this;
		}
	}
}
