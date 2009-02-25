package path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import camera.vector.Vector2;

public class Parameter
{
	private final static int BEZIER_SUBDIVISIONS_PER_FRAME = 10;
	private final static boolean DEFAULT_LOCK_INOUT = true;

	private SortedMap<Integer, KeyFrame> map = new TreeMap<Integer, KeyFrame>();
	private List<KeyFrame> keys = new ArrayList<KeyFrame>();
	private KeyFrame lastPrevious, lastNext;

	public Parameter()
	{
	}

	public int getFirstFrame()
	{
		return map.firstKey();
	}

	public int getLastFrame()
	{
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
		addKey(frame, getValue(frame));
	}

	public void addKey(int frame, double value)
	{
		if (map.containsKey(frame))
			return;

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
	}

	public void setLockInOut(int index, boolean lock)
	{
		KeyFrame key = keys.get(index);
		key.lockInOut = lock;
		if (lock)
			lockOut(index);
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
	}

	private KeyFrame getOrNull(int index)
	{
		if (index < 0 || index >= size())
			return null;
		return keys.get(index);
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

	public double getValue(int frame)
	{
		KeyFrame key = getPreviousKey(frame);
		if (key == null)
			throw new IndexOutOfBoundsException();
		if (key.values == null)
			return key.value;
		int index = frame - key.frame;
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

	private static class KeyFrame implements Comparable<KeyFrame>
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
			this.inPercent = 0.5;
			this.outPercent = 0.5;
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
	}

	public static void main(String[] args)
	{
		Parameter parameter = new Parameter();
		parameter.addKey(0, 100);
		parameter.addKey(100, 200);
		parameter.addKey(200, 50);

		parameter.setInPercent(1, 0.1, 150);

		for (int i = parameter.getFirstFrame(); i <= parameter.getLastFrame(); i++)
		{
			System.out.println(parameter.getValue(i));
		}
	}
}
