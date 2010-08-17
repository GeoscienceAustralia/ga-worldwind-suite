package au.gov.ga.worldwind.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;

public interface Bounded
{
	public Sector getSector();

	public class Reader
	{
		public static Sector getSector(Object source)
		{
			if (source instanceof Bounded)
			{
				return ((Bounded) source).getSector();
			}
			else if (source instanceof AVList)
			{
				Object o = ((AVList) source).getValue(AVKey.SECTOR);
				if (o instanceof Sector)
					return (Sector) o;
			}

			return null;
		}
	}
}
