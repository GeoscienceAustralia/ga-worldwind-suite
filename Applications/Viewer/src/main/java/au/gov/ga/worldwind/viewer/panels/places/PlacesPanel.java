package au.gov.ga.worldwind.viewer.panels.places;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.util.message.ViewerMessageConstants.*;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.Logging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Document;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.FileFilters;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.util.HSLColor;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.URLUtil;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.panels.layers.ILayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.INode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreePersistance;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

/**
 * A {@link ThemePanel} that manages a list of predefines placemark places.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlacesPanel extends AbstractThemePanel
{
	private static final String DEFAULT_PLACES_PERSISTANCE_FILENAME = "places.xml";
	private String placesPersistanceFilename = DEFAULT_PLACES_PERSISTANCE_FILENAME;
	private boolean persistPlaces = true;

	private List<Place> places = new ArrayList<Place>();

	private JFrame frame;
	private WorldWindow wwd;
	private JList list;
	private DefaultListModel model;
	private ListItem dragging;
	private PlaceLayer layer;
	private boolean playing = false;

	private BasicAction addAction;
	private BasicAction editAction;
	private BasicAction deleteAction;
	private BasicAction deleteAllAction;
	private BasicAction playAction;
	private BasicAction importAction;
	private BasicAction exportAction;
	private BasicAction previousAction;
	private BasicAction nextAction;

	private JFileChooser exportImportChooser;
	private LayersPanel layersPanel;
	private RenderingListener opacityChanger;
	private RenderingListener exaggerationChanger;

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

	public JFrame getFrame()
	{
		return frame;
	}

	public WorldWindow getWwd()
	{
		return wwd;
	}

	protected void loadPlaces(File file, boolean append)
	{
		try
		{
			List<Place> places = PlacePersistance.readFromXML(file, URLUtil.fromObject(file.getParentFile()));

			//if the read failed, attempt to read from the old format
			if (places == null && wwd != null)
			{
				places = LegacyPlaceReader.readPlacesFromLegacyXML(file, wwd.getModel().getGlobe());
			}

			if (places == null)
			{
				places = new ArrayList<Place>();
			}

			if (append)
			{
				this.places.addAll(places);
			}
			else
			{
				this.places = places;
			}

			populateList();
		}
		catch (Exception e)
		{
			Logging.logger().warning("Error loading places from " + file.getName() + ": " + e.getLocalizedMessage());
		}
	}

	protected void savePlaces(File file)
	{
		PlacePersistance.saveToXML(places, file);
	}

	protected File getPlacesFile()
	{
		return SettingsUtil.getSettingsFile(placesPersistanceFilename);
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
		initialiseActions();
		registerKeyboardShortcuts();
		addToolbar();
		inititalisePlacesList();

		stopPlaces();
	}

	private void inititalisePlacesList()
	{
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
		list.setSelectionBackground(hsl.adjustTone(15));

		ListSelectionListener lsl = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				enableActions();
				ListItem item = (ListItem) list.getSelectedValue();
				if (item != null)
				{
					placeSelected(item.place);
				}
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
							Rectangle checkRect = new Rectangle(rect.x, rect.y, rect.height, rect.height);
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
	}

	private void addToolbar()
	{
		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.add(addAction);
		toolBar.add(editAction);
		toolBar.add(deleteAction);
		toolBar.add(deleteAllAction);
		toolBar.addSeparator();
		toolBar.add(importAction);
		toolBar.add(exportAction);
		toolBar.addSeparator();
		toolBar.add(playAction);
		toolBar.add(nextAction);
		toolBar.add(previousAction);
		add(toolBar, BorderLayout.PAGE_START);
	}

	private void registerKeyboardShortcuts()
	{
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('w'), "places.next");
		getActionMap().put("places.next", nextAction);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('q'), "places.previous");
		getActionMap().put("places.previous", previousAction);
	}

	private void initialiseActions()
	{
		addAction =
				new BasicAction(getMessage(getPlacesAddLabelKey()), getMessage(getPlacesAddTooltipKey()),
						Icons.add.getIcon());
		addAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addNew();
			}
		});

		editAction =
				new BasicAction(getMessage(getPlacesEditLabelKey()), getMessage(getPlacesEditTooltipKey()),
						Icons.properties.getIcon());
		editAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				editSelected();
			}
		});

		deleteAction =
				new BasicAction(getMessage(getPlacesDeleteLabelKey()), getMessage(getPlacesDeleteTooltipKey()),
						Icons.delete.getIcon());
		deleteAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				deleteSelected();
			}
		});
		
		deleteAllAction = new BasicAction(getMessage(getPlacesDeleteAllLabelKey()), getMessage(getPlacesDeleteAllTooltipKey()), Icons.deleteall.getIcon());
		deleteAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				promptToDeleteAllPlaces();
			}
		});
		
		playAction =
				new BasicAction(getMessage(getPlacesPlayLabelKey()), getMessage(getPlacesPlayTooltipKey()),
						Icons.run.getIcon());
		playAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (playing)
				{
					stopPlaces();
				}
				else
				{
					playPlaces();
				}
			}
		});

		importAction =
				new BasicAction(getMessage(getPlacesImportLabelKey()), getMessage(getPlacesImportTooltipKey()),
						Icons.imporrt.getIcon());
		importAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				importPlaces();
			}
		});

		exportAction =
				new BasicAction(getMessage(getPlacesExportLabelKey()), getMessage(getPlacesExportTooltipKey()),
						Icons.export.getIcon());
		exportAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				exportPlaces();
			}
		});

		nextAction =
				new BasicAction(getMessage(getPlacesNextLabelKey()), getMessage(getPlacesNextTooltipKey()),
						Icons.down.getIcon());
		nextAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				nextPlace();
			}
		});

		previousAction =
				new BasicAction(getMessage(getPlacesPreviousLabelKey()), getMessage(getPlacesPreviousTooltipKey()),
						Icons.up.getIcon());
		previousAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				previousPlace();
			}
		});
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
		double zoom = eyePoint.distanceTo3(view.getGlobe().computePointFromPosition(centerPosition));
		double minZoom = zoom * 5;

		Place place = new Place("", centerPosition, minZoom);
		place.setEyePosition(eyePosition);
		place.setUpVector(upVector);
		place.setSaveCamera(false);
		int value = PlaceEditor.edit(place, this, "New place");
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
			int value = PlaceEditor.edit(editing, this, "Edit place");
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
			int value = JOptionPane.showConfirmDialog(this,
												  	 getMessage(getDeletePlaceWarnMessageKey(), item.place.getLabel()),
												  	 getMessage(getDeletePlaceWarnTitleKey()),
												  	 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (value == JOptionPane.YES_OPTION)
			{
				model.removeElement(item);
				places.remove(item.place);
				list.repaint();
				if (index >= model.getSize())
				{
					index = model.getSize() - 1;
				}
				list.setSelectedIndex(index);
				refreshLayer();
			}
		}
	}

	private void promptToDeleteAllPlaces()
	{
		int value = JOptionPane.showConfirmDialog(frame, 
												  getMessage(getDeleteAllPlacesWarnMessageKey()), 
												  getMessage(getDeleteAllPlacesWarnTitleKey()), 
												  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
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
		{
			wwd.getView().stopAnimations();
		}
		playing = false;
		playAction.setIcon(Icons.run.getIcon());
		playAction.setToolTipText(getMessage(getPlacesPlayTooltipKey()));
		enableActions();
	}

	private synchronized void nextPlace()
	{
		if (places.isEmpty())
		{
			return;
		}
		
		stopAllMotion();

		int index = getSelectedPlaceIndex();
		int nextIndex = (index + 1) % places.size();

		if (nextIndex == index || places.isEmpty())
		{
			return;
		}

		Place place = places.get(nextIndex);
		selectPlace(place);
		flyToPlace(place);
	}

	private synchronized void previousPlace()
	{
		if (places.isEmpty())
		{
			return;
		}
		
		stopAllMotion();

		int index = getSelectedPlaceIndex();
		int previousIndex = ((index - 1) + places.size()) % places.size();

		if (previousIndex == index || places.isEmpty())
		{
			return;
		}

		Place place = places.get(previousIndex);
		selectPlace(place);
		flyToPlace(place);
	}

	private void stopAllMotion()
	{
		if (playing)
		{
			stopPlaces();
		}
		wwd.getView().stopMovement();
		wwd.getView().stopAnimations();
	}

	private synchronized void playPlaces()
	{
		if (places.isEmpty())
		{
			return;
		}
		
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
					int index = getSelectedPlaceIndex();

					long jump = 100;
					while (playing && index >= 0)
					{
						final Place place = places.get(index);
						if (!place.isExcludeFromPlaylist())
						{
							SwingUtil.invokeTaskOnEDT(new Runnable(){
								@Override
								public void run()
								{
									selectPlace(place);
								}
							});
							long length = flyToPlace(place);
							if (length < 0)
							{
								break;
							}
							length += Settings.get().getPlacesPause();

							// sleep for 'length' in 'jump' increments
							for (; playing && length > jump; length -= jump)
							{
								sleep(jump);
							}
							if (playing)
							{
								sleep(length);
							}
						}

						int nextIndex = index;
						while (true)
						{
							if (++nextIndex >= places.size())
							{
								nextIndex = 0;
							}
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
		playAction.setToolTipText(getMessage(getPlacesStopTooltipKey()));
		enableActions();
	}

	private long flyToPlace(Place place)
	{
		View view = wwd.getView();

		double centerElevation = view.getGlobe().getElevation(place.getLatLon().latitude, place.getLatLon().longitude);
		Position centerPosition = new Position(place.getLatLon(), centerElevation);
		Position eyePosition = place.getEyePosition();
		if (!place.isSaveCamera() || eyePosition == null)
		{
			Position currentEyePosition = view.getCurrentEyePosition();
			double elevation = currentEyePosition.elevation;

			double minZoom = place.getMinZoom();
			double maxZoom = place.getMaxZoom();

			if (minZoom >= 0 && elevation > minZoom)
			{
				elevation = Math.max(minZoom, 1000);
			}
			else if (maxZoom >= 0 && elevation < maxZoom)
			{
				elevation = maxZoom;
			}

			eyePosition = new Position(centerPosition, elevation);
		}

		wwd.getView().stopAnimations();
		wwd.getView().stopMovement();
		wwd.removeRenderingListener(opacityChanger);

		long lengthMillis = AnimatorHelper.addAnimator(view, centerPosition, eyePosition, place.getUpVector());
		animateLayers(place, lengthMillis);
		animateVerticalExaggeration(place, lengthMillis);
		wwd.redraw();

		return lengthMillis;
	}

	private void animateLayers(Place place, final long lengthMillis)
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

		//TODO most of the above is implemented, apart from reordering
		//see revision 1331 for a almost-working reordering implementation

		if (layersPanel == null)
		{
			return;
		}

		INode placeNode = place.getLayers();
		if (placeNode == null)
		{
			return;
		}

		INode currentNode = layersPanel.getRoot();
		if (currentNode == null)
		{
			return;
		}

		List<ILayerNode> placeLayers = flattenLayerHierarchy(placeNode);
		List<ILayerNode> currentLayers = flattenLayerHierarchy(currentNode);

		//get the layer tree model from the panel
		final LayerTreeModel model = layersPanel.getModel();

		//generate a map of URL to lists of indices, mapping each layer URL to one or multiple
		//indices in the list (if there are multiple instances of the URL in the list)
		Map<URL, List<Integer>> currentUrlIndexMap = new HashMap<URL, List<Integer>>();
		for (int i = 0; i < currentLayers.size(); i++)
		{
			URL url = currentLayers.get(i).getLayerURL();
			List<Integer> list = currentUrlIndexMap.get(url);
			if (list == null)
			{
				list = new ArrayList<Integer>();
				currentUrlIndexMap.put(url, list);
			}
			list.add(i);
		}

		//find out the index in the current layer list of each layer in the place
		//also generate a list of the layers required for enabling/opacity changing
		List<Integer> indexOfPlaceInCurrent = new ArrayList<Integer>();
		final List<ILayerNode> toModify = new ArrayList<ILayerNode>();
		for (int i = 0; i < placeLayers.size(); i++)
		{
			ILayerNode placeLayer = placeLayers.get(i);
			URL url = placeLayer.getLayerURL();
			if (!currentUrlIndexMap.containsKey(url))
			{
				placeLayers.remove(i--);
				continue;
			}

			List<Integer> indices = currentUrlIndexMap.get(url);
			Integer index = indices.remove(0);
			if (indices.isEmpty())
			{
				currentUrlIndexMap.remove(url);
			}
			indexOfPlaceInCurrent.add(index);

			ILayerNode currentLayer = currentLayers.get(index);
			if (!currentLayer.isEnabled() || currentLayer.getOpacity() != placeLayer.getOpacity())
			{
				toModify.add(currentLayer);
			}
		}

		//urlIndexMap now contains all the indices of the layers that don't exist in the place's layers 
		final List<ILayerNode> toDisable = new ArrayList<ILayerNode>();
		for (Entry<URL, List<Integer>> entry : currentUrlIndexMap.entrySet())
		{
			for (Integer index : entry.getValue())
			{
				ILayerNode currentLayer = currentLayers.get(index);
				if (currentLayer.isEnabled())
				{
					toDisable.add(currentLayer);
				}
			}
		}

		//find out the index in the place of each layer in the current layer list
		final int[] indexOfCurrentInPlace = new int[currentLayers.size()];
		//initialise array to -1
		for (int i = 0; i < indexOfCurrentInPlace.length; i++)
		{
			indexOfCurrentInPlace[i] = -1;
		}
		//init array to indices of current layers in the places array
		for (int i = 0; i < indexOfPlaceInCurrent.size(); i++)
		{
			indexOfCurrentInPlace[indexOfPlaceInCurrent.get(i)] = i;
		}

		//find an array of start/end opacities for the layers to enable/change opacity
		final double[] modifyStartOpacities = new double[toModify.size()];
		final double[] modifyEndOpacities = new double[toModify.size()];
		for (int i = 0; i < toModify.size(); i++)
		{
			ILayerNode layer = toModify.get(i);
			if (!layer.isEnabled())
			{
				modifyStartOpacities[i] = 0d;
				model.setEnabled(layer, true);
				model.setOpacity(layer, 0d);
			}
			else
			{
				modifyStartOpacities[i] = layer.getOpacity();
			}
			int currentIndex = currentLayers.indexOf(layer);
			modifyEndOpacities[i] = placeLayers.get(indexOfCurrentInPlace[currentIndex]).getOpacity();
		}

		//find an array of start opacities for the layers to disable
		final double[] disableStartOpacities = new double[toDisable.size()];
		for (int i = 0; i < toDisable.size(); i++)
		{
			ILayerNode layer = toDisable.get(i);
			disableStartOpacities[i] = layer.getOpacity();
		}


		final long startTime = System.currentTimeMillis();
		opacityChanger = new RenderingListener()
		{
			@Override
			public void stageChanged(RenderingEvent event)
			{
				if (event.getStage() == RenderingEvent.BEFORE_RENDERING)
				{
					long currentTime = System.currentTimeMillis();
					double percent = (currentTime - startTime) / (double) lengthMillis;
					percent = Math.max(0d, Math.min(1d, percent));
					boolean complete = percent >= 1d;

					for (int i = 0; i < toDisable.size(); i++)
					{
						ILayerNode layer = toDisable.get(i);
						double opacity = Util.mixDouble(percent, disableStartOpacities[i], 0d);
						model.setOpacity(layer, opacity);

						if (complete)
						{
							model.setEnabled(layer, false);
						}
					}

					for (int i = 0; i < toModify.size(); i++)
					{
						ILayerNode layer = toModify.get(i);
						double opacity = Util.mixDouble(percent, modifyStartOpacities[i], modifyEndOpacities[i]);
						model.setOpacity(layer, opacity);
					}

					if (complete)
					{
						wwd.removeRenderingListener(this);
					}
				}
			}
		};
		wwd.addRenderingListener(opacityChanger);

		wwd.getInputHandler().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				wwd.removeRenderingListener(opacityChanger);
				wwd.getInputHandler().removeMouseListener(this);
			}
		});
	}

	private List<ILayerNode> flattenLayerHierarchy(INode root)
	{
		List<ILayerNode> layers = new ArrayList<ILayerNode>();
		addLayersToList(root, layers);
		return layers;
	}

	private void addLayersToList(INode parent, List<ILayerNode> layers)
	{
		if (parent instanceof ILayerNode && !parent.isTransient())
		{
			layers.add((ILayerNode) parent);
		}
		for (int i = 0; i < parent.getChildCount(); i++)
		{
			addLayersToList(parent.getChild(i), layers);
		}
	}

	private void animateVerticalExaggeration(final Place place, final long lengthMillis)
	{
		final double startExaggeration = wwd.getSceneController().getVerticalExaggeration();
		if (place.getVerticalExaggeration() == null || place.getVerticalExaggeration() == startExaggeration)
		{
			return;
		}

		final long startTime = System.currentTimeMillis();
		exaggerationChanger = new RenderingListener()
		{
			@Override
			public void stageChanged(RenderingEvent event)
			{
				long currentTime = System.currentTimeMillis();
				double percent = (currentTime - startTime) / (double) lengthMillis;
				percent = Math.max(0d, Math.min(1d, percent));
				boolean complete = percent >= 1d;

				double exaggeration = Util.mixDouble(percent, startExaggeration, place.getVerticalExaggeration());
				//wwd.getSceneController().setVerticalExaggeration(exaggeration);
				Settings.get().setVerticalExaggeration(exaggeration);

				if (complete)
				{
					wwd.removeRenderingListener(this);
				}
			}
		};
		wwd.addRenderingListener(exaggerationChanger);

		wwd.getInputHandler().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				wwd.removeRenderingListener(exaggerationChanger);
				wwd.getInputHandler().removeMouseListener(this);
			}
		});
	}

	private JFileChooser getChooser()
	{
		if (exportImportChooser == null)
		{
			exportImportChooser = new JFileChooser();
			exportImportChooser.setFileFilter(FileFilters.getXmlFilter());
		}
		return exportImportChooser;
	}

	private void importPlaces()
	{
		final JFileChooser chooser = getChooser();
		chooser.setMultiSelectionEnabled(true);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			for (File file : chooser.getSelectedFiles())
			{
				try
				{
					importPlaces(file);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "Could not import " + file.getName(), "Import error",
							JOptionPane.ERROR_MESSAGE);
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
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".xml"))
			{
				file = new File(file.getAbsolutePath() + ".xml");
			}
			if (file.exists())
			{
				int answer =
						JOptionPane.showConfirmDialog(frame, file.getAbsolutePath()
								+ " already exists.\nDo you want to replace it?", "Export", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
				if (answer != JOptionPane.YES_OPTION)
				{
					file = null;
				}
			}
			if (file != null)
			{
				try
				{
					exportPlaces(file);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "Error: " + e, "Export error", JOptionPane.ERROR_MESSAGE);
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
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
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
		frame = theme.getFrame();
		layer = new PlaceLayer(wwd, this);
		wwd.getModel().getLayers().add(layer);

		for (ThemePanel panel : theme.getPanels())
		{
			if (panel instanceof LayersPanel)
			{
				this.layersPanel = (LayersPanel) panel;
			}
		}

		if (!Util.isBlank(theme.getPlacesPersistanceFilename()))
		{
			placesPersistanceFilename = theme.getPlacesPersistanceFilename();
			loadPlaces(getPlacesFile(), false);
		}
		loadPlaces(Settings.getSettingsFile(), true);

		persistPlaces = theme.isPersistPlaces();

		// If no places were loaded, initialise from set path, if provided
		if (places.isEmpty() && theme.getPlacesInitialisationPath() != null)
		{
			try
			{
				loadPlaces(new File(theme.getPlacesInitialisationPath().toURI()), false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dispose()
	{
		if (persistPlaces)
		{
			savePlaces(getPlacesFile());
		}
	}

	public void setLayersFromLayersPanel(Place place)
	{
		if (layersPanel != null)
		{
			INode node = layersPanel.getRoot();
			//deep copy the layer hierarchy using it's own persistance mechanism
			try
			{
				Document document = LayerTreePersistance.saveToDocument(node);
				INode copy = LayerTreePersistance.readFromXML(document);
				removeAnyDisabled(copy);
				removeAnyUnneededProperties(copy);
				place.setLayers(copy);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void removeAnyUnneededProperties(INode node)
	{
		if (node instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) node;
			layer.setExpiryTime(null);
			layer.setLegendURL(null);
			layer.setQueryURL(null);
			layer.setEnabled(false);
		}

		node.setExpanded(false);
		node.setIconURL(null);
		node.setName(null);
		node.setInfoURL(null);

		for (int i = 0; i < node.getChildCount(); i++)
		{
			removeAnyUnneededProperties(node.getChild(i));
		}
	}

	private void removeAnyDisabled(INode node)
	{
		for (int i = 0; i < node.getChildCount(); i++)
		{
			INode child = node.getChild(i);
			if (!anyEnabled(child))
			{
				node.removeChild(child);
				i--;
			}
			else
			{
				//recurse into children
				removeAnyDisabled(child);
			}
		}
	}

	/**
	 * @return Whether any nodes in the tree given by the provided root node are
	 *         enabled
	 */
	private boolean anyEnabled(INode node)
	{
		if (node instanceof ILayerNode)
		{
			if (((ILayerNode) node).isEnabled())
			{
				return true;
			}
		}

		for (int i = 0; i < node.getChildCount(); i++)
		{
			if (anyEnabled(node.getChild(i)))
			{
				return true;
			}
		}

		return false;
	}

	private int getSelectedPlaceIndex()
	{
		ListItem item = (ListItem) list.getSelectedValue();
		int index = -1;
		if (item != null)
		{
			index = places.indexOf(item.place);
		}
		else if (!places.isEmpty())
		{
			index = 0;
		}
		return index;
	}
}
