package au.gov.ga.worldwind.viewer.panels.geonames;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.viewer.components.FlatJButton;
import au.gov.ga.worldwind.viewer.panels.geonames.GeoNamesSearch.Results;
import au.gov.ga.worldwind.viewer.panels.geonames.GeoNamesSearch.SearchType;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.util.HSLColor;
import au.gov.ga.worldwind.viewer.util.Icons;
import au.gov.ga.worldwind.viewer.util.Util;

public class GeoNamesSearchPanel extends AbstractThemePanel
{
	private WorldWindow wwd;
	private GeoNameLayer geonameLayer = new GeoNameLayer();
	private DefaultListModel listModel;
	private Thread currentSearch;
	private Object lock = new Object();

	private JTextField searchText;
	private JList list;
	private FlatJButton searchButton;
	private FlatJButton clearButton;

	private JScrollPane listScrollPane;

	public GeoNamesSearchPanel()
	{
		super(new GridBagLayout());
		GridBagConstraints c;

		searchText = new JTextField();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(searchText, c);

		searchButton = new FlatJButton(Icons.search.getIcon());
		searchButton.restrictSize();
		searchButton.setToolTipText("Search");
		searchButton.setEnabled(false);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		add(searchButton, c);

		clearButton = new FlatJButton(Icons.remove.getIcon());
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
		showResults.setSelected(geonameLayer.isEnabled());
		showResults.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				geonameLayer.setEnabled(showResults.isSelected());
				if (wwd != null)
					wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.WEST;
		add(showResults, c);

		panel = new JPanel(new BorderLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(panel, c);

		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setCellRenderer(new CustomRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listScrollPane = new JScrollPane(list);
		listScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		panel.add(listScrollPane, BorderLayout.CENTER);
		listScrollPane.setPreferredSize(new Dimension(MINIMUM_LIST_HEIGHT, MINIMUM_LIST_HEIGHT));

		list.setSelectionForeground(Color.black);
		Color backgroundSelection = list.getSelectionBackground();
		HSLColor hsl = new HSLColor(backgroundSelection);
		list.setSelectionBackground(hsl.adjustTone(80));

		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					flyToSelection();
				}
			}
		});

		list.addMouseMotionListener(new MouseMotionAdapter()
		{
			private GeoName lastPlace;

			@Override
			public void mouseMoved(MouseEvent e)
			{
				String text = null;
				int index = list.locationToIndex(e.getPoint());
				if (index >= 0)
				{
					Rectangle r = list.getCellBounds(index, index);
					if (r.contains(e.getPoint()))
					{
						Object o = list.getModel().getElementAt(index);
						if (o instanceof GeoName)
						{
							GeoName place = (GeoName) o;
							text = "<html>Name: " + place.name + "<br>";
							text += "Country: " + place.country + "<br>";
							text +=
									"Class: " + Util.capitalizeFirstLetter(place.fcodename)
											+ "<br>";
							text +=
									"Location: ("
											+ String.format("%7.3f\u00B0, %7.3f\u00B0",
													place.latlon.getLatitude().degrees,
													place.latlon.getLongitude().degrees)
											+ ")</html>";

							if (lastPlace != place)
								setToolTipText(null);
							lastPlace = place;
						}
					}
				}
				list.setToolTipText(text);
			}
		});

		ActionListener al = new ActionListener()
		{
			@Override
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

					listScrollPane.setVisible(true);
					setResizable(true);
					validate();

					search(str, type);
				}
			}
		};
		searchButton.addActionListener(al);
		searchText.addActionListener(al);

		searchText.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				searchTextChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				searchTextChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				searchTextChanged();
			}
		});

		clearButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchText.setText("");
				clear();
			}
		});

		clear();
	}

	@Override
	public Icon getIcon()
	{
		return Icons.search.getIcon();
	}

	private void clear()
	{
		listScrollPane.setVisible(false);
		setResizable(false);
		currentSearch = null;
		geonameLayer.clearText();
		listModel.clear();
		clearButton.setEnabled(false);
		if (wwd != null)
			wwd.redraw();

		validate();
	}

	private void searchTextChanged()
	{
		searchButton.setEnabled(searchText.getText().length() > 0);
	}

	private void flyToSelection()
	{
		Object object = list.getSelectedValue();

		if (object instanceof GeoName)
		{
			GeoName place = (GeoName) object;

			if (wwd != null)
			{
				View view = wwd.getView();
				if (view instanceof OrbitView)
				{
					OrbitView orbitView = (OrbitView) view;
					Position center = orbitView.getCenterPosition();
					Position newCenter = place.getPosition();
					long lengthMillis = Util.getScaledLengthMillis(center, newCenter);

					double zoom = Math.max(1e4, Math.min(1e5, orbitView.getZoom()));

					view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView,
							center, newCenter, orbitView.getHeading(), Angle.ZERO, orbitView
									.getPitch(), Angle.ZERO, orbitView.getZoom(), zoom,
							lengthMillis, true));
					wwd.redraw();
				}
			}
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
					@Override
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
		geonameLayer.clearText();
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
				for (GeoName place : results.places)
				{
					geonameLayer.addText(place);
					listModel.addElement(place);
				}
				clearButton.setEnabled(true);
			}
		}

		if (wwd != null)
			wwd.redraw();
	}

	private void search(final String text, final SearchType type)
	{
		synchronized (lock)
		{
			currentSearch = new Thread(new Runnable()
			{
				@Override
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
			currentSearch.setName("Place search");
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

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
		{
			if (value instanceof GeoName)
			{
				GeoName place = (GeoName) value;
				String text = place.name;
				if (place.country != null && !place.fclass.equals("PCLI"))
				{
					text += " (" + place.country + ")";
				}
				setText(text);

				BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
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

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();
		LayerList layers = wwd.getModel().getLayers();
		if (!layers.contains(geonameLayer))
		{
			layers.add(geonameLayer);
		}
	}

	@Override
	public void dispose()
	{
	}
}
