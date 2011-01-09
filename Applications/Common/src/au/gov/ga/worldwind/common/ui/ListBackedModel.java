package au.gov.ga.worldwind.common.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * An implementation of the {@link ListModel} interface that is backed by a {@link List}.
 * <p/>
 * Add and remove events are fired when elements are added or removed from the backing list.
 */
public class ListBackedModel<T> extends AbstractListModel implements List<T>
{
	private static final long serialVersionUID = 20100910L;
	
	private List<T> backingList = new ArrayList<T>();

	@Override
	public boolean contains(Object object)
	{
		return backingList.contains(object);
	}
	
	@Override
	public boolean add(T object)
	{
		boolean result = backingList.add(object);
		fireIntervalAdded(this, backingList.size() - 1, backingList.size());
		
		return result;
	}
	
	@Override
	public void add(int index, T object)
	{
		backingList.add(index, object);
		fireIntervalAdded(this, backingList.size() - 1, backingList.size());
	}
	
	@Override
	public boolean remove(Object object)
	{
		boolean elementRemoved = backingList.remove(object);
		if (elementRemoved)
		{
			fireIntervalRemoved(this,  backingList.size(),  Math.max(0, backingList.size() - 1));
		}
		
		return elementRemoved;
	}
	
	@Override
	public T remove(int index)
	{
		T removedElement = backingList.remove(index);
		if (removedElement != null)
		{
			fireIntervalRemoved(this,  backingList.size(),  backingList.size() - 1);
		}
		
		return removedElement;
	}
	
	@Override
	public int getSize()
	{
		return backingList.size();
	}

	@Override
	public int size()
	{
		return getSize();
	}
	
	@Override
	public Object getElementAt(int index)
	{
		return backingList.get(index);
	}

	@Override
	public boolean isEmpty()
	{
		return getSize() == 0;
	}

	@Override
	public Iterator<T> iterator()
	{
		return backingList.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return backingList.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a)
	{
		return backingList.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return backingList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		boolean listChanged = backingList.addAll(c);
		if (listChanged)
		{
			fireIntervalAdded(this, backingList.size() - 1, backingList.size());
		}
		return listChanged;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		boolean listChanged = backingList.addAll(index, c);
		if (listChanged)
		{
			fireIntervalAdded(this, backingList.size() - 1, backingList.size());
		}
		return listChanged;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean listChanged = backingList.removeAll(c);
		if (listChanged)
		{
			fireIntervalRemoved(this, backingList.size(), backingList.size() - 1);
		}
		return listChanged;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		boolean listChanged = backingList.retainAll(c);
		if (listChanged)
		{
			fireIntervalRemoved(this, backingList.size(), backingList.size() - 1);
		}
		return listChanged;
	}

	@Override
	public void clear()
	{
		backingList.clear();
		fireIntervalRemoved(this, backingList.size(), backingList.size());
	}

	@Override
	public T get(int index)
	{
		return backingList.get(index);
	}

	@Override
	public T set(int index, T element)
	{
		T result = backingList.set(index, element);
		fireContentsChanged(this, index, index);
		return result;
	}

	@Override
	public int indexOf(Object o)
	{
		return backingList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return backingList.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return backingList.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index)
	{
		return backingList.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		return backingList.subList(fromIndex, toIndex);
	}
	
}