package au.gov.ga.worldwind.animator.panels;

import java.awt.datatransfer.DataFlavor;
import java.net.URL;

import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;

/**
 * A class containing supported data transfer flavors in the Animator application.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class DataTransferFlavors
{
	private static final DataFlavor ANIMATION_OBJECT_FLAVOR = new DataFlavor(AnimationObject.class, "AnimationObject");
	public static DataFlavor getAnimationObjectFlavor() { return ANIMATION_OBJECT_FLAVOR; }
	
	public static DataFlavor getStringFlavor() { return DataFlavor.stringFlavor; }
	
	public static DataFlavor getFileListFlavor() { return DataFlavor.javaFileListFlavor; }
	
	private static final DataFlavor URL_FLAVOR = new DataFlavor(URL.class, "URL");
	public static DataFlavor getURLFlavor() { return URL_FLAVOR; }
	
	private static final DataFlavor LAYER_IDENTIFIER_FLAVOR = new DataFlavor(LayerIdentifier.class, "LayerIdentifier");
	public static DataFlavor getLayerIdentifierFlavor() { return LAYER_IDENTIFIER_FLAVOR; }
}
