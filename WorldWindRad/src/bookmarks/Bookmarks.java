package bookmarks;

import java.util.ArrayList;
import java.util.List;

import settings.Settings;

public class Bookmarks
{
	private static List<BookmarkListener> listeners = new ArrayList<BookmarkListener>();

	private Bookmarks()
	{
	}
	
	public static List<Bookmark> list()
	{
		return Settings.get().getBookmarks();
	}

	public static void moveUp(Bookmark bookmark)
	{
		int index = list().indexOf(bookmark);
		if (index > 0)
		{
			list().remove(index);
			list().add(index - 1, bookmark);
			notifyListeners();
		}
	}

	public static void moveDown(Bookmark bookmark)
	{
		int index = list().indexOf(bookmark);
		if (index >= 0 && index < list().size() - 1)
		{
			list().remove(index);
			list().add(index + 1, bookmark);
			notifyListeners();
		}
	}

	public static void moveTo(Bookmark bookmark, int index)
	{
		index = Math.max(0, Math.min(list().size() - 1, index));
		int oldindex = list().indexOf(bookmark);
		if (oldindex >= 0)
		{
			list().remove(oldindex);
			list().add(index, bookmark);
			notifyListeners();
		}
	}

	public static void rename(Bookmark bookmark, String name)
	{
		bookmark.setName(name);
		if (list().contains(bookmark))
		{
			notifyListeners();
		}
	}

	public static void delete(Bookmark bookmark)
	{
		if (list().contains(bookmark))
		{
			list().remove(bookmark);
			notifyListeners();
		}
	}

	public static void add(Bookmark bookmark)
	{
		list().add(bookmark);
		notifyListeners();
	}

	public static int size()
	{
		return list().size();
	}

	public static Bookmark get(int index)
	{
		return list().get(index);
	}

	public static Iterable<Bookmark> iterable()
	{
		return list();
	}

	public static void addBookmarkListener(BookmarkListener listener)
	{
		listeners.add(listener);
	}

	public static void removeBookmarkListener(BookmarkListener listener)
	{
		listeners.remove(listener);
	}

	private static void notifyListeners()
	{
		for (BookmarkListener listener : listeners)
		{
			listener.modified();
		}
	}
}
