package layers.geonames;

import java.util.Iterator;

public class IterableProxy<E> implements Iterable<E>
{
	private Iterable<? extends E> source;

	public IterableProxy(Iterable<? extends E> source)
	{
		this.source = source;
	}

	public Iterator<E> iterator()
	{
		return new IteratorProxy<E>(source.iterator());
	}

	private class IteratorProxy<O> implements Iterator<O>
	{
		private Iterator<? extends O> iterator;

		public IteratorProxy(Iterator<? extends O> iterator)
		{
			this.iterator = iterator;
		}

		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		public O next()
		{
			return iterator.next();
		}

		public void remove()
		{
			iterator.remove();
		}
	}
}
