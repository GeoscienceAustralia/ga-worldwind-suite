/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.application.render;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.common.util.DaemonThreadFactory;
import au.gov.ga.worldwind.common.view.target.ITargetView;

/**
 * A base class for animation renderers
 * 
 * @author James Navin (james.navin@ga.gov.au)
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
	public final void render(Animation animation)
	{
		render(animation, animation.getRenderParameters());
	}
	
	@Override
	public final Thread render(final Animation animation, final RenderParameters renderParams)
	{
		resetRenderFlags();
		if (animation == null || !animation.hasKeyFrames() || 
				!renderParams.isFrameRangeSet() || renderParams.getEndFrame() < renderParams.getStartFrame())
		{
			return null;
		}
		
		Runnable renderTask = new Runnable()
		{
			@Override
			public void run()
			{
				renderOnThread(animation, renderParams);
			}
		};

		Thread renderThread = DaemonThreadFactory.newThread(renderTask, "Animator render thread");
		renderThread.start();
		return renderThread;
	}
	
	protected void renderOnThread(Animation animation, RenderParameters renderParams)
	{
		notifyStarted();
		preRender(animation, renderParams);
		
		for (int frame = renderParams.getStartFrame(); frame <= renderParams.getEndFrame(); frame ++)
		{
			currentFrame = frame;
			notifyStartingFrame(frame);
			
			renderFrame(frame, animation, renderParams);
			
			notifyFinishedFrame(frame);
			completedPercentage = (double)(frame - renderParams.getStartFrame()) / (double)(renderParams.getEndFrame() - renderParams.getStartFrame());
			
			if (isStopped())
			{
				break;
			}
		}
		
		postRender(animation, renderParams);
		notifyCompleted(renderParams.getEndFrame());
	}
	
	protected void renderFrame(int frame, Animation animation, RenderParameters renderParams)
	{
		File targetFile = AnimationImageSequenceNameFactory.createImageSequenceFile(animation, frame, 
																					renderParams.getFrameName(), 
																					renderParams.getRenderDirectory());
		doRender(frame, targetFile, animation, renderParams);
	}

	private void resetRenderFlags()
	{
		stop.set(false);
		done.set(false);
		started.set(false);
	}
	
	protected void preRender(Animation animation, RenderParameters renderParams)
	{
		if(animation.getView() instanceof ITargetView)
		{
			((ITargetView) animation.getView()).setDrawAxisMarker(false);
		}
		doPreRender(animation, renderParams);
	}
	
	/**
	 * Perform any required pre-render setup
	 */
	protected abstract void doPreRender(Animation animation, RenderParameters renderParams);

	
	/**
	 * Render the given frame of the given animation into the given file
	 */
	protected abstract void doRender(int frame, File targetFile, Animation animation, RenderParameters renderParams);
	
	protected void postRender(Animation animation, RenderParameters renderParams)
	{
		if(animation.getView() instanceof ITargetView)
		{
			((ITargetView) animation.getView()).setDrawAxisMarker(true);
		}
		doPostRender(animation, renderParams);
	}
	
	/**
	 * Perform any required post-render cleanup
	 */
	protected abstract void doPostRender(Animation animation, RenderParameters renderParams);
	
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
