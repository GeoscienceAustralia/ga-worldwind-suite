package au.gov.ga.worldwind.common.view.free;

import gov.nasa.worldwind.awt.ViewInputHandler;

/**
 * Input handler for the {@link FreeView}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface FreeViewInputHandler extends ViewInputHandler
{
	/**
	 * Rotate the camera by the given deltas. Rotations are applied in this
	 * order: pitch, heading, roll.
	 * 
	 * @param deltaHeading
	 *            Rotation about the y-axis
	 * @param deltaPitch
	 *            Rotation about the x-axis
	 * @param deltaRoll
	 *            Rotation about the z-axis
	 */
	void look(double deltaHeading, double deltaPitch, double deltaRoll);

	/**
	 * Move the camera by the given deltas.
	 * 
	 * @param deltaX
	 *            Distance to move along the x-axis
	 * @param deltaY
	 *            Distance to move along the y-axis
	 * @param deltaZ
	 *            Distance to move along the z-axis
	 */
	void move(double deltaX, double deltaY, double deltaZ);
}
