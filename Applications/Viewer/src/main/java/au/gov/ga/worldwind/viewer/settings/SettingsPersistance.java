package au.gov.ga.worldwind.viewer.settings;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SettingsPersistance
{
	//TODO don't use XMLEncoder, but rather write a custom XML file
	
	public static Settings load(Object source)
	{
		/*Settings settings = new Settings();

		Element element = XMLUtil.getElementFromSource(source);
		if (element != null)
		{
		}
		
		return settings;*/
		
		
		
		Settings settings = null;
		File file = (File) source;
		if (file.exists())
		{
			XMLDecoder xmldec = null;
			try
			{
				FileInputStream fis = new FileInputStream(file);
				xmldec = new XMLDecoder(fis);
				settings = (Settings) xmldec.readObject();
			}
			catch (Exception e)
			{
				settings = null;
			}
			finally
			{
				if (xmldec != null)
					xmldec.close();
			}
		}
		return settings;
	}

	public static void save(Settings settings, File file)
	{
		XMLEncoder xmlenc = null;
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			xmlenc = new XMLEncoder(fos);
			xmlenc.writeObject(settings);
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
}
