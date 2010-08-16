package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.Restorable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.math.vector.Vector2;
import nasa.worldwind.util.RestorableSupport;

/**
 * A {@link Parameter} represents a single animatable property of some {@link Animatable} object (e.g. Camera position, layer opacity etc.).
 * <p/>
 * TODO: Refactor into new structure
 * 
 */
public class OldParameter implements Serializable, Restorable
{
	private final static int BEZIER_SUBDIVISIONS_PER_FRAME = 10;
	private final static boolean DEFAULT_LOCK_INOUT = true;
	final static double DEFAULT_INOUT_PERCENT = 0.4;

	private SortedMap<Integer, KeyFrame> map = new TreeMap<Integer, KeyFrame>();
	private List<KeyFrame> keys = new ArrayList<KeyFrame>();
	private KeyFrame lastPrevious, lastNext;

	private transient List<ChangeListener> changeListeners;

	public OldParameter()
	{
	}

	public int getFirstFrame()
	{
		if (map.isEmpty())
		{
			return 0;
		}
		return map.firstKey();
	}

	public int getLastFrame()
	{
		if (map.isEmpty())
		{
			return 0;
		}
		return map.lastKey();
	}

	public int size()
	{
		return keys.size();
	}

	public int getFrame(int index)
	{
		return keys.get(index).getFrame();
	}

	public int indexOf(int frame)
	{
		KeyFrame key = map.get(frame);
		if (key == null)
		{
			return -1;
		}
		return keys.indexOf(key);
	}

	public double getMaximumValue()
	{
		double max = Double.NEGATIVE_INFINITY;
		for (KeyFrame key : keys)
		{
			max = Math.max(max, key.getMaxValue());
			max = Math.max(max, key.getValue());
			max = Math.max(max, key.getInValue());
			max = Math.max(max, key.getOutValue());
		}
		return max;
	}

	public double getMinimumValue()
	{
		double min = Double.POSITIVE_INFINITY;
		for (KeyFrame key : keys)
		{
			min = Math.min(min, key.getMinValue());
			min = Math.min(min, key.getValue());
			min = Math.min(min, key.getInValue());
			min = Math.min(min, key.getOutValue());
		}
		return min;
	}

	public void addKey(int frame)
	{
		addKey(frame, getInterpolatedValue(frame));
	}

	public void addKey(int frame, double value)
	{
		if (map.containsKey(frame))
		{
			KeyFrame key = map.remove(frame);
			keys.remove(key);
		}

		KeyFrame key = new KeyFrame(frame, value);
		keys.add(key);
		Collections.sort(keys);
		map.put(frame, key);
		clearLast();

		int index = keys.indexOf(key);
		updateBezier(index - 1);

		setLockInOut(index, DEFAULT_LOCK_INOUT);
		if (!DEFAULT_LOCK_INOUT)
		{
			updateBezier(index);
		}

		notifyChange();
	}

	public void removeKey(int index)
	{
		KeyFrame key = keys.get(index);
		keys.remove(key);
		map.remove(key.getFrame());
		updateBezier(index - 1);

		notifyChange();
	}

	public void setValue(int index, double value)
	{
		KeyFrame key = keys.get(index);
		double diff = value - key.getValue();
		key.setValue(value);
		key.setInValue(key.getInValue() + diff);
		key.setInValue(key.getOutValue() + diff);
		updateBezier(index - 1);
		updateBezier(index);

		notifyChange();
	}

	public double getValue(int index)
	{
		KeyFrame key = keys.get(index);
		return key.getValue();
	}

	public Vector2 getIn(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		if (p == null)
		{
			return null;
		}
		double frame = key.getFrame() - key.getInPercent() * (key.getFrame() - p.getFrame());
		return new Vector2(frame, key.getInValue());
	}

	public Vector2 getInPercent(int index)
	{
		KeyFrame key = keys.get(index);
		return new Vector2(key.getInPercent(), key.getInValue());
	}

	public void setIn(int index, Vector2 v)
	{
		setIn(index, v.x, v.y);
	}

