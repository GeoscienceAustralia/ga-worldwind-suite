package animation;

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

import math.vector.Vector2;
import nasa.worldwind.util.RestorableSupport;

public class Parameter implements Serializable, Restorable
{
	private final static int BEZIER_SUBDIVISIONS_PER_FRAME = 10;
	private final static boolean DEFAULT_LOCK_INOUT = true;
	private final static double DEFAULT_INOUT_PERCENT = 0.4;

	private SortedMap<Integer, KeyFrame> map = new TreeMap<Integer, KeyFrame>();
	private List<KeyFrame> keys = new ArrayList<KeyFrame>();
	private KeyFrame lastPrevious, lastNext;

	private transient List<ChangeListener> changeListeners;

	public Parameter()
	{
	}

	public int getFirstFrame()
	{
		if (map.isEmpty())
			return 0;
		return map.firstKey();
	}

	public int getLastFrame()
	{
		if (map.isEmpty())
			return 0;
		return map.lastKey();
	}

	public int size()
	{
		return keys.size();
	}

	public int getFrame(int index)
	{
		return keys.get(index).frame;
	}

	public int indexOf(int frame)
	{
		KeyFrame key = map.get(frame);
		if (key == null)
			return -1;
		return keys.indexOf(key);
	}

	public double getMaximumValue()
	{
		double max = Double.NEGATIVE_INFINITY;
		for (KeyFrame key : keys)
		{
			max = Math.max(max, key.maxValue);
			max = Math.max(max, key.value);
			max = Math.max(max, key.inValue);
			max = Math.max(max, key.outValue);
		}
		return max;
	}

	public double getMinimumValue()
	{
		double min = Double.POSITIVE_INFINITY;
		for (KeyFrame key : keys)
		{
			min = Math.min(min, key.minValue);
			min = Math.min(min, key.value);
			min = Math.min(min, key.inValue);
			min = Math.min(min, key.outValue);
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
			updateBezier(index);

		notifyChange();
	}

	public void removeKey(int index)
	{
		KeyFrame key = keys.get(index);
		keys.remove(key);
		map.remove(key.frame);
		updateBezier(index - 1);

		notifyChange();
	}

	public void setValue(int index, double value)
	{
		KeyFrame key = keys.get(index);
		double diff = value - key.value;
		key.value = value;
		key.inValue += diff;
		key.outValue += diff;
		updateBezier(index - 1);
		updateBezier(index);

		notifyChange();
	}

	public double getValue(int index)
	{
		KeyFrame key = keys.get(index);
		return key.value;
	}

	public Vector2 getIn(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		if (p == null)
			return null;
		double frame = key.frame - key.inPercent * (key.frame - p.frame);
		return new Vector2(frame, key.inValue);
	}

	public Vector2 getInPercent(int index)
	{
		KeyFrame key = keys.get(index);
		return new Vector2(key.inPercent, key.inValue);
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
			inPercent = (key.frame - inFrame) / (double) (key.frame - p.frame);
		setInPercent(index, inPercent, inValue);
	}

	public void setInPercent(int index, double inPercent, double inValue)
	{
		KeyFrame key = keys.get(index);
		key.inValue = inValue;
		key.inPercent = clampPercent(inPercent);
		updateBezier(index - 1);

		if (key.lockInOut)
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
		double frame = key.frame + key.outPercent * (s.frame - key.frame);
		return new Vector2(frame, key.outValue);
	}

	public Vector2 getOutPercent(int index)
	{
		KeyFrame key = keys.get(index);
		return new Vector2(key.outPercent, key.outValue);
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
			outPercent = (outFrame - key.frame)
					/ (double) (s.frame - key.frame);
		setOutPercent(index, outPercent, outValue);
	}

	public void setOutPercent(int index, double outPercent, double outValue)
	{
		KeyFrame key = keys.get(index);
		key.outValue = outValue;
		key.outPercent = clampPercent(outPercent);
		updateBezier(index);

		if (key.lockInOut)
		{
			lockIn(index);
		}

		notifyChange();
	}

	public boolean isLockInOut(int index)
	{
		KeyFrame key = keys.get(index);
		return key.lockInOut;
	}

	public void setLockInOut(int index, boolean lock)
	{
		KeyFrame key = keys.get(index);
		key.lockInOut = lock;
		if (lock)
			lockOut(index);

		notifyChange();
	}

	private void lockIn(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		KeyFrame s = getOrNull(index + 1);

		if (p == null || s == null)
			return;

		double deltaOutX = (s.frame - key.frame) * key.outPercent;
		double deltaOutY = key.inValue - key.value;
		double deltaInX = (key.frame - p.frame) * key.outPercent;
		double deltaInY = (deltaOutY / deltaOutX) * deltaInX;

		key.inPercent = key.outPercent;
		key.inValue = key.value - deltaInY;
		updateBezier(index - 1);
	}

	private void lockOut(int index)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		KeyFrame s = getOrNull(index + 1);

		if (p == null || s == null)
			return;

		double deltaInX = (key.frame - p.frame) * key.inPercent;
		double deltaInY = key.inValue - key.value;
		double deltaOutX = (s.frame - key.frame) * key.inPercent;
		double deltaOutY = (deltaInY / deltaInX) * deltaOutX;

		key.outPercent = key.inPercent;
		key.outValue = key.value - deltaOutY;

		updateBezier(index);
	}

