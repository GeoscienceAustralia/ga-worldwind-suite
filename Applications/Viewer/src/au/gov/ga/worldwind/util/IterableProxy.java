package au.gov.ga.worldwind.util;

import java.util.Iterator;

public class IterableProxy<E> implements Iterable<E>
{
	private Iterable<? extends E> source;

	public IterableProxy(Iterable<? extends E> source)
	{
		this.source = source;
	}

	@Override
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

		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		@Override
		public O next()
		{
			return iterator.next();
		}

		@Override
		public void remove()
		{
			iterator.remove();
		}
	}
}
