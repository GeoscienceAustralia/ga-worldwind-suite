package au.gov.ga.worldwind.common.layers.delegate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

/**
 * Factory that creates instances of delegates from string definitions. Also
 * handles inserting class replacements for certain delegate classes.
 * <p>
 * All DelegateFactory instances should be implemented as singletons. This
 * function returns the singleton instance of this DelegateFactory.
 * 
 * @author Michael de Hoog
 */
public interface IDelegateFactory
{
	/**
	 * Register a delegate class.
	 * 
	 * @param delegateClass
	 *            Class to register
	 */
	void registerDelegate(Class<? extends IDelegate> delegateClass);

	/**
	 * Register a delegate class which is to be replaced by another delegate in
	 * the createDelegate() method. The replacement class should be able to be
	 * instanciated by the same string definition as the class it is replacing.
	 * 
	 * @param fromClass
	 *            Class to be replaced
	 * @param toClass
	 *            Class to replace fromClass with
	 */
	void registerReplacementClass(Class<? extends IDelegate> fromClass, Class<? extends IDelegate> toClass);

	/**
	 * Create a new delgate from a string definition.
	 * 
	 * @param definition
	 * @return New delegate corresponding to {@code definition}
	 */
	IDelegate createDelegate(String definition, Element layerElement, AVList params);
}
