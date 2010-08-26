package au.gov.ga.worldwind.animator.animation.io;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

/**
 * An interface for types that can be saved to XML
 * <p/>
 * <b>Important:</b> All implementations of {@link XmlSerializable} must provide a no-arg default
 * constructor. This can be private to protect class contracts etc, but it must exist.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface XmlSerializable<S>
{
	/**
	 * Create and return an XML element from this {@link XmlSerializable} object.
	 * <p/>
	 * The returned {@link Element} will be set as a child of the provided parent.
	 * 
	 * @param parent The parent element for this {@link XmlSerializable}
	 * 
	 * @return The element that represents this object
	 */
	Element toXml(Element parent);

	/**
	 * Create and return a new instance of this class from the provided {@link Element}.
	 * 
	 * @param element The element to de-serialise from
	 * @param versionId The ID of the version the provided element is in
	 * @param context The context needed to de-serialise the object.
	 * 
	 * @return The de-serialised object
	 */
	S fromXml(Element element, String versionId, AVList context);
}
