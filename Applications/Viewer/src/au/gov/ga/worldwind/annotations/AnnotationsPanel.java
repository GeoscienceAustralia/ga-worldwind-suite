package au.gov.ga.worldwind.annotations;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
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

import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.theme.AbstractThemePanel;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.util.BasicAction;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.Util;

public class AnnotationsPanel extends AbstractThemePanel
{
	private WorldWindow wwd;
	private JList list;
	private DefaultListModel model;
	private ListItem dragging;
	private AnnotationsLayer layer;
	private Window window;
	private boolean playing = false;
	private BasicAction addAction, editAction, deleteAction, playAction, importAction,
			exportAction;
	private JFileChooser exportImportChooser;

	private class ListItem
	{
		public final JCheckBox check;
		public final Annotation annotation;

		public ListItem(Annotation annotation, JCheckBox check)
		{
			this.annotation = annotation;
			this.check = check;
		}
	}

	public AnnotationsPanel()
	{
		super(new BorderLayout());

		createPanel();
		populateList();
	}

	private void createPanel()
	{
		addAction = new BasicAction("Add", "Add annotation", Icons.add.getIcon());
		addAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addNew();
			}
		});

		editAction = new BasicAction("Edit", "Edit selected", Icons.edit.getIcon());
		editAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editSelected();
			}
		});

		deleteAction = new BasicAction("Delete", "Delete selected", Icons.delete.getIcon());
		deleteAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteSelected();
			}
		});

		playAction = new BasicAction("Play", "Fly through all annotations", Icons.run.getIcon());
		playAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (playing)
					stopAnnotations();
				else
					playAnnotations();
			}
		});

		importAction = new BasicAction("Import", "Import annotations", Icons.imporrt.getIcon());
		importAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				importAnnotations();
			}
		});

		exportAction = new BasicAction("Export", "Export annotations", Icons.export.getIcon());
		exportAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				exportAnnotations();
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

		ListSelectionListener lsl = new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				enableActions();
				ListItem item = (ListItem) list.getSelectedValue();
				if (item != null)
					annotationSelected(item.annotation);
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

		stopAnnotations();
	}

	private void enableActions()
	{
		addAction.setEnabled(!playing);
		editAction.setEnabled(list.getSelectedIndex() >= 0 && !playing);
		deleteAction.setEnabled(list.getSelectedIndex() >= 0 && !playing);
	}

	private void annotationSelected(Annotation annotation)
	{
		layer.selectAnnotation(annotation);
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
			item.annotation.setVisible(item.check.isSelected());
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
			flyToAnnotation(item.annotation);
		}
	}

	private void populateList()
	{
		model.removeAllElements();
		for (Annotation annotation : Settings.get().getAnnotations())
		{
			addAnnotation(annotation);
		}
		list.repaint();
	}

	private void addAnnotation(Annotation annotation)
	{
		JCheckBox check = new JCheckBox("", annotation.isVisible());
		ListItem item = new ListItem(annotation, check);
		model.addElement(item);
	}

	private void addNew()
	{
		View view = wwd.getView();
		if (view instanceof OrbitView)
		{
			OrbitView orbitView = (OrbitView) view;
			Position pos = orbitView.getCenterPosition();
			double minZoom = orbitView.getZoom() * 5;
			Annotation annotation =
					new Annotation("", pos.getLatitude().degrees, pos.getLongitude().degrees,
							minZoom);
			annotation.setZoom(orbitView.getZoom());
			annotation.setHeading(orbitView.getHeading().degrees);
			annotation.setPitch(orbitView.getPitch().degrees);
			annotation.setSaveCamera(false);
			AnnotationEditor editor =
					new AnnotationEditor(wwd, window, "New annotation", annotation);
			int value = editor.getOkCancel();
			if (value == JOptionPane.OK_OPTION)
			{
				Settings.get().getAnnotations().add(annotation);
				addAnnotation(annotation);
				list.repaint();
				refreshLayer();
			}
		}
	}

	public void selectAnnotation(Annotation annotation)
	{
		if (annotation != null)
		{
			for (int i = 0; i < model.size(); i++)
			{
				ListItem item = (ListItem) model.get(i);
				if (item.annotation == annotation)
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
			Annotation editing = new Annotation(item.annotation);
			AnnotationEditor editor = new AnnotationEditor(wwd, window, "Edit annotation", editing);
			int value = editor.getOkCancel();
			if (value == JOptionPane.OK_OPTION)
			{
				item.annotation.setValuesFrom(editing);
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
							"Are you sure you want to delete the annotation '"
									+ item.annotation.getLabel() + "'?", "Delete annotation",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (value == JOptionPane.YES_OPTION)
			{
				model.removeElement(item);
				Settings.get().getAnnotations().remove(item.annotation);
				list.repaint();
				if (index >= model.getSize())
					index = model.getSize() - 1;
				list.setSelectedIndex(index);
				refreshLayer();
			}
		}
	}

	public void deleteAllAnnotations()
	{
		Settings.get().getAnnotations().clear();
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
			Settings.get().getAnnotations().remove(item.annotation);
			Settings.get().getAnnotations().add(index, item.annotation);
			list.setSelectedIndex(index);
			list.repaint();
		}
	}

	private void refreshLayer()
	{
		layer.refresh();
		wwd.redraw();
	}

	private synchronized void stopAnnotations()
	{
		if (wwd != null)
			wwd.getView().stopAnimations();
		playing = false;
		playAction.setIcon(Icons.run.getIcon());
		playAction.setToolTipText("Play through annotations");
		enableActions();
	}

	private synchronized void playAnnotations()
	{
		if (!playing)
		{
			playing = true;
			wwd.getInputHandler().addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					stopAnnotations();
					if (!playing)
					{
						wwd.getInputHandler().removeMouseListener(this);
					}
				}
			});
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					ListItem item = (ListItem) list.getSelectedValue();
					List<Annotation> annotations = Settings.get().getAnnotations();
					int index = -1;
					if (item != null)
					{
						index = annotations.indexOf(item.annotation);
					}
					else if (!annotations.isEmpty())
					{
						index = 0;
					}

					long jump = 100;
					while (playing && index >= 0)
					{
						Annotation annotation = annotations.get(index);
						if (!annotation.isExcludeFromPlaylist())
						{
							selectAnnotation(annotation);
							long length = flyToAnnotation(annotation);
							if (length < 0)
								break;
							length += Settings.get().getAnnotationsPause();

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
							if (++nextIndex >= annotations.size())
								nextIndex = 0;
							if (nextIndex == index)
							{
								index = -1;
								break;
							}
							if (!annotations.get(nextIndex).isExcludeFromPlaylist())
							{
								index = nextIndex;
								break;
							}
						}
					}
					stopAnnotations();
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
			thread.setName("Annotations");
			thread.setDaemon(true);
			thread.start();
		}

		playAction.setIcon(Icons.stop.getIcon());
		playAction.setToolTipText("Stop playback");
		enableActions();
	}

	private long flyToAnnotation(Annotation annotation)
	{
		View view = wwd.getView();
		if (view instanceof OrbitView)
		{
			OrbitView orbitView = (OrbitView) view;
			Position center = orbitView.getCenterPosition();
			Position newCenter =
					Position.fromDegrees(annotation.getLatitude(), annotation.getLongitude(), 0);
			long lengthMillis = Util.getScaledLengthMillis(center, newCenter);

			Angle heading = orbitView.getHeading();
			Angle pitch = orbitView.getPitch();
			double zoom = orbitView.getZoom();
			if (annotation.isSaveCamera())
			{
				zoom = annotation.getZoom();
				heading = Angle.fromDegrees(annotation.getHeading());
				pitch = Angle.fromDegrees(annotation.getPitch());
			}
			else
			{
				double minZoom = annotation.getMinZoom();
				double maxZoom = annotation.getMaxZoom();
				if (minZoom >= 0 && zoom > minZoom)
					zoom = Math.max(minZoom, 1000);
				else if (maxZoom >= 0 && zoom < maxZoom)
					zoom = maxZoom;
			}

			view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView, center,
					newCenter, orbitView.getHeading(), heading, orbitView.getPitch(), pitch,
					orbitView.getZoom(), zoom, lengthMillis, true));
			wwd.redraw();

			return lengthMillis;
		}
		return -1;
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

	private void importAnnotations()
	{
		final JFileChooser chooser = getChooser();
		chooser.setMultiSelectionEnabled(true);
		if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION)
		{
			for (File file : chooser.getSelectedFiles())
			{
				try
				{
					importAnnotations(file);
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

	private void importAnnotations(File file) throws Exception
	{
		if (file.exists())
		{
			XMLDecoder xmldec = null;
			try
			{
				FileInputStream fis = new FileInputStream(file);
				xmldec = new XMLDecoder(fis);
				List<Annotation> annotations = Settings.get().getAnnotations();
				List<?> newAnnotations = (List<?>) xmldec.readObject();
				if (newAnnotations != null)
				{
					for (Object object : newAnnotations)
					{
						if (object instanceof Annotation)
							annotations.add((Annotation) object);
					}
				}
			}
			catch (Exception e)
			{
				throw e;
			}
			finally
			{
				if (xmldec != null)
					xmldec.close();
			}
			populateList();
			refreshLayer();
		}
	}

	private void exportAnnotations()
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
					exportAnnotations(file);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(window, "Error: " + e, "Export error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void exportAnnotations(File file) throws Exception
	{
		XMLEncoder xmlenc = null;
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			xmlenc = new XMLEncoder(fos);
			xmlenc.writeObject(Settings.get().getAnnotations());
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (xmlenc != null)
				xmlenc.close();
		}
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

		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
		{
			Color background = isSelected ? list.getSelectionBackground() : list.getBackground();

			if (value instanceof ListItem)
			{
				final Annotation annotation = ((ListItem) value).annotation;
				final JCheckBox check = ((ListItem) value).check;
				if (panel.getComponentCount() != 1 || panel.getComponent(0) != check)
				{
					panel.removeAll();
					panel.add(check, BorderLayout.CENTER);
				}
				label.setText(annotation.getLabel());
				check.setBackground(background);
				check.setSelected(annotation.isVisible());
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
		layer = new AnnotationsLayer(wwd, this);
		wwd.getModel().getLayers().add(layer);

		window = SwingUtilities.getWindowAncestor(this);
	}

	@Override
	public void dispose()
	{
	}
}
