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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import au.gov.ga.worldwind.components.FlatJButton;
import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.Util;

public class AnnotationsPanel extends JPanel
{
	private WorldWindow wwd;
	private JList list;
	private DefaultListModel model;
	private ListItem dragging;
	private AnnotationsLayer layer;
	private Frame frame;
	private boolean playing = false;
	private FlatJButton addButton, editButton, deleteButton, playButton;

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

	public AnnotationsPanel(WorldWindow wwd, Frame frame)
	{
		this.wwd = wwd;
		this.frame = frame;
		createPanel();
		populateList();
		layer = new AnnotationsLayer(wwd, this);
		wwd.getModel().getLayers().add(layer);
	}

	private void createPanel()
	{
		setLayout(new BorderLayout());

		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());
		add(panel, BorderLayout.NORTH);

		addButton = new FlatJButton(Icons.add.getIcon());
		addButton.setToolTipText("Add annotation");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1d / 4d;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(addButton, c);
		addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addNew();
			}
		});

		editButton = new FlatJButton(Icons.edit.getIcon());
		editButton.setToolTipText("Edit selected");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1d / 4d;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(editButton, c);
		ActionListener editAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editSelected();
			}
		};
		editButton.addActionListener(editAL);

		deleteButton = new FlatJButton(Icons.delete.getIcon());
		deleteButton.setToolTipText("Delete selected");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 1d / 4d;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(deleteButton, c);
		ActionListener deleteAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteSelected();
			}
		};
		deleteButton.addActionListener(deleteAL);

		playButton = new FlatJButton(Icons.run.getIcon());
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 1 / 4d;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(playButton, c);
		playButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (playing)
					stopAnnotations();
				else
					playAnnotations();
			}
		});

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
				enableButtons();
				ListItem item = (ListItem) list.getSelectedValue();
				if (item != null)
					annotationSelected(item.annotation);
			}
		};
		list.getSelectionModel().addListSelectionListener(lsl);
		lsl.valueChanged(null);

		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem editMenu = new JMenuItem("Edit");
		popupMenu.add(editMenu);
		editMenu.addActionListener(editAL);
		JMenuItem deleteMenu = new JMenuItem("Delete");
		popupMenu.add(deleteMenu);
		deleteMenu.addActionListener(deleteAL);

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
							Rectangle checkRect = new Rectangle(rect.x, rect.y,
									rect.height, rect.height);
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

	private void enableButtons()
	{
		addButton.setEnabled(!playing);
		editButton.setEnabled(list.getSelectedIndex() >= 0 && !playing);
		deleteButton.setEnabled(list.getSelectedIndex() >= 0 && !playing);
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
			Annotation annotation = new Annotation("",
					pos.getLatitude().degrees, pos.getLongitude().degrees,
					minZoom);
			annotation.setZoom(orbitView.getZoom());
			annotation.setHeading(orbitView.getHeading().degrees);
			annotation.setPitch(orbitView.getPitch().degrees);
			annotation.setSaveCamera(false);
			AnnotationEditor editor = new AnnotationEditor(wwd, frame,
					"New annotation", annotation);
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
			AnnotationEditor editor = new AnnotationEditor(wwd, frame,
					"Edit annotation", editing);
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
			int value = JOptionPane.showConfirmDialog(this,
					"Are you sure you want to delete the annotation '"
							+ item.annotation.getLabel() + "'?",
					"Delete annotation", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
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
		wwd.getView().stopAnimations();
		playing = false;
		playButton.setIcon(Icons.run.getIcon());
		playButton.setToolTipText("Play through annotations");
		enableButtons();
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
					List<Annotation> annotations = Settings.get()
							.getAnnotations();
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
							if (!annotations.get(nextIndex)
									.isExcludeFromPlaylist())
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

		playButton.setIcon(Icons.stop.getIcon());
		playButton.setToolTipText("Stop playback");
		enableButtons();
	}

	private long flyToAnnotation(Annotation annotation)
	{
		View view = wwd.getView();
		if (view instanceof OrbitView)
		{
			OrbitView orbitView = (OrbitView) view;
			Position center = orbitView.getCenterPosition();
			Position newCenter = Position.fromDegrees(annotation.getLatitude(),
					annotation.getLongitude(), 0);
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

			view.addAnimator(FlyToOrbitViewAnimator
					.createFlyToOrbitViewAnimator(orbitView, center, newCenter,
							orbitView.getHeading(), heading, orbitView
									.getPitch(), pitch, orbitView.getZoom(),
							zoom, lengthMillis, true));
			wwd.redraw();

			return lengthMillis;
		}
		return -1;
	}

	public void importAnnotations(File file) throws Exception
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

	public void exportAnnotations(File file) throws Exception
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

	private class CheckboxListCellRenderer extends JPanel implements
			ListCellRenderer
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

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			Color background = isSelected ? list.getSelectionBackground()
					: list.getBackground();

			if (value instanceof ListItem)
			{
				final Annotation annotation = ((ListItem) value).annotation;
				final JCheckBox check = ((ListItem) value).check;
				if (panel.getComponentCount() != 1
						|| panel.getComponent(0) != check)
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
}