	public void setIn(int index, double inFrame, double inValue)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		double inPercent = 0;
		if (p != null)
		{
			inPercent = (key.getFrame() - inFrame) / (double) (key.getFrame() - p.getFrame());
		}
		setInPercent(index, inPercent, inValue);
	}

	public void setInPercent(int index, double inPercent, double inValue)
	{
		KeyFrame key = keys.get(index);
		key.setInValue(inValue);
		key.setInPercent(clampPercent(inPercent));
		updateBezier(index - 1);

		if (key.isLockInOut())
		{
			lockOut(index);
		}

		notifyChange();
	}

	public Vector2 getOut(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame s = getOrNull(index + 1);
		if (s == null)
			return null;
		double frame = key.getFrame() + key.getOutPercent() * (s.getFrame() - key.getFrame());
		return new Vector2(frame, key.getOutValue());
	}

	public Vector2 getOutPercent(int index)
	{
		KeyFrame key = keys.get(index);
		return new Vector2(key.getOutPercent(), key.getOutValue());
	}

	public void setOut(int index, Vector2 v)
	{
		setOut(index, v.x, v.y);
	}

	public void setOut(int index, double outFrame, double outValue)
	{
		KeyFrame key = keys.get(index);
		KeyFrame s = getOrNull(index + 1);
		double outPercent = 0;
		if (s != null)
			outPercent = (outFrame - key.getFrame()) / (double) (s.getFrame() - key.getFrame());
		setOutPercent(index, outPercent, outValue);
	}

	public void setOutPercent(int index, double outPercent, double outValue)
	{
		KeyFrame key = keys.get(index);
		key.setOutValue(outValue);
		key.setOutPercent(clampPercent(outPercent));
		updateBezier(index);

		if (key.isLockInOut())
		{
			lockIn(index);
		}

		notifyChange();
	}

	public boolean isLockInOut(int index)
	{
		KeyFrame key = keys.get(index);
		return key.isLockInOut();
	}

	public void setLockInOut(int index, boolean lock)
	{
		KeyFrame key = keys.get(index);
		key.setLockInOut(lock);
		if (lock)
		{
			lockOut(index);
		}

		notifyChange();
	}

	private void lockIn(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		KeyFrame s = getOrNull(index + 1);

		if (p == null || s == null)
			return;

		double deltaOutX = (s.getFrame() - key.getFrame()) * key.getOutPercent();
		double deltaOutY = key.getInValue() - key.getValue();
		double deltaInX = (key.getFrame() - p.getFrame()) * key.getOutPercent();
		double deltaInY = (deltaOutY / deltaOutX) * deltaInX;

		key.setInPercent(key.getOutPercent());
		key.setInValue(key.getValue() - deltaInY);
		updateBezier(index - 1);
	}

	private void lockOut(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		KeyFrame s = getOrNull(index + 1);

		if (p == null || s == null)
			return;

		double deltaInX = (key.getFrame() - p.getFrame()) * key.getInPercent();
		double deltaInY = key.getInValue() - key.getValue();
		double deltaOutX = (s.getFrame() - key.getFrame()) * key.getInPercent();
		double deltaOutY = (deltaInY / deltaInX) * deltaOutX;

		key.setOutPercent(key.getInPercent());
		key.setOutValue(key.getValue() - deltaOutY);

		updateBezier(index);
	}

	public void setFrame(int index, int frame)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		KeyFrame s = getOrNull(index + 1);

		frame =
				clamp(frame, p == null ? Integer.MIN_VALUE : p.getFrame() + 1,
						s == null ? Integer.MAX_VALUE : s.getFrame() - 1);
		map.remove(key.getFrame());
		key.setFrame(frame);
		map.put(frame, key);

		updateBezier(index - 1);
		updateBezier(index);

		notifyChange();
	}

	public void setFrames(int[] frames)
	{
		if (frames.length != size())
			throw new IllegalArgumentException();

		Arrays.sort(frames);

		for (int i = 0; i < size(); i++)
		{
			KeyFrame key = keys.get(i);
			map.remove(key.getFrame());
			key.setFrame(frames[i]);
			map.put(frames[i], key);
		}

		for (int i = 0; i < size() - 1; i++)
		{
			updateBezier(i);
		}

		notifyChange();
	}

	public void smooth(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		KeyFrame s = getOrNull(index + 1);
		if (p != null && s != null)
		{
			double y = key.getValue();
			if (Math.signum(key.getValue() - p.getValue()) != Math.signum(key.getValue() - s.getValue()))
			{
				//same direction
				key.setInPercent(DEFAULT_INOUT_PERCENT / 2.0);
				key.setOutPercent(DEFAULT_INOUT_PERCENT / 2.0);
				double m = (s.getValue() - p.getValue()) / (s.getFrame() - p.getFrame());
				double x = (key.getFrame() - p.getFrame()) * key.getInPercent();
				y = key.getValue() - m * x;

			}
			else
			{
				key.setInPercent(DEFAULT_INOUT_PERCENT);
				key.setOutPercent(DEFAULT_INOUT_PERCENT);
			}
			setInPercent(index, key.getInPercent(), y);
			if (!key.isLockInOut())
			{
				lockOut(index);
			}

			notifyChange();
		}
	}

	public void scaleValues(double scale)
	{
		for (KeyFrame key : keys)
		{
			key.setValue(key.getValue() * scale);
			key.setInValue(key.getInValue() * scale);
			key.setOutValue(key.getOutValue() * scale);
		}

		for (int i = 0; i < size() - 1; i++)
		{
			updateBezier(i);
		}

		notifyChange();
	}

	private KeyFrame getOrNull(int index)
	{
		if (index < 0 || index >= size())
		{
			return null;
		}
		return keys.get(index);
	}

	private void updateAllBeziers()
	{
		for (int i = 0; i < size(); i++)
			updateBezier(i);
	}

	private void updateBezier(int index)
	{
		if (index < 0 || index >= keys.size())
		{
			return;
		}

		KeyFrame key = keys.get(index);
		key.setValues(null);
		key.setMaxValue(Double.NEGATIVE_INFINITY);
		key.setMinValue(Double.POSITIVE_INFINITY);

		//is this the last key?
		if (index >= keys.size() - 1)
		{
			return;
		}

		KeyFrame end = keys.get(index + 1);
		double sf = key.getFrame() + key.getOutPercent() * (end.getFrame() - key.getFrame());
		double ef = end.getFrame() - end.getInPercent() * (end.getFrame() - key.getFrame());
		Vector2 out = new Vector2(sf, key.getOutValue());
		Vector2 in = new Vector2(ef, end.getInValue());
		Vector2 begin = new Vector2(key.getFrame(), key.getValue());
		Vector2 endv = new Vector2(end.getFrame(), end.getValue());

		int frames = end.getFrame() - key.getFrame();
		int subdivisions = frames * BEZIER_SUBDIVISIONS_PER_FRAME;
		key.setValues(new double[frames]);
		key.getValues()[0] = key.getValue();
		int current = 1;
		Vector2 vNext, vLast = null;
		for (int i = 0; i < subdivisions; i++)
		{
			vNext = bezierPointAt(i / (double) subdivisions, begin, out, in, endv);

			if (vNext.x > current + key.getFrame())
			{
				double value;
				if (vLast == null)
				{
					value = vNext.y;
				}
				else
				{
					double percent = ((current + key.getFrame()) - vLast.x) / (vNext.x - vLast.x);
					value = vLast.y + (vNext.y - vLast.y) * percent;
				}
				key.getValues()[current++] = value;
				key.setMinValue(Math.min(key.getMinValue(), value));
				key.setMaxValue(Math.max(key.getMaxValue(), value));
			}

			if (current >= frames)
				break;

			vLast = vNext;
		}
	}

	private Vector2 bezierPointAt(double t, Vector2 begin, Vector2 out, Vector2 in, Vector2 end)
	{
		t = clampPercent(t);
		double t2 = t * t;
		Vector2 c = out.subtract(begin).multLocal(3d);
		Vector2 b = in.subtract(out).multLocal(3d).subtractLocal(c);
		Vector2 a = end.subtract(begin).subtractLocal(c).subtractLocal(b);
		a.multLocal(t2 * t);
		b.multLocal(t2);
		c.multLocal(t);
		a.addLocal(b).addLocal(c).addLocal(begin);
		return a;
	}

	private int clamp(int value, int min, int max)
	{
		return Math.min(max, Math.max(min, value));
	}

	private double clampPercent(double value)
	{
		return Math.min(1, Math.max(0, value));
	}

	public double getInterpolatedValue(int frame)
	{
		KeyFrame key = getPreviousKey(frame);
		if (key == null)
		{
			if (keys.isEmpty())
			{
				return 0;
			}
			else
			{
				return keys.get(0).getValue();
			}
		}
		int index = frame - key.getFrame();
		if (key.getValues() == null || index >= key.getValues().length)
		{
			return key.getValue();
		}
		return key.getValues()[index];
	}

	private KeyFrame getPreviousKey(int frame)
	{
		if (lastPrevious == null || frame < lastPrevious.getFrame()
				|| (lastNext != null && frame >= lastNext.getFrame()))
		{
			lastPrevious = getPreviousKeyFromMap(frame);
			if (lastPrevious != null)
			{
				lastNext = getOrNull(keys.indexOf(lastPrevious) + 1);
			}
		}
		return lastPrevious;
	}

	private KeyFrame getPreviousKeyFromMap(int frame)
	{
		if (map.containsKey(frame))
		{
			return map.get(frame);
		}
		SortedMap<Integer, KeyFrame> head = map.headMap(frame);
		if (head.isEmpty())
		{
			return null;
		}
		return map.get(head.lastKey());
	}

	private void clearLast()
	{
		lastPrevious = null;
		lastNext = null;
	}

	public void addChangeListener(ChangeListener changeListener)
	{
		checkChangeListeners();
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener)
	{
		checkChangeListeners();
		changeListeners.remove(changeListener);
	}

	private void notifyChange()
	{
		if (changeListeners != null)
		{
			ChangeEvent e = new ChangeEvent(this);
			for (ChangeListener changeListener : changeListeners)
			{
				changeListener.stateChanged(e);
			}
		}
	}

	private void checkChangeListeners()
	{
		if (changeListeners == null)
			changeListeners = new ArrayList<ChangeListener>();
	}

	@Override
	public String getRestorableState()
	{
		RestorableSupport restorableSupport = RestorableSupport.newRestorableSupport();
		if (restorableSupport == null)
		{
			return null;
		}

		RestorableSupport.StateObject keysState = restorableSupport.addStateObject("keys");
		for (KeyFrame key : keys)
		{
			RestorableSupport.StateObject keyState = restorableSupport.addStateObject(keysState, "key");
			restorableSupport.addStateValueAsRestorable(keyState, "key", key);
		}

		return restorableSupport.getStateAsXml();
	}

	@Override
	public void restoreState(String stateInXml)
	{
		if (stateInXml == null)
		{
			throw new IllegalArgumentException();
		}

		RestorableSupport restorableSupport;
		try
		{
			restorableSupport = RestorableSupport.parse(stateInXml);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Parsing failed", e);
		}

		map.clear();
		keys.clear();

		RestorableSupport.StateObject keysState = restorableSupport.getStateObject("keys");
		if (keysState != null)
		{
			RestorableSupport.StateObject[] keyStateArray = restorableSupport.getAllStateObjects(keysState, "key");
			if (keyStateArray != null)
			{
				for (RestorableSupport.StateObject keyState : keyStateArray)
				{
					if (keyState != null)
					{
						KeyFrame keyFrame = new KeyFrame();
						keyFrame = restorableSupport.getStateValueAsRestorable(keyState, "key", keyFrame);
						if (keyFrame != null)
						{
							keys.add(keyFrame);
							map.put(keyFrame.getFrame(), keyFrame);
						}
					}
				}
			}
		}

		Collections.sort(keys);
		clearLast();
		updateAllBeziers();
		notifyChange();
	}
}
