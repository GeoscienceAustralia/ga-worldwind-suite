package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.render.DrawContext;

/**
 * Task which can be added to the {@link AnimatorSceneController} to run before
 * or after a repaint.
 * 
 * @author Michael de Hoog
 * @author James Navin
 */
public interface PaintTask
{
	/**
	 * Run the task; called by the SceneController.
	 * 
	 * @param dc
	 *            Current {@link DrawContext}
	 */
	void run(DrawContext dc);
}
