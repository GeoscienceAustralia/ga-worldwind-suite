package au.gov.ga.worldwind.layers.local;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LocalLayers
{
	private final static String LOCAL_LAYERS_FILENAME = ".gaww.locallayers.xml";

	private static LocalLayers instance;

	public static void init(WorldWindow wwd)
	{
		instance = new LocalLayers(wwd);
	}

	public static LocalLayers get()
	{
		if (instance == null)
		{
			throw new IllegalStateException("Must call init() first");
		}
		return instance;
	}

	public static void save()
	{
		get().saveDefinitions();
	}

	private WorldWindow wwd;
	private List<LocalLayer> layers = new ArrayList<LocalLayer>();
	private List<LocalLayerDefinition> definitions = new ArrayList<LocalLayerDefinition>();
	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	private boolean ignoreChange = false;

	private LocalLayers(WorldWindow wwd)
	{
		this.wwd = wwd;
		loadDefinitions();
		LayerList wlayers = wwd.getModel().getLayers();
		for (LocalLayerDefinition def : definitions)
		{
			LocalLayer layer = def.createLayer();
			layers.add(layer);
			wlayers.add(layer);
		}
	}

	public void addLayer(LocalLayerDefinition def)
	{
		LocalLayer layer = def.createLayer();
		layers.add(layer);
		definitions.add(def);
		wwd.getModel().getLayers().add(layer);
		wwd.redraw();
		notifyChangeListeners();
	}

	public void insertLayer(LocalLayerDefinition def, int index)
	{
		if (index > layers.size())
			throw new IndexOutOfBoundsException();

		if (layers.isEmpty())
		{
			addLayer(def);
		}
		else
		{
			int firstIndex = wwd.getModel().getLayers().indexOf(layers.get(0));
			LocalLayer layer = def.createLayer();
			layers.add(index, layer);
			definitions.add(index, def);
			wwd.getModel().getLayers().add(firstIndex + index, layer);
			wwd.redraw();
			notifyChangeListeners();
		}
	}

	public void removeLayer(LocalLayerDefinition def)
	{
		int index = indexOfLayer(def);
		if (index < 0)
			return;
		removeLayer(index);
	}

	public void removeLayer(int index)
	{
		LocalLayer layer = layers.get(index);
		layers.remove(layer);
		definitions.remove(index);
		wwd.getModel().getLayers().remove(layer);
		wwd.redraw();
		notifyChangeListeners();
	}

	public int indexOfLayer(LocalLayerDefinition def)
	{
		return definitions.indexOf(def);
	}

	public void updateLayer(LocalLayerDefinition def)
	{
		int index = indexOfLayer(def);
		if (index < 0)
			return;
		ignoreChange = true;
		removeLayer(index);
		ignoreChange = false;
		insertLayer(def, index);
	}

	public List<LocalLayer> getLayers()
	{
		return layers;
	}

	public boolean isEmpty()
	{
		return definitions.isEmpty();
	}

	public int size()
	{
		return definitions.size();
	}

	public void addChangeListener(ChangeListener changeListener)
	{
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener)
	{
		changeListeners.remove(changeListener);
	}

	private void notifyChangeListeners()
	{
		if (ignoreChange)
			return;
		ChangeEvent e = new ChangeEvent(new Object());
		for (ChangeListener changeListener : changeListeners)
		{
			changeListener.stateChanged(e);
		}
	}

	//PERSISTANCE

	private void loadDefinitions()
	{
		Definitions def = null;
		File file = getSettingsFile();
		if (file.exists())
		{
			XMLDecoder xmldec = null;
			try
			{
				FileInputStream fis = new FileInputStream(file);
				xmldec = new XMLDecoder(fis);
				def = (Definitions) xmldec.readObject();
			}
			catch (Exception e)
			{
				def = null;
			}
			finally
			{
				if (xmldec != null)
					xmldec.close();
			}
		}
		if (def == null || def.getDefinitions() == null)
			definitions = new ArrayList<LocalLayerDefinition>();
		else
			definitions = def.getDefinitions();
	}

	private void saveDefinitions()
	{
		XMLEncoder xmlenc = null;
		try
		{
			Definitions def = new Definitions();
			def.setDefinitions(definitions);
			File file = getSettingsFile();
			FileOutputStream fos = new FileOutputStream(file);
			xmlenc = new XMLEncoder(fos);
			xmlenc.writeObject(def);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (xmlenc != null)
				xmlenc.close();
		}
	}

	private static File getSettingsFile()
	{
		String home = System.getProperty("user.home");
		return new File(new File(home), LOCAL_LAYERS_FILENAME);
	}

	public static class Definitions implements Serializable
	{
		private List<LocalLayerDefinition> definitions;

		public Definitions()
		{
		}

		public List<LocalLayerDefinition> getDefinitions()
		{
			return definitions;
		}

		public void setDefinitions(List<LocalLayerDefinition> definitions)
		{
			this.definitions = definitions;
		}
	}
}
