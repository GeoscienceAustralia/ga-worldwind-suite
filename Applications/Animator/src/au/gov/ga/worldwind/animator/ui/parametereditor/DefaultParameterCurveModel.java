package au.gov.ga.worldwind.animator.ui.parametereditor;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * The default implementation of the parameter curve model.
 * <p/>
 * Uses a provided executor service to calculate and cache curve points.
 *
 */
public class DefaultParameterCurveModel implements ParameterCurveModel, AnimationEventListener
{
	/** The parameter being drawn */
	private Parameter parameter;
	
	// Fields used to handle the recalculation of the curve's points on a separate thread
	private ExecutorService updater;
	private Future<CurveUpdateTask> currentTask;
	private Future<CurveUpdateTask> nextTask;
	
	// Buffers used to hold the calculated curve points
	// X-axis: frames
	// Y-axis: parameter value
	private TreeMap<Integer, ParameterCurvePoint> curvePointsBackBuffer = new TreeMap<Integer, ParameterCurvePoint>();
	private TreeMap<Integer, ParameterCurvePoint> curvePointsFrontBuffer = new TreeMap<Integer, ParameterCurvePoint>();
	private Lock backBufferLock = new ReentrantLock();
	private Lock frontBufferLock = new ReentrantLock();
	private double maxValue;
	private double minValue;
	
	private List<ParameterCurveModelListener> listeners = new ArrayList<ParameterCurveModelListener>();
	
	public DefaultParameterCurveModel(Parameter parameter, ExecutorService updater)
	{
		Validate.notNull(parameter, "A parameter is required");
		Validate.notNull(updater, "An executor service is required");
		
		this.updater = updater;
		
		this.parameter = parameter;
		this.parameter.addChangeListener(this);
		
		this.updater = updater;
		
		// Update the curve on this thread the first time around to ensure 
		// the model begins life in a valid state
		new CurveUpdateTask().run();
	}

	@Override
	public String getParameterLabel()
	{
		return parameter.getName();
	}
	
	/**
	 * Destroy's this curve. Once called, no further updates will take place for the curve.
	 */
	public void destroy()
	{
		parameter.removeChangeListener(this);

		if (currentTask != null)
		{
			currentTask.cancel(true);
		}
		if (nextTask != null)
		{
			nextTask.cancel(true);
		}
		
		backBufferLock.lock();
		frontBufferLock.lock();
		curvePointsBackBuffer.clear();
		curvePointsFrontBuffer.clear();
		backBufferLock.unlock();
		frontBufferLock.unlock();
		
		updater.shutdownNow();
	}
	
	@Override
	public double getValueAtFrame(int frame)
	{
		try
		{
			frontBufferLock.lock();
			
			if (curvePointsFrontBuffer.isEmpty())
			{
				return parameter.getDefaultValue();
			}
			
			if (frame <= curvePointsFrontBuffer.firstKey())
			{
				return curvePointsFrontBuffer.ceilingEntry(frame).getValue().value;
			}
			return curvePointsFrontBuffer.floorEntry(frame).getValue().value;
		}
		finally
		{
			frontBufferLock.unlock();
		}
	}
	
	@Override
	public double getMinValue()
	{
		return minValue;
	}
	
	@Override
	public double getMaxValue()
	{
		return maxValue;
	}
	
	@Override
	public int getMinFrame()
	{
		if (curvePointsFrontBuffer.isEmpty())
		{
			return 0;
		}
		return curvePointsFrontBuffer.firstKey();
	}
	
	@Override
	public int getMaxFrame()
	{
		if (curvePointsFrontBuffer.isEmpty())
		{
			return 0;
		}
		return curvePointsFrontBuffer.lastKey();
	}
	
	@Override
	public int getAnimationFrameCount()
	{
		return parameter.getAnimation().getFrameCount();
	}
	
	@Override
	public int getCurrentFrame()
	{
		return parameter.getAnimation().getCurrentFrame();
	}
	
	@Override
	public Parameter getParameter()
	{
		return parameter;
	}
	
	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		if (!isEventForThisParameter(event))
		{
			return;
		}
		
		// Calculate a 'dirty window' from the event to minimise the amount of curve that needs to be recalculated
		ParameterValue changedValue = getParameterValueFromEvent(event);
		Integer startFrame = null;
		Integer endFrame = null;
		
		if (changedValue != null)
		{
			ParameterValue previousKeyValue = parameter.getValueAtKeyFrameBeforeFrame(changedValue.getFrame());
			startFrame = previousKeyValue == null ? null : previousKeyValue.getFrame();
			
			ParameterValue nextKeyValue = parameter.getValueAtKeyFrameAfterFrame(changedValue.getFrame());
			endFrame = nextKeyValue == null ? null : nextKeyValue.getFrame();
		}
		
