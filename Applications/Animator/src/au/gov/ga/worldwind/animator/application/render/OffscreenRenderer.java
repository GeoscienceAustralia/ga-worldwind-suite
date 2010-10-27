package au.gov.ga.worldwind.animator.application.render;

import java.io.File;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * An {@link AnimationRenderer} that renders each frame of the animation to an offscreen texture, then
 * writes that texture to disk.
 *
 */
public class OffscreenRenderer extends AnimationRendererBase
{

	
	
	@Override
	protected void doPreRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRender(Animation animation, int frame, File targetFile, double detailHint, boolean alpha)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void doPostRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha)
	{
		// TODO Auto-generated method stub

	}

}
