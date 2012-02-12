package au.gov.ga.worldwind.common.util;

import java.util.Iterator;

/**
 * Provides the ability to create an {@link Iterable} of a subclass from an
 * {@link Iterable} of one of its superclasses.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 */
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
