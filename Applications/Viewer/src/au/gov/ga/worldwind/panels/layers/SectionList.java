package au.gov.ga.worldwind.panels.layers;

import java.util.Collection;

/**
 * A SectionList allows an object to register as a section, and then whenever
 * addAllFromObject is called by that object, the collection is added in the
 * same relative position as the size of the list when registerSectionObject was
 * called. It is either implemented by:
 * <ol>
 * <li>
 * using a dummy object inserted when registerSectionObject is called, and
 * whenever addAllFromObject is called, the index of the dummy object is looked
 * up and the collection is inserted at that position.</li>
 * <li>
 * using sublists within the list.</li>
 * </ol>
 */
public interface SectionList<E>
{
	public void registerSectionObject(Object section);

	public void addAllFromSection(Object section, Collection<? extends E> c);

	public void removeAllFromSection(Object section, Collection<? extends E> c);
}
