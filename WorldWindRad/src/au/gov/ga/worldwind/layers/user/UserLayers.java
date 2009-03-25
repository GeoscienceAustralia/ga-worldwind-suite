package au.gov.ga.worldwind.layers.user;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.settings.Settings;

public class UserLayers
{
	private static WorldWindow wwd;
	private static List<UserLayer> layers = new ArrayList<UserLayer>();
	private static List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	private static boolean ignoreChange = false;

	public static void init(WorldWindow wwd)
	{
		UserLayers.wwd = wwd;
		LayerList wlayers = wwd.getModel().getLayers();
		List<UserLayerDefinition> userDefs = Settings.get().getUserLayers();
		for (UserLayerDefinition def : userDefs)
		{
			UserLayer layer = def.createLayer();
			layers.add(layer);
			wlayers.add(layer);
		}
	}

	public static void addUserLayer(UserLayerDefinition def)
	{
		Settings.get().getUserLayers().add(def);
		UserLayer layer = def.createLayer();
		layers.add(layer);
		wwd.getModel().getLayers().add(layer);
		wwd.redraw();
		notifyChangeListeners();
	}

	public static void insertUserLayer(UserLayerDefinition def, int index)
	{
		if (index > layers.size())
			throw new IndexOutOfBoundsException();

		if (layers.isEmpty())
		{
			addUserLayer(def);
		}
		else
		{
			int firstIndex = wwd.getModel().getLayers().indexOf(layers.get(0));
			UserLayer layer = def.createLayer();
			layers.add(index, layer);
			wwd.getModel().getLayers().add(firstIndex + index, layer);
			wwd.redraw();
			notifyChangeListeners();
		}
	}

	public static void removeUserLayer(UserLayerDefinition def)
	{
		int index = indexOfLayer(def);
		if (index < 0)
			return;
		removeUserLayer(index);
	}

	public static void removeUserLayer(int index)
	{
		UserLayer layer = layers.get(index);
		Settings.get().getUserLayers().remove(layer.getDefinition());
		layers.remove(layer);
		wwd.getModel().getLayers().remove(layer);
		wwd.redraw();
		notifyChangeListeners();
	}

	public static int indexOfLayer(UserLayerDefinition def)
	{
		for (int i = 0; i < layers.size(); i++)
		{
			if (layers.get(i).getDefinition() == def)
			{
				return i;
			}
		}
		return -1;
	}

	public static void updateUserLayer(UserLayerDefinition def)
	{
		int index = indexOfLayer(def);
		if (index < 0)
			return;
		ignoreChange = true;
		removeUserLayer(index);
		ignoreChange = false;
		insertUserLayer(def, index);
	}

	public static List<UserLayer> getLayers()
	{
		return layers;
	}

	public static boolean isEmpty()
	{
		return layers.isEmpty();
	}

	public static int size()
	{
		return layers.size();
	}

	public static void addChangeListener(ChangeListener changeListener)
	{
		changeListeners.add(changeListener);
	}

	public static void removeChangeListener(ChangeListener changeListener)
	{
		changeListeners.remove(changeListener);
	}

	private static void notifyChangeListeners()
	{
		if (ignoreChange)
			return;
		ChangeEvent e = new ChangeEvent(new Object());
		for (ChangeListener changeListener : changeListeners)
		{
			changeListener.stateChanged(e);
		}
	}
}
