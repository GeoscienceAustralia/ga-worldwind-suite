package au.gov.ga.worldwind.animator.panels;

import java.awt.datatransfer.DataFlavor;

import au.gov.ga.worldwind.animator.animation.AnimationObject;

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
}
