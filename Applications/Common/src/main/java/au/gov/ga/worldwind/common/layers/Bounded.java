package au.gov.ga.worldwind.common.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;

/**
 * Represents an object that can be bounded by a sector.
 * 
 * @author Michael de Hoog
 */
public interface Bounded
{
	/**
	 * @return This object's bounds
	 */
	public Sector getSector();

	/**
	 * Utility class which reads a sector from an Object.
	 */
	public class Reader
	{
		/**
		 * Get source's bounds. Checks if object is an instance of Bounded,
		 * otherwise checks if the object is an AVList and contains a value for
		 * AVKey.SECTOR.
		 * 
		 * @param source
		 * @return source's bounds, or null if they couldn't be determined
		 */
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
