package au.gov.ga.worldwind.animator.application.render;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.io.File;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.AnimatorSceneController;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An {@link AnimationRenderer} that works by applying the animation state, 
 * then taking a screenshot of the current viewport window.
 * <p/>
 * Has the advantage that animation state is updated as each frame is rendered, but suffers
 * from the disadvantage that the viewport window *must* be kept as the foremost window in the
 * user's desktop.
 */
public class ViewportScreenshotRenderer extends AnimationRendererBase
{
	private WorldWindow worldWindow;
	private Animator targetApplication;
	private AnimatorSceneController animatorSceneController;
	
	private boolean detectCollisions;
	private double detailHintBackup;
	private boolean wasImmediate;
	
	public ViewportScreenshotRenderer(WorldWindow wwd, Animator targetApplication)
	{
		Validate.notNull(wwd, "A world window is required");
		Validate.notNull(targetApplication, "An Animator application is required");
		
		this.worldWindow = wwd;
		this.targetApplication = targetApplication;
		this.animatorSceneController = (AnimatorSceneController)wwd.getSceneController();
	}
	
	@Override
	protected void doPreRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha)
	{
		wasImmediate = ImmediateMode.isImmediate();
		ImmediateMode.setImmediate(true);

		targetApplication.resizeWindowToRenderDimensions();
		targetApplication.disableUtilityLayers();

		detailHintBackup = targetApplication.getDetailedElevationModel().getDetailHint();
		targetApplication.getDetailedElevationModel().setDetailHint(detailHint);

		OrbitView orbitView = (OrbitView) worldWindow.getView();
		detectCollisions = orbitView.isDetectCollisions();
		orbitView.setDetectCollisions(false);
		
		targetApplication.getFrame().setAlwaysOnTop(true);
	}
	
	@Override
	protected void doRender(Animation animation, int frame, File targetFile, double detailHint, boolean alpha)
	{
		targetApplication.setSlider(frame);
		animation.applyFrame(frame);

		animatorSceneController.takeScreenshot(targetFile, alpha);
		worldWindow.redraw();
		animatorSceneController.waitForScreenshot();
	}
	
	@Override
	protected void doPostRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha)
	{
		targetApplication.reenableUtilityLayers();
		
		targetApplication.getDetailedElevationModel().setDetailHint(detailHintBackup);
		((OrbitView)worldWindow.getView()).setDetectCollisions(detectCollisions);
		ImmediateMode.setImmediate(wasImmediate);
		
		targetApplication.getFrame().setAlwaysOnTop(false);
	}

}