		submitUpdateTask(new CurveUpdateTask(startFrame, endFrame));
	}

	@SuppressWarnings("unchecked")
	private void submitUpdateTask(CurveUpdateTask updateTask)
	{
		if (currentTask == null || currentTask.isDone())
		{
			currentTask = (Future<CurveUpdateTask>) updater.submit(updateTask);
		}
		else
		{
			if (nextTask != null)
			{
				nextTask.cancel(true);
			}
			nextTask = (Future<CurveUpdateTask>) updater.submit(updateTask);
		}
	}

	private boolean isEventForThisParameter(AnimationEvent event)
	{
		Parameter p = event.getObjectInChainOfType(Parameter.class);
		return p != null && p.equals(parameter);
	}
	
	private ParameterValue getParameterValueFromEvent(AnimationEvent event)
	{
		ParameterValue p = event.getObjectInChainOfType(ParameterValue.class);
		if (p != null && p.getOwner().equals(parameter))
		{
			return p;
		}
		return null;
	}

	@Override
	public void lock()
	{
		frontBufferLock.lock();
	}
	
	@Override
	public void unlock()
	{
		frontBufferLock.unlock();
	}
	
	private void notifyCurveChanged()
	{
		if (listeners.isEmpty())
		{
			return;
		}
		for (int i = listeners.size() - 1; i <= 0; i--)
		{
			listeners.get(i).curveChanged();
		}
	}
	
	@Override
	public void addListener(ParameterCurveModelListener listener)
	{
		if (listener == null || listeners.contains(listener))
		{
			return;
		}
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(ParameterCurveModelListener listener)
	{
		listeners.remove(listener);
	}
	
	@Override
	public List<ParameterCurveKeyNode> getKeyFrameNodes()
	{
		List<ParameterCurveKeyNode> result = new ArrayList<ParameterCurveKeyNode>();
		for (KeyFrame keyFrame : parameter.getKeyFramesWithThisParameter())
		{
			result.add(new ParameterCurveKeyNode(keyFrame.getValueForParameter(parameter)));
		}
		return result;
	}
	
	/**
	 * A runnable task that recalculates the curves points
	 */
	private class CurveUpdateTask implements Runnable
	{
		private Integer startFrame;
		private Integer endFrame;

		/**
		 * Create a new update task that updates the entire curve
		 */
		public CurveUpdateTask()
		{
		}
		
		/**
		 * Create a new update task that updates the curve between the start and end frames
		 */
		public CurveUpdateTask(Integer startFrame, Integer endFrame)
		{
			this.startFrame = startFrame;
			this.endFrame = endFrame;
		}
		
		@Override
		public void run()
		{
			try
			{
				backBufferLock.lock();
				recalculatePoints(startFrame, endFrame);
				frontBufferLock.lock();
				swapBuffers();
			}
			finally
			{
				frontBufferLock.unlock();
				backBufferLock.unlock();
			}
			notifyCurveChanged();
		}

		/**
		 * Recalculates the curve points between the provided (optional) start and end frames.
		 */
		private void recalculatePoints(Integer dirtyWindowStart, Integer dirtyWindowEnd)
		{
			TreeMap<Integer, ParameterCurvePoint> tmpBuffer = new TreeMap<Integer, ParameterCurvePoint>();
			
			List<KeyFrame> keyFrames = parameter.getKeyFramesWithThisParameter();
			if (keyFrames.isEmpty())
			{
				curvePointsBackBuffer.clear();
				return;
			}
			
			double minValue = Double.POSITIVE_INFINITY;
			double maxValue = Double.NEGATIVE_INFINITY;
			
			int firstFrame = keyFrames.get(0).getFrame();
			int lastFrame = keyFrames.get(keyFrames.size() - 1).getFrame();
			
			// Retrieve all values inside the dirty window
			if (dirtyWindowStart == null)
			{
				dirtyWindowStart = firstFrame;
			}
			if (dirtyWindowEnd == null)
			{
				dirtyWindowEnd = lastFrame;
			}
			ParameterValue[] windowValues = parameter.getValuesBetweenFrames(dirtyWindowStart, dirtyWindowEnd, null);
			
			// Repopulate the buffer from the recalculated values
			for (int frame = firstFrame; frame <= lastFrame; frame++)
			{
				ParameterCurvePoint curvePoint = null;
				
				boolean inWindow = inWindow(frame, dirtyWindowStart, dirtyWindowEnd);
				if (inWindow || !curvePointsBackBuffer.containsKey(frame))
				{
					double curveValue = inWindow ? windowValues[frame - dirtyWindowStart].getValue() : parameter.getValueAtFrame(frame).getValue();
					curvePoint = new ParameterCurvePoint(frame, curveValue);
				}
				else
				{
					curvePoint = curvePointsBackBuffer.get(frame);
				}
				
				if (curvePoint.value > maxValue)
				{
					maxValue = curvePoint.value;
				}
				
				if (curvePoint.value < minValue)
				{
					minValue = curvePoint.value;
				}
				
				tmpBuffer.put(frame, curvePoint);
			}
			
			curvePointsBackBuffer = tmpBuffer;
			DefaultParameterCurveModel.this.maxValue = maxValue;
			DefaultParameterCurveModel.this.minValue = minValue;
		}
		
		private boolean inWindow(int frame, Integer dirtyWindowStart, Integer dirtyWindowEnd)
		{
			int start = dirtyWindowStart == null ? Integer.MIN_VALUE : dirtyWindowStart;
			int end = dirtyWindowEnd == null ? Integer.MAX_VALUE : dirtyWindowEnd;
			
			return frame >= start && frame <= end;
		}

		private void swapBuffers()
		{
			TreeMap<Integer, ParameterCurvePoint> tmpBuffer = curvePointsBackBuffer;
			curvePointsBackBuffer = curvePointsFrontBuffer;
			curvePointsFrontBuffer = tmpBuffer;
		}
		
	}
}
