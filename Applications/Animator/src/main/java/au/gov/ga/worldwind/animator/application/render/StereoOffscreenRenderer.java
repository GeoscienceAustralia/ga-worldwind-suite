package au.gov.ga.worldwind.animator.application.render;

import java.io.File;

import gov.nasa.worldwind.WorldWindow;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.animation.camera.StereoCamera;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.common.view.stereo.StereoView;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;

/**
 * An extension of the basic {@link OffscreenRenderer} that supports a stereo view,
 * rendering two file sequences when stereo is enabled, one for the left and right eye.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StereoOffscreenRenderer extends OffscreenRenderer
{
	public StereoOffscreenRenderer(WorldWindow wwd, Animator targetApplication)
	{
		super(wwd, targetApplication);
	}

	@Override
	protected void renderFrame(int frame, Animation animation, RenderParameters renderParams)
	{
		//if the view is not a stereo view, then just render with the super method
		boolean stereo = wwd.getView() instanceof StereoView && animation.getCamera() instanceof StereoCamera;
		if (!stereo)
		{
			super.renderFrame(frame, animation, renderParams);
			return;
		}

		StereoView view = (StereoView) wwd.getView();
		view.setup(true, Eye.LEFT);

		File targetFile = AnimationImageSequenceNameFactory.createStereoImageSequenceFile(animation, frame, renderParams.getFrameName(), renderParams.getRenderDirectory(), Eye.LEFT);
		doRender(frame, targetFile, animation, renderParams);

		view.setup(true, Eye.RIGHT);

		targetFile = AnimationImageSequenceNameFactory.createStereoImageSequenceFile(animation, frame, renderParams.getFrameName(), renderParams.getRenderDirectory(), Eye.RIGHT);
		doRender(frame, targetFile, animation, renderParams);

		view.setup(false, Eye.LEFT);
	}

}