	public void setFrame(int index, int frame)
	{
		KeyFrame key = keys.get(index);
		KeyFrame p = getOrNull(index - 1);
		KeyFrame s = getOrNull(index + 1);

		frame = clamp(frame, p == null ? Integer.MIN_VALUE : p.frame + 1,
				s == null ? Integer.MAX_VALUE : s.frame - 1);
		map.remove(key.frame);
		key.frame = frame;
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
			map.remove(key.frame);
			key.frame = frames[i];
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
			double y = key.value;
			if (Math.signum(key.value - p.value) != Math.signum(key.value
					- s.value))
			{
				//same direction
				key.inPercent = DEFAULT_INOUT_PERCENT / 2.0;
				key.outPercent = DEFAULT_INOUT_PERCENT / 2.0;
				double m = (s.value - p.value) / (s.frame - p.frame);
				double x = (key.frame - p.frame) * key.inPercent;
				y = key.value - m * x;

			}
			else
			{
				key.inPercent = DEFAULT_INOUT_PERCENT;
				key.outPercent = DEFAULT_INOUT_PERCENT;
			}
			setInPercent(index, key.inPercent, y);
			if (!key.lockInOut)
				lockOut(index);

			notifyChange();
		}
	}

	public void scaleValues(double scale)
	{
		for (KeyFrame key : keys)
		{
			key.value *= scale;
			key.inValue *= scale;
			key.outValue *= scale;
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
			return null;
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
			return;

		KeyFrame key = keys.get(index);
		key.values = null;
		key.maxValue = Double.NEGATIVE_INFINITY;
		key.minValue = Double.POSITIVE_INFINITY;

		//is this the last key?
		if (index >= keys.size() - 1)
			return;

		KeyFrame end = keys.get(index + 1);
		double sf = key.frame + key.outPercent * (end.frame - key.frame);
		double ef = end.frame - end.inPercent * (end.frame - key.frame);
		Vector2 out = new Vector2(sf, key.outValue);
		Vector2 in = new Vector2(ef, end.inValue);
		Vector2 begin = new Vector2(key.frame, key.value);
		Vector2 endv = new Vector2(end.frame, end.value);

		int frames = end.frame - key.frame;
		int subdivisions = frames * BEZIER_SUBDIVISIONS_PER_FRAME;
		key.values = new double[frames];
		key.values[0] = key.value;
		int current = 1;
		Vector2 vNext, vLast = null;
		for (int i = 0; i < subdivisions; i++)
		{
			vNext = bezierPointAt(i / (double) subdivisions, begin, out, in,
					endv);

			if (vNext.x > current + key.frame)
			{
				double value;
				if (vLast == null)
				{
					value = vNext.y;
				}
				else
				{
					double percent = ((current + key.frame) - vLast.x)
							/ (vNext.x - vLast.x);
					value = vLast.y + (vNext.y - vLast.y) * percent;
				}
				key.values[current++] = value;
				key.minValue = Math.min(key.minValue, value);
				key.maxValue = Math.max(key.maxValue, value);
			}

			if (current >= frames)
				break;

			vLast = vNext;
		}
	}

	private Vector2 bezierPointAt(double t, Vector2 begin, Vector2 out,
			Vector2 in, Vector2 end)
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
				return 0;
			else
				return keys.get(0).value;
		}
		int index = frame - key.frame;
		if (key.values == null || index >= key.values.length)
			return key.value;
		return key.values[index];
	}

	private KeyFrame getPreviousKey(int frame)
	{
		if (lastPrevious == null || frame < lastPrevious.frame
				|| (lastNext != null && frame >= lastNext.frame))
		{
			lastPrevious = getPreviousKeyFromMap(frame);
			if (lastPrevious != null)
				lastNext = getOrNull(keys.indexOf(lastPrevious) + 1);
		}
		return lastPrevious;
	}

	private KeyFrame getPreviousKeyFromMap(int frame)
	{
		if (map.containsKey(frame))
			return map.get(frame);
		SortedMap<Integer, KeyFrame> head = map.headMap(frame);
		if (head.isEmpty())
			return null;
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

	private static class KeyFrame implements Comparable<KeyFrame>,
			Serializable, Restorable
	{
		private int frame;
		private double value;
		private double inValue;
		private double inPercent;
		private double outValue;
		private double outPercent;
		private boolean lockInOut;

		private double[] values;
		private double maxValue = Double.NEGATIVE_INFINITY;
		private double minValue = Double.POSITIVE_INFINITY;

		public KeyFrame(int frame, double value)
		{
			this.frame = frame;
			this.value = value;
			this.inValue = value;
			this.outValue = value;
			this.inPercent = DEFAULT_INOUT_PERCENT;
			this.outPercent = DEFAULT_INOUT_PERCENT;
		}

		private KeyFrame()
		{
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof KeyFrame))
				return false;
			return ((KeyFrame) obj).frame == this.frame;
		}

		public int compareTo(KeyFrame o)
		{
			return this.frame - o.frame;
		}

		public String getRestorableState()
		{
			RestorableSupport restorableSupport = RestorableSupport
					.newRestorableSupport();
			if (restorableSupport == null)
				return null;

			restorableSupport.addStateValueAsInteger("frame", frame);
			restorableSupport.addStateValueAsDouble("value", value);
			restorableSupport.addStateValueAsDouble("inValue", inValue);
			restorableSupport.addStateValueAsDouble("inPercent", inPercent);
			restorableSupport.addStateValueAsDouble("outValue", outValue);
			restorableSupport.addStateValueAsDouble("outPercent", outPercent);
			restorableSupport.addStateValueAsBoolean("lockInOut", lockInOut);

			return restorableSupport.getStateAsXml();
		}

		public void restoreState(String stateInXml)
		{
			if (stateInXml == null)
				throw new IllegalArgumentException();

			RestorableSupport restorableSupport;
			try
			{
				restorableSupport = RestorableSupport.parse(stateInXml);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Parsing failed", e);
			}

			frame = restorableSupport.getStateValueAsInteger("frame");
			value = restorableSupport.getStateValueAsDouble("value");
			inValue = restorableSupport.getStateValueAsDouble("inValue");
			inPercent = restorableSupport.getStateValueAsDouble("inPercent");
			outValue = restorableSupport.getStateValueAsDouble("outValue");
			outPercent = restorableSupport.getStateValueAsDouble("outPercent");
			lockInOut = restorableSupport.getStateValueAsBoolean("lockInOut");
		}

		public static KeyFrame fromStateXml(String stateInXml)
		{
			try
			{
				KeyFrame keyFrame = new KeyFrame();
				keyFrame.restoreState(stateInXml);
				return keyFrame;
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}

	public String getRestorableState()
	{
		RestorableSupport restorableSupport = RestorableSupport
				.newRestorableSupport();
		if (restorableSupport == null)
			return null;

		RestorableSupport.StateObject keysState = restorableSupport
				.addStateObject("keys");
		for (KeyFrame key : keys)
		{
			RestorableSupport.StateObject keyState = restorableSupport
					.addStateObject(keysState, "key");
			restorableSupport.addStateValueAsRestorable(keyState, "key", key);
		}

		return restorableSupport.getStateAsXml();
	}

	public void restoreState(String stateInXml)
	{
		if (stateInXml == null)
			throw new IllegalArgumentException();

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

		RestorableSupport.StateObject keysState = restorableSupport
				.getStateObject("keys");
		if (keysState != null)
		{
			RestorableSupport.StateObject[] keyStateArray = restorableSupport
					.getAllStateObjects(keysState, "key");
			if (keyStateArray != null)
			{
				for (RestorableSupport.StateObject keyState : keyStateArray)
				{
					if (keyState != null)
					{
						KeyFrame keyFrame = new KeyFrame();
						keyFrame = restorableSupport.getStateValueAsRestorable(
								keyState, "key", keyFrame);
						if (keyFrame != null)
						{
							keys.add(keyFrame);
							map.put(keyFrame.frame, keyFrame);
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
