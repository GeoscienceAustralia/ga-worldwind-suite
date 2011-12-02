package au.gov.ga.worldwind.animator.application.render;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getRenderProgressDialogTitleKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getRenderProgressFrameMessageKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getRenderProgressStartingMessageKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Component;

import javax.swing.ProgressMonitor;

import au.gov.ga.worldwind.animator.application.render.AnimationRenderer.RenderEventListener;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A dialog box used to show the progress of a render task
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RenderProgressDialog implements RenderEventListener
{
	private AnimationRenderer renderer;
	private ProgressMonitor progressMonitor;
	private Component owner;
	
	/**
	 * Attach a render progress dialog to the given renderer, using the provided owner as the parent component
	 */
	public static void attachToRenderer(Component owner, AnimationRenderer renderer)
	{
		new RenderProgressDialog(owner, renderer);
	}
	
	public RenderProgressDialog(Component owner, AnimationRenderer renderer)
	{
		Validate.notNull(renderer, "A renderer is required");
		Validate.notNull(owner, "An owner frame must be provided");
		
		this.renderer = renderer;
		this.owner = owner;
		renderer.addListener(this);
	}

	@Override
	public void started()
	{
		progressMonitor = new ProgressMonitor(owner, getMessage(getRenderProgressDialogTitleKey()), getMessage(getRenderProgressStartingMessageKey()), 0, 100);
		progressMonitor.setMillisToPopup(0);
		progressMonitor.setMillisToDecideToPopup(0);
		progressMonitor.setProgress(0);
	}

	@Override
	public void startingFrame(int frame)
	{
		if (progressMonitor.isCanceled())
		{
			renderer.stop();
			progressMonitor.close();
		}
		else
		{
			progressMonitor.setNote(getMessage(getRenderProgressFrameMessageKey(), frame));
		}
	}

	@Override
	public void finishedFrame(int frame)
	{
		if (progressMonitor.isCanceled())
		{
			renderer.stop();
			progressMonitor.close();
		}
		else
		{
			progressMonitor.setProgress((int)(renderer.getPercentComplete() * 100d));
		}
	}

	@Override
	public void stopped(int frame)
	{
		progressMonitor.close();
	}

	@Override
	public void completed()
	{
		progressMonitor.close();
	}

}
