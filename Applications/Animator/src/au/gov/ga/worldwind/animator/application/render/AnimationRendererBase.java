package au.gov.ga.worldwind.animator.application.render;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.animator.util.FileUtil;

/**
 * A base class for animation renderers
 */
public abstract class AnimationRendererBase implements AnimationRenderer
{
	private List<RenderEventListener> listeners = new ArrayList<RenderEventListener>();
	
	private int currentFrame;
	private double completedPercentage = 0.0;
	
	private AtomicBoolean started = new AtomicBoolean(false);
	private AtomicBoolean stop = new AtomicBoolean(true);
	private AtomicBoolean done = new AtomicBoolean(false);

	@Override
	public void stop()
	{
		if (!stop.getAndSet(true))
		{
			notifyStopped(currentFrame);
		}
	}

	@Override
	public final void render(final Animation animation, final int firstFrame, final int lastFrame, final File outputDir, final String frameName, final double detailHint, final boolean alpha)
	{
		resetRenderFlags();
		if (animation == null || !animation.hasKeyFrames() || lastFrame < firstFrame)
		{
			return;
		}
		
		Runnable renderTask = new Runnable()
		{
			@Override
			public void run()
			{
				renderOnThread(animation, firstFrame, lastFrame, outputDir, frameName, detailHint, alpha);
			}
		};

		Thread renderThread = DaemonThreadFactory.newThread(renderTask, "Animator render thread");
		renderThread.start();
	}
	
	protected void renderOnThread(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha)
	{
		notifyStarted();
		doPreRender(animation, firstFrame, lastFrame, outputDir, frameName, detailHint, alpha);
		
		int numeralPadLength = String.valueOf(lastFrame).length();
		
		for (int frame = firstFrame; frame <= lastFrame; frame ++)
		{
			currentFrame = frame;
			notifyStartingFrame(frame);
			
			renderFrame(frame, animation, outputDir, frameName, numeralPadLength, detailHint, alpha);
			
			notifyFinishedFrame(frame);
			completedPercentage = (double)(frame - firstFrame) / (double)(lastFrame - firstFrame);
			
			if (isStopped())
			{
				break;
			}
		}
		
		doPostRender(animation, firstFrame, lastFrame, outputDir, frameName, detailHint, alpha);
		notifyCompleted(lastFrame);
	}
	
	protected void renderFrame(int frame, Animation animation, File outputDir, String frameName, int numeralPadLength, double detailHint, boolean alpha)
	{
		File targetFile = createFileForFrame(frame, outputDir, frameName, numeralPadLength);
		doRender(animation, frame, targetFile, detailHint, alpha);
	}

	private void resetRenderFlags()
	{
		stop.set(false);
		done.set(false);
		started.set(false);
	}
	
	protected File createFileForFrame(int frame, File outputDir, String frameName, int numeralPadLength)
	{
		return new File(outputDir, createImageSequenceName(frameName, frame, numeralPadLength));
	}
	
	/**
	 * @return The name of a file in an image sequence, of the form <code>{prefix}{padded sequence number}.tga</code>
	 */
	protected String createImageSequenceName(String prefix, int sequenceNumber, int padTo)
	{
		return prefix + FileUtil.paddedInt(sequenceNumber, padTo) + ".tga";
	}
	
	/**
	 * Perform any required pre-render setup
	 */
	protected abstract void doPreRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha);

	
	/**
	 * Render the given frame of the given animation into the given file
	 */
	protected abstract void doRender(Animation animation, int frame, File targetFile, double detailHint, boolean alpha);
	
	/**
	 * Perform any required post-render cleanup
	 */
	protected abstract void doPostRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha);
	
	@Override
	public boolean isDone()
	{
		return done.get();
	}

	protected boolean isStarted()
	{
		return started.get();
	}
	
	protected boolean isStopped()
	{
		return stop.get();
	}
	
	@Override
	public double getPercentComplete()
	{
		if (isDone())
		{
			return 1;
		}
		if (isStarted())
		{
			return 0;
		}
		return completedPercentage;
	}

	@Override
	public void addListener(RenderEventListener listener)
	{
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(RenderEventListener listener)
	{
		this.listeners.remove(listener);
	}

	public void notifyStartingFrame(int frame)
	{
		for (int i = listeners.size()-1; i >= 0; i--)
		{
			listeners.get(i).startingFrame(frame);
		}
	}
	
	public void notifyFinishedFrame(int frame)
	{
		for (int i = listeners.size()-1; i >= 0; i--)
		{
			listeners.get(i).finishedFrame(frame);
		}
	}
	
	public void notifyStopped(int frame)
	{
		for (int i = listeners.size()-1; i >= 0; i--)
		{
			listeners.get(i).stopped(frame);
		}
	}
	
	public void notifyStarted()
	{
		for (int i = listeners.size()-1; i >= 0; i--)
		{
			listeners.get(i).started();
		}
	}
	
	public void notifyCompleted(int frame)
	{
		for (int i = listeners.size()-1; i >= 0; i--)
		{
			listeners.get(i).completed();
		}
	}
	
}
