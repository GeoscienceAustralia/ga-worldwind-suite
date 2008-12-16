package bookmarks;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Bookmarks
{
	private static List<Bookmark> bookmarks;
	private static List<BookmarkListener> listeners = new ArrayList<BookmarkListener>();

	static
	{
		load();
	}

	private Bookmarks()
	{
	}

	public static void moveUp(Bookmark bookmark)
	{
		int index = bookmarks.indexOf(bookmark);
		if (index > 0)
		{
			bookmarks.remove(index);
			bookmarks.add(index - 1, bookmark);
			notifyListeners();
		}
	}

	public static void moveDown(Bookmark bookmark)
	{
		int index = bookmarks.indexOf(bookmark);
		if (index >= 0 && index < bookmarks.size() - 1)
		{
			bookmarks.remove(index);
			bookmarks.add(index + 1, bookmark);
			notifyListeners();
		}
	}

	public static void moveTo(Bookmark bookmark, int index)
	{
		index = Math.max(0, Math.min(bookmarks.size() - 1, index));
		int oldindex = bookmarks.indexOf(bookmark);
		if (oldindex >= 0)
		{
			bookmarks.remove(oldindex);
			bookmarks.add(index, bookmark);
			notifyListeners();
		}
	}

	public static void rename(Bookmark bookmark, String name)
	{
		bookmark.name = name;
		if (bookmarks.contains(bookmark))
		{
			notifyListeners();
		}
	}

	public static void delete(Bookmark bookmark)
	{
		if (bookmarks.contains(bookmark))
		{
			bookmarks.remove(bookmark);
			notifyListeners();
		}
	}

	public static void add(Bookmark bookmark)
	{
		bookmarks.add(bookmark);
		notifyListeners();
	}

	public static int size()
	{
		return bookmarks.size();
	}

	public static Bookmark get(int index)
	{
		return bookmarks.get(index);
	}

	public static Iterable<Bookmark> iterable()
	{
		return bookmarks;
	}

	public static void addBookmarkListener(BookmarkListener listener)
	{
		listeners.add(listener);
	}

	public static void removeBookmarkListener(BookmarkListener listener)
	{
		listeners.remove(listener);
	}

	private static void load()
	{
		bookmarks = loadObject();
	}

	public static void save()
	{
		saveObject(bookmarks);
	}

	private static void notifyListeners()
	{
		for (BookmarkListener listener : listeners)
		{
			listener.modified();
		}
	}

	private static File getFile()
	{
		return WorldWind.getDataFileCache().newFile("GA/Bookmarks/bookmarks.dat");
	}

	private static List<Bookmark> loadObject()
	{
		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
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
					for (SerializableBookmark sf : sl.list)
					{
						bookmarks.add(sf.toBookmark());
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		return bookmarks;
	}

	private static void saveObject(List<Bookmark> bookmarks)
	{
		List<SerializableBookmark> lsb = new ArrayList<SerializableBookmark>();
		for (Bookmark b : bookmarks)
		{
			lsb.add(SerializableBookmark.fromBookmark(b));
		}
		SerializableList sl = new SerializableList(lsb);
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

	private static class SerializableBookmark implements Serializable
	{
		public String name;
		public double lat;
		public double lon;
		public double elevation;
		public double heading;
		public double pitch;
		public double zoom;

		private SerializableBookmark(String name, double lat, double lon,
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

		public static SerializableBookmark fromBookmark(Bookmark b)
		{
			return new SerializableBookmark(b.name,
					b.center.getLatitude().degrees,
					b.center.getLongitude().degrees, b.center.getElevation(),
					b.heading.degrees, b.pitch.degrees, b.zoom);
		}

		public Bookmark toBookmark()
		{
			return new Bookmark(name,
					Position.fromDegrees(lat, lon, elevation), Angle
							.fromDegrees(heading), Angle.fromDegrees(pitch),
					zoom);
		}
	}

	private static class SerializableList implements Serializable
	{
		public List<SerializableBookmark> list;

		public SerializableList(List<SerializableBookmark> list)
		{
			this.list = list;
		}
	}
}
