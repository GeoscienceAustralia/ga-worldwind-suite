package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.util.HSLColor;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

public class PlacesPanel extends AbstractThemePanel
{
	private static final String DEFAULT_PLACES_PERSISTANCE_FILENAME = "places.xml";
	private String placesPersistanceFilename = DEFAULT_PLACES_PERSISTANCE_FILENAME;

	private List<Place> places = new ArrayList<Place>();

	private WorldWindow wwd;
	private JList list;
	private DefaultListModel model;
	private ListItem dragging;
	private PlaceLayer layer;
	private Window window;
	private boolean playing = false;
	private BasicAction addAction, editAction, deleteAction, playAction, importAction,
			exportAction;
	private JFileChooser exportImportChooser;
	private LayersPanel layerPanels;

	private class ListItem
	{
		public final JCheckBox check;
		public final Place place;

		public ListItem(Place place, JCheckBox check)
		{
			this.place = place;
			this.check = check;
		}
	}

	public PlacesPanel()
	{
		super(new BorderLayout());

		createPanel();
		loadPlaces(getPlacesFile(), false);
	}

	protected void loadPlaces(File file, boolean append)
	{
		List<Place> places = PlacePersistance.readFromXML(file);

		//if the read failed, attempt to read from the old format
		if (places == null && wwd != null)
		{
			places = LegacyPlaceReader.readPlacesFromLegacyXML(file, wwd.getModel().getGlobe());
		}

		if (places == null)
			places = new ArrayList<Place>();

		if (append)
			this.places.addAll(places);
		else
			this.places = places;

		populateList();
	}

	protected void savePlaces(File file)
	{
		PlacePersistance.saveToXML(places, file);
	}

	protected File getPlacesFile()
	{
		return new File(SettingsUtil.getUserDirectory(), placesPersistanceFilename);
	}

	public List<Place> getPlaces()
	{
		return places;
	}

	@Override
	public ImageIcon getIcon()
	{
		return Icons.bookmark.getIcon();
	}

