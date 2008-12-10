package panels.favourite;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.FlatJButton;
import util.Icons;
import util.Util;

public class FavouritePanel extends JPanel
{
	private WorldWindow wwd;
	private DefaultListModel listModel;
	private JList list;
	private FlatJButton edit;
	private FlatJButton delete;

	public FavouritePanel(final WorldWindow wwd)
	{
		super(new GridBagLayout());
		GridBagConstraints c;

		this.wwd = wwd;

		listModel = new DefaultListModel();
		list = new JList(listModel);
		//list.setCellRenderer(new CustomRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		add(scrollPane, c);

		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				selectionChanged();
				if (e.getClickCount() == 2)
				{
					flyToSelection();
				}
			}
		});

		JPanel panel = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		FlatJButton add = new FlatJButton(Icons.add);
		panel.add(add);
		add.setToolTipText("Add");
		add.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog(FavouritePanel.this,
						"Please enter a name", "Add favourite",
						JOptionPane.QUESTION_MESSAGE);
				if (name != null && name.length() > 0)
				{
					View view = wwd.getView();
					if (view instanceof OrbitView)
					{
						OrbitView orbitView = (OrbitView) view;
						Favourite favourite = new Favourite(name, orbitView
								.getCenterPosition(), orbitView.getHeading(),
								orbitView.getPitch(), orbitView.getZoom());
						listModel.addElement(favourite);
					}
					selectionChanged();
				}
			}
		});

		edit = new FlatJButton(Icons.edit);
		panel.add(edit);
		edit.setToolTipText("Rename selected");
		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object o = list.getSelectedValue();
				if (o instanceof Favourite)
				{
					Favourite f = (Favourite) o;
					Object name = JOptionPane.showInputDialog(
							FavouritePanel.this, "Enter the new name",
							"Rename favourite", JOptionPane.QUESTION_MESSAGE,
							null, null, f.name);
					if (name instanceof String)
					{
						f.name = (String) name;
						list.repaint();
					}
				}
			}
		});

		delete = new FlatJButton(Icons.delete);
		panel.add(delete);
		delete.setToolTipText("Delete selected");
		delete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int index = list.getSelectedIndex();
				if (index >= 0)
				{
					Object o = list.getSelectedValue();
					int value = JOptionPane.YES_OPTION;
					if (o instanceof Favourite)
					{
						value = JOptionPane.showConfirmDialog(
								FavouritePanel.this,
								"Are you sure you want to delete the favourite '"
										+ ((Favourite) o).name + "'?",
								"Delete favourite", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
					}
					if (value == JOptionPane.YES_OPTION)
					{
						listModel.remove(index);
					}
				}
				selectionChanged();
			}
		});

		load();
		selectionChanged();
	}

	private void flyToSelection()
	{
		Object o = list.getSelectedValue();
		if (o instanceof Favourite)
		{
			Favourite f = (Favourite) o;

			View view = wwd.getView();
			if (view instanceof OrbitView)
			{
				OrbitView orbitView = (OrbitView) view;
				Position center = orbitView.getCenterPosition();
				long lengthMillis = Util.getScaledLengthMillis(center
						.getLatLon(), f.center.getLatLon(), 2000, 8000);

				ViewStateIterator vsi = FlyToOrbitViewStateIterator
						.createPanToIterator(wwd.getModel().getGlobe(), center,
								f.center, orbitView.getHeading(), f.heading,
								orbitView.getPitch(), f.pitch, orbitView
										.getZoom(), f.zoom, lengthMillis, true);

				view.applyStateIterator(vsi);
			}
		}
	}

	private void selectionChanged()
	{
		boolean enabled = list.getSelectedIndex() >= 0;
		edit.setEnabled(enabled);
		delete.setEnabled(enabled);
	}

	private File getFile()
	{
		return WorldWind.getDataFileCache().newFile("favourites.fav");
	}

	public void load()
	{
		listModel.clear();
		List<Favourite> favourites = loadObject();
		for (Favourite f : favourites)
		{
			listModel.addElement(f);
		}
	}

	public void save()
	{
		List<Favourite> favourites = new ArrayList<Favourite>();
		for (int i = 0; i < listModel.getSize(); i++)
		{
			Object o = listModel.get(i);
			if (o instanceof Favourite)
			{
				favourites.add((Favourite) o);
			}
		}
		saveObject(favourites);
	}

	private List<Favourite> loadObject()
	{
		List<Favourite> favourites = new ArrayList<Favourite>();
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					getFile()));
			Object object = ois.readObject();
			if (object instanceof SerializableList)
			{
				SerializableList sl = (SerializableList) object;
				if (sl.list != null)
				{
					for (SerializableFavourite sf : sl.list)
					{
						favourites.add(Favourite.fromSerializable(sf));
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		return favourites;
	}

	private void saveObject(List<Favourite> favourites)
	{
		List<SerializableFavourite> lsf = new ArrayList<SerializableFavourite>();
		for (Favourite f : favourites)
		{
			lsf.add(f.toSerializable());
		}
		SerializableList sl = new SerializableList(lsf);
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(getFile()));
			oos.writeObject(sl);
		}
		catch (Exception e)
		{
		}
	}

	private static class Favourite
	{
		public String name;
		public Position center;
		public Angle heading;
		public Angle pitch;
		public double zoom;

		public Favourite(String name, Position center, Angle heading,
				Angle pitch, double zoom)
		{
			this.name = name;
			this.center = center;
			this.heading = heading;
			this.pitch = pitch;
			this.zoom = zoom;
		}

		public SerializableFavourite toSerializable()
		{
			return new SerializableFavourite(name,
					center.getLatitude().degrees,
					center.getLongitude().degrees, center.getElevation(),
					heading.degrees, pitch.degrees, zoom);
		}

		public static Favourite fromSerializable(SerializableFavourite sf)
		{
			return new Favourite(sf.name, Position.fromDegrees(sf.lat, sf.lon,
					sf.elevation), Angle.fromDegrees(sf.heading), Angle
					.fromDegrees(sf.pitch), sf.zoom);
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private static class SerializableFavourite implements Serializable
	{
		public String name;
		public double lat;
		public double lon;
		public double elevation;
		public double heading;
		public double pitch;
		public double zoom;

		public SerializableFavourite(String name, double lat, double lon,
				double elevation, double heading, double pitch, double zoom)
		{
			this.name = name;
			this.lat = lat;
			this.lon = lon;
			this.elevation = elevation;
			this.heading = heading;
			this.pitch = pitch;
			this.zoom = zoom;
		}
	}

	private static class SerializableList implements Serializable
	{
		public List<SerializableFavourite> list;

		public SerializableList(List<SerializableFavourite> list)
		{
			this.list = list;
		}
	}
}
