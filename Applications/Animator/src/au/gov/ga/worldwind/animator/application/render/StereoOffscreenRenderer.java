package au.gov.ga.worldwind.animator.application.render;

import java.io.File;

import gov.nasa.worldwind.WorldWindow;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.camera.StereoCamera;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.common.view.stereo.StereoView;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;

public class StereoOffscreenRenderer extends OffscreenRenderer
{
	public StereoOffscreenRenderer(WorldWindow wwd, Animator targetApplication)
	{
		super(wwd, targetApplication);
	}

	@Override
	protected void renderFrame(int frame, Animation animation, File outputDir, String frameName,
			int numeralPadLength, double detailHint, boolean alpha)
	{
		//if the view is not a stereo view, then just render with the super method
		boolean stereo = wwd.getView() instanceof StereoView && animation.getCamera() instanceof StereoCamera;
		if (!stereo)
		{
			super.renderFrame(frame, animation, outputDir, frameName, numeralPadLength, detailHint,
					alpha);
			return;
		}

		StereoView view = (StereoView) wwd.getView();
		view.setup(true, Eye.LEFT);

		File targetFile =
				createFileForFrame(frame, outputDir, frameName, numeralPadLength, Eye.LEFT);
		doRender(animation, frame, targetFile, detailHint, alpha);

		view.setup(true, Eye.RIGHT);

		targetFile = createFileForFrame(frame, outputDir, frameName, numeralPadLength, Eye.RIGHT);
		doRender(animation, frame, targetFile, detailHint, alpha);

		view.setup(false, Eye.LEFT);
	}

	protected File createFileForFrame(int frame, File outputDir, String frameName,
			int numeralPadLength, Eye eye)
	{
		String eyeString = eye == Eye.LEFT ? "left" : "right";
		File dir = new File(outputDir, frameName + "_" + eyeString);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return new File(dir, createImageSequenceName(frameName, frame, numeralPadLength));
	}
}