	private void createPanel()
	{
		addAction = new BasicAction("Add", "Add place", Icons.add.getIcon());
		addAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addNew();
			}
		});

		editAction = new BasicAction("Edit", "Edit selected", Icons.properties.getIcon());
		editAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				editSelected();
			}
		});

		deleteAction = new BasicAction("Delete", "Delete selected", Icons.delete.getIcon());
		deleteAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				deleteSelected();
			}
		});

		playAction = new BasicAction("Play", "", Icons.run.getIcon());
		playAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (playing)
					stopPlaces();
				else
					playPlaces();
			}
		});

		importAction = new BasicAction("Import", "Import places", Icons.imporrt.getIcon());
		importAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				importPlaces();
			}
		});

		exportAction = new BasicAction("Export", "Export places", Icons.export.getIcon());
		exportAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				exportPlaces();
			}
		});



		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.add(addAction);
		toolBar.add(editAction);
		toolBar.add(deleteAction);
		toolBar.addSeparator();
		toolBar.add(importAction);
		toolBar.add(exportAction);
		toolBar.addSeparator();
		toolBar.add(playAction);
		add(toolBar, BorderLayout.PAGE_START);

		model = new DefaultListModel();
		list = new JList(model);
		list.setCellRenderer(new CheckboxListCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(MINIMUM_LIST_HEIGHT, MINIMUM_LIST_HEIGHT));

		list.setSelectionForeground(Color.black);
		Color backgroundSelection = list.getSelectionBackground();
		HSLColor hsl = new HSLColor(backgroundSelection);
		list.setSelectionBackground(hsl.adjustTone(80));

		ListSelectionListener lsl = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				enableActions();
				ListItem item = (ListItem) list.getSelectedValue();
				if (item != null)
					placeSelected(item.place);
			}
		};
		list.getSelectionModel().addListSelectionListener(lsl);
		lsl.valueChanged(null);

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(editAction);
		popupMenu.add(deleteAction);

		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int index = list.locationToIndex(e.getPoint());
				if (index >= 0)
				{
					Rectangle rect = list.getCellBounds(index, index);
					if (rect.contains(e.getPoint()))
					{
						if (e.getButton() == MouseEvent.BUTTON1)
						{
							Rectangle checkRect =
									new Rectangle(rect.x, rect.y, rect.height, rect.height);
							ListItem listItem = (ListItem) model.get(index);
							if (checkRect.contains(e.getPoint()))
							{
								toggleCheck(listItem);
							}
							else if (e.getClickCount() == 2)
							{
								flyTo(listItem);
							}
						}
						else if (e.getButton() == MouseEvent.BUTTON3)
						{
							list.setSelectedIndex(index);
							popupMenu.show(list, e.getX(), e.getY());
						}
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				dragging = getListItemUnderMouse(e.getPoint());
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				dragging = null;
			}
		});

		list.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (dragging != null)
				{
					int index = list.locationToIndex(e.getPoint());
					moveTo(dragging, index);
				}
			}
		});

		list.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
				{
					deleteSelected();
				}
				else if (e.getKeyCode() == KeyEvent.VK_SPACE)
				{
					toggleCheck(null);
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					flyTo(null);
				}
			}
		});

		stopPlaces();
	}

	private void enableActions()
	{
		addAction.setEnabled(!playing);
		editAction.setEnabled(list.getSelectedIndex() >= 0 && !playing);
		deleteAction.setEnabled(list.getSelectedIndex() >= 0 && !playing);
	}

	private void placeSelected(Place place)
	{
		layer.selectPlace(place);
		wwd.redraw();
	}

	private ListItem getListItemUnderMouse(Point point)
	{
		int index = list.locationToIndex(point);
		if (index >= 0)
		{
			Rectangle rect = list.getCellBounds(index, index);
			if (rect.contains(point))
			{
				return (ListItem) model.get(index);
			}
		}
		return null;
	}

	private void toggleCheck(ListItem item)
	{
		if (item == null)
		{
			item = (ListItem) list.getSelectedValue();
		}
		if (item != null)
		{
			item.check.setSelected(!item.check.isSelected());
			item.place.setVisible(item.check.isSelected());
			list.repaint();
			refreshLayer();
		}
	}

	private void flyTo(ListItem item)
	{
		if (item == null)
		{
			item = (ListItem) list.getSelectedValue();
		}
		if (item != null)
		{
			flyToPlace(item.place);
		}
	}

	private void populateList()
	{
		model.removeAllElements();
		for (Place place : places)
		{
			addPlace(place);
		}
		list.repaint();
	}

	private void addPlace(Place place)
	{
		JCheckBox check = new JCheckBox("", place.isVisible());
		ListItem item = new ListItem(place, check);
		model.addElement(item);
	}

	private void addNew()
	{
		View view = wwd.getView();

		Vec4 eyePoint = view.getCurrentEyePoint();
		Position eyePosition = view.getGlobe().computePositionFromPoint(eyePoint);
		Position centerPosition = Util.computeViewClosestCenterPosition(view, eyePoint);
		Vec4 upVector = view.getUpVector();
		double zoom =
				eyePoint.distanceTo3(view.getGlobe().computePointFromPosition(centerPosition));
		double minZoom = zoom * 5;

		Place place = new Place("", centerPosition, minZoom);
		place.setEyePosition(eyePosition);
		place.setUpVector(upVector);
		place.setSaveCamera(false);
		PlaceEditor editor = new PlaceEditor(wwd, window, "New place", place, getIcon());
		int value = editor.getOkCancel();
		if (value == JOptionPane.OK_OPTION)
		{
			places.add(place);
			addPlace(place);
			list.repaint();
			refreshLayer();
		}
	}

	void selectPlace(Place place)
	{
		if (place != null)
		{
			for (int i = 0; i < model.size(); i++)
			{
				ListItem item = (ListItem) model.get(i);
				if (item.place == place)
				{
					list.setSelectedValue(item, true);
					break;
				}
			}
		}
	}

	private void editSelected()
	{
		ListItem item = (ListItem) list.getSelectedValue();
		if (item != null)
		{
			Place editing = new Place(item.place);
			PlaceEditor editor = new PlaceEditor(wwd, window, "Edit place", editing, getIcon());
			int value = editor.getOkCancel();
			if (value == JOptionPane.OK_OPTION)
			{
				item.place.setValuesFrom(editing);
				list.repaint();
				refreshLayer();
			}
		}
	}

	private void deleteSelected()
	{
		int index = list.getSelectedIndex();
		ListItem item = (ListItem) list.getSelectedValue();
		if (item != null)
		{
			int value =
					JOptionPane.showConfirmDialog(this,
							"Are you sure you want to delete the place '" + item.place.getLabel()
									+ "'?", "Delete place", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (value == JOptionPane.YES_OPTION)
			{
				model.removeElement(item);
				places.remove(item.place);
				list.repaint();
				if (index >= model.getSize())
					index = model.getSize() - 1;
				list.setSelectedIndex(index);
				refreshLayer();
			}
		}
	}

	public void deleteAllPlacesWarn()
	{
		int value =
				JOptionPane
						.showConfirmDialog(window, "All places will be deleted!\nAre you sure?",
								"Delete all places", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
		if (value == JOptionPane.YES_OPTION)
		{
			deleteAllPlaces();
		}
	}

	private void deleteAllPlaces()
	{
		places.clear();
		populateList();
		refreshLayer();
	}

	private void moveTo(ListItem item, int index)
	{
		int srcIndex = model.indexOf(item);
		if (srcIndex != index)
		{
			model.remove(srcIndex);
			model.add(index, item);
			places.remove(item.place);
			places.add(index, item.place);
			list.setSelectedIndex(index);
			list.repaint();
		}
	}

	private void refreshLayer()
	{
		layer.refresh();
		wwd.redraw();
	}

	private synchronized void stopPlaces()
	{
		if (wwd != null)
			wwd.getView().stopAnimations();
		playing = false;
		playAction.setIcon(Icons.run.getIcon());
		playAction.setToolTipText("Play through places");
		enableActions();
	}

	private synchronized void playPlaces()
	{
		if (!playing)
		{
			playing = true;
			wwd.getInputHandler().addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					stopPlaces();
					if (!playing)
					{
						wwd.getInputHandler().removeMouseListener(this);
					}
				}
			});

			//if there are any current movements or animations, they will change the view
			//after the place animation has been applied, so stop them
			wwd.getView().stopMovement();
			wwd.getView().stopAnimations();

			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					ListItem item = (ListItem) list.getSelectedValue();
					//List<Place> places = Settings.get().getPlaces();
					int index = -1;
					if (item != null)
					{
						index = places.indexOf(item.place);
					}
					else if (!places.isEmpty())
					{
						index = 0;
					}

					long jump = 100;
					while (playing && index >= 0)
					{
						Place place = places.get(index);
						if (!place.isExcludeFromPlaylist())
						{
							selectPlace(place);
							long length = flyToPlace(place);
							if (length < 0)
								break;
							length += Settings.get().getPlacesPause();

							// sleep for 'length' in 'jump' increments
							for (; playing && length > jump; length -= jump)
							{
								sleep(jump);
							}
							if (playing)
								sleep(length);
						}

						int nextIndex = index;
						while (true)
						{
							if (++nextIndex >= places.size())
								nextIndex = 0;
							if (nextIndex == index)
							{
								index = -1;
								break;
							}
							if (!places.get(nextIndex).isExcludeFromPlaylist())
							{
								index = nextIndex;
								break;
							}
						}
					}
					stopPlaces();
				}

				private void sleep(long millis)
				{
					try
					{
						Thread.sleep(millis);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			});
			thread.setName("Places playback");
			thread.setDaemon(true);
			thread.start();
		}

		playAction.setIcon(Icons.stop.getIcon());
		playAction.setToolTipText("Stop playback");
		enableActions();
	}

	private long flyToPlace(Place place)
	{
		View view = wwd.getView();

		double centerElevation =
				view.getGlobe().getElevation(place.getLatLon().latitude,
						place.getLatLon().longitude);
		Position centerPosition = new Position(place.getLatLon(), centerElevation);
		Position eyePosition = place.getEyePosition();
		if (!place.isSaveCamera() || eyePosition == null)
		{
			Position currentEyePosition = view.getCurrentEyePosition();
			double elevation = currentEyePosition.elevation;

			double minZoom = place.getMinZoom();
			double maxZoom = place.getMaxZoom();
			if (minZoom >= 0 && elevation > minZoom)
				elevation = Math.max(minZoom, 1000);
			else if (maxZoom >= 0 && elevation < maxZoom)
				elevation = maxZoom;

			eyePosition = new Position(centerPosition, elevation);
		}

		long lengthMillis =
				AnimatorHelper.addAnimator(view, centerPosition, eyePosition, place.getUpVector());
		wwd.redraw();

		return lengthMillis;
	}

	private void animateLayers(Place place)
	{
		// - find a list of currently enabled layers (enabled && opacity > 0): OLD
		// - find the list of enabled layers in the place: NEW
		// - move backwards through the NEW list, finding any that are in a different order
		//       outOfOrder means: (any that are in a greater position relative to any other
		//       layers in the NEW list compared to the OLD list, and are in both lists)
		// - add those out of order to a third list: OOO

		// - fade (NEW - OLD - OOO) layers from 0 to new between 0.0 and 1.0 percent
		// - fade (OLD - NEW - OOO) layers from current to 0 between 0.0 and 1.0 percent
		// - fade OOO layers from current to 0 between 0.0 and 0.5 percent
		// - move layers to correct positions at 0.5 percent
		// - fade OOO layers from 0 to new between 0.5 and 1.0 percent

		//TODO Have to modify the implementation of saving the places.
		//No longer save to the settings file, but save instead to a places file.
		//Use proper XML handling instead of the XMLEncoder.
		//Save the full layer tree (perhaps use the LayerPersistance class) to an element in the place.
	}

	private JFileChooser getChooser()
	{
		if (exportImportChooser == null)
		{
			exportImportChooser = new JFileChooser();
			FileFilter filter = new FileFilter()
			{
				@Override
				public boolean accept(File f)
				{
					if (f.isDirectory())
						return true;
					int index = f.getName().lastIndexOf('.');
					if (index < 0)
						return false;
					String ext = f.getName().substring(index + 1);
					return ext.toLowerCase().equals("xml");
				}

				@Override
				public String getDescription()
				{
					return "XML files (*.xml)";
				}
			};
			exportImportChooser.setFileFilter(filter);
		}
		return exportImportChooser;
	}

	private void importPlaces()
	{
		final JFileChooser chooser = getChooser();
		chooser.setMultiSelectionEnabled(true);
		if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION)
		{
			for (File file : chooser.getSelectedFiles())
			{
				try
				{
					importPlaces(file);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(window, "Could not import " + file.getName(),
							"Import error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		chooser.setMultiSelectionEnabled(false);
	}

	private void importPlaces(File file) throws Exception
	{
		if (file.exists())
		{
			loadPlaces(file, true);
			refreshLayer();
		}
	}

	private void exportPlaces()
	{
		final JFileChooser chooser = getChooser();
		if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".xml"))
			{
				file = new File(file.getAbsolutePath() + ".xml");
			}
			if (file.exists())
			{
				int answer =
						JOptionPane.showConfirmDialog(window, file.getAbsolutePath()
								+ " already exists.\nDo you want to replace it?", "Export",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (answer != JOptionPane.YES_OPTION)
					file = null;
			}
			if (file != null)
			{
				try
				{
					exportPlaces(file);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(window, "Error: " + e, "Export error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void exportPlaces(File file) throws Exception
	{
		savePlaces(file);
	}

	private class CheckboxListCellRenderer extends JPanel implements ListCellRenderer
	{
		private JPanel panel;
		private JLabel label;

		public CheckboxListCellRenderer()
		{
			setLayout(new BorderLayout());
			panel = new JPanel(new BorderLayout());
			add(panel, BorderLayout.WEST);
			label = new JLabel();
			label.setOpaque(true);
			add(label, BorderLayout.CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
		{
			Color background = isSelected ? list.getSelectionBackground() : list.getBackground();

			if (value instanceof ListItem)
			{
				final Place place = ((ListItem) value).place;
				final JCheckBox check = ((ListItem) value).check;
				if (panel.getComponentCount() != 1 || panel.getComponent(0) != check)
				{
					panel.removeAll();
					panel.add(check, BorderLayout.CENTER);
				}
				label.setText(place.getLabel());
				check.setBackground(background);
				check.setSelected(place.isVisible());
			}
			else
			{
				label.setText(value.toString());
				panel.removeAll();
			}

			label.setBackground(background);
			panel.setBackground(background);

			return this;
		}
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();
		layer = new PlaceLayer(wwd, this);
		wwd.getModel().getLayers().add(layer);

		window = SwingUtilities.getWindowAncestor(this);

		for (ThemePanel panel : theme.getPanels())
		{
			if (panel instanceof LayersPanel)
			{
				this.layerPanels = (LayersPanel) panel;
			}
		}

		//load legacy places
		loadPlaces(Settings.getSettingsFile(), true);
	}

	@Override
	public void dispose()
	{
		savePlaces(getPlacesFile());
	}
}
