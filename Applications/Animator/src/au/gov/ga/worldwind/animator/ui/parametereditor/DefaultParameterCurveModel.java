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
import au.gov.ga.worldwind.animator.util.Validate;

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
		
		updateCurve();
	}

	/**
	 * Destroy's this curve. Once called, no further updates will take place for the curve.
	 */
	public void destroy()
	{
		parameter.removeChangeListener(this);

		backBufferLock.lock();
		frontBufferLock.lock();
		curvePointsBackBuffer.clear();
		curvePointsFrontBuffer.clear();
		backBufferLock.unlock();
		frontBufferLock.unlock();
	}
	
	@Override
	public double getValueAtFrame(int frame)
	{
		try
		{
			frontBufferLock.lock();
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
	public void receiveAnimationEvent(AnimationEvent event)
	{
		updateCurve();
	}

	@SuppressWarnings("unchecked")
	private void updateCurve()
	{
		if (currentTask == null || currentTask.isDone())
		{
			currentTask = (Future<CurveUpdateTask>) updater.submit(new CurveUpdateTask());
		}
		else
		{
			if (nextTask != null)
			{
				nextTask.cancel(true);
			}
			nextTask = (Future<CurveUpdateTask>) updater.submit(new CurveUpdateTask());
		}
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
		@Override
		public void run()
		{
			try
			{
				backBufferLock.lock();
				recalculatePoints();
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

		private void recalculatePoints()
		{
			curvePointsBackBuffer.clear();

			List<KeyFrame> keyFrames = parameter.getKeyFramesWithThisParameter();
			if (keyFrames.isEmpty())
			{
				return;
			}
			
			double minValue = Double.POSITIVE_INFINITY;
			double maxValue = Double.NEGATIVE_INFINITY;
			
			int firstFrame = keyFrames.get(0).getFrame();
			int lastFrame = keyFrames.get(keyFrames.size() - 1).getFrame();
			for (int frame = firstFrame; frame <= lastFrame; frame++)
			{
				ParameterCurvePoint curvePoint = new ParameterCurvePoint(frame, parameter.getValueAtFrame(frame).getValue());
				curvePointsBackBuffer.put(frame, curvePoint);
				
				if (curvePoint.value > maxValue)
				{
					maxValue = curvePoint.value;
				}
				
				if (curvePoint.value < minValue)
				{
					minValue = curvePoint.value;
				}
			}
			
			DefaultParameterCurveModel.this.maxValue = maxValue;
			DefaultParameterCurveModel.this.minValue = minValue;
		}
		
		private void swapBuffers()
		{
			TreeMap<Integer, ParameterCurvePoint> tmpBuffer = curvePointsBackBuffer;
			curvePointsBackBuffer = curvePointsFrontBuffer;
			curvePointsFrontBuffer = tmpBuffer;
		}
		
	}
}
