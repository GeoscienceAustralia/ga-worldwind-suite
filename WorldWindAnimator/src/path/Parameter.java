package path;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import camera.vector.Vector2;

public class Parameter
{
	private final static int BEZIER_SUBDIVISIONS_PER_FRAME = 10;

	private SortedSet<KeyFrame> keys = new TreeSet<KeyFrame>();
	private Map<Integer, KeyFrame> map = new HashMap<Integer, KeyFrame>();

	public Parameter()
	{
	}

	public int getFirstFrame()
	{
		return keys.first().frame;
	}

	public int getLastFrame()
	{
		return keys.last().frame;
	}

	public int[] getFrames()
	{
		int[] frames = new int[keys.size()];
		int i = 0;
		for (KeyFrame key : keys)
			frames[i++] = key.frame;
		return frames;
	}

	public void addKey(int frame)
	{
		addKey(frame, getValue(frame));
	}

	public void addKey(int frame, double value)
	{
		KeyFrame key = new KeyFrame(frame, value);
		if (!keys.contains(key))
		{
			keys.add(key);
			map.put(frame, key);
			setPredecessorAndSuccessor(key);
			updateBezier(key.predecessor);
			updateBezier(key);
		}
	}

	private void setPredecessorAndSuccessor(KeyFrame key)
	{
		if (key != null)
		{
			KeyFrame p = getPredecessor(key);
			KeyFrame s = getSuccessor(key);
			key.predecessor = p;
			key.successor = s;
			if (p != null)
				p.successor = key;
			if (s != null)
				s.predecessor = key;
		}
	}

	private KeyFrame getPredecessor(KeyFrame key)
	{
		SortedSet<KeyFrame> head = keys.headSet(key);
		if (head.isEmpty())
			return null;
		return head.last();
	}

	private KeyFrame getSuccessor(KeyFrame key)
	{
		SortedSet<KeyFrame> tail = keys.tailSet(new KeyFrame(key.frame + 1));
		if (tail.isEmpty())
			return null;
		return tail.first();
	}

	public double getValue(int frame)
	{
		KeyFrame key = getPreviousKey(frame);
		if (key == null)
			throw new IndexOutOfBoundsException();
		double[] values = key.values;
		if (values == null)
			return key.value;
		int index = frame - key.frame;
		return key.values[index];
	}

	public void setValue(int frame, double value)
	{
		KeyFrame key = getKey(frame);
		if (key != null)
		{
			double diff = value - key.value;
			key.value = value;
			key.inValue += diff;
			key.outValue += diff;
			updateBezier(key.predecessor);
			updateBezier(key);
		}
	}

	public void setIn(int frame, Vector2 v)
	{
		setIn(frame, v.x, v.y);
	}

	public void setIn(int frame, double inFrame, double inValue)
	{
		KeyFrame key = getKey(frame);
		if (key != null)
		{
			KeyFrame p = key.predecessor;
			double inPercent = 0;
			if (p != null)
				inPercent = (inFrame - p.frame)
						/ (double) (key.frame - p.frame);
			setInPercent(key, inPercent, inValue);
		}
	}

	public void setInPercent(int frame, double inPercent, double inValue)
	{
		KeyFrame key = getKey(frame);
		if (key != null)
		{
			setInPercent(key, inPercent, inValue);
		}
	}

	private void setInPercent(KeyFrame key, double inPercent, double inValue)
	{
		key.inValue = inValue;
		key.inPercent = clampPercent(inPercent);
		updateBezier(key.predecessor);

		if (key.lockInOut)
		{
			lockOut(key);
		}
	}

	public void setOut(int frame, Vector2 v)
	{
		setOut(frame, v.x, v.y);
	}

	public void setOut(int frame, double outFrame, double outValue)
	{
		KeyFrame key = getKey(frame);
		if (key != null)
		{
			KeyFrame s = key.successor;
			double outPercent = 0;
			if (s != null)
				outPercent = (s.frame - outFrame)
						/ (double) (s.frame - key.frame);
			setOutPercent(key, outPercent, outValue);
		}
	}

	public void setOutPercent(int frame, double outPercent, double outValue)
	{
		KeyFrame key = getKey(frame);
		if (key != null)
		{
			setOutPercent(key, outPercent, outValue);
		}
	}

	private void setOutPercent(KeyFrame key, double outPercent, double outValue)
	{
		key.outValue = outValue;
		key.outPercent = clampPercent(outPercent);
		updateBezier(key);

		if (key.lockInOut)
		{
			lockIn(key);
		}
	}

	public void setLockInOut(int frame, boolean lock)
	{
		KeyFrame key = getKey(frame);
		if (key != null)
		{
			key.lockInOut = lock;
			if (lock)
				lockOut(key);
		}
	}

	private void lockIn(KeyFrame key)
	{
		KeyFrame p = key.predecessor;
		KeyFrame s = key.successor;
		if (p == null || s == null)
			return;

		double deltaOutX = (s.frame - key.frame) * key.outPercent;
		double deltaOutY = key.inValue - key.value;
		double deltaInX = (key.frame - p.frame) * key.outPercent;
		double deltaInY = (deltaOutY / deltaOutX) * deltaInX;

		key.inPercent = key.outPercent;
		key.inValue = key.value - deltaInY;
		updateBezier(p);
	}

	private void lockOut(KeyFrame key)
	{
		KeyFrame p = key.predecessor;
		KeyFrame s = key.successor;
		if (p == null || s == null)
			return;

		double deltaInX = (key.frame - p.frame) * key.inPercent;
		double deltaInY = key.inValue - key.value;
		double deltaOutX = (s.frame - key.frame) * key.inPercent;
		double deltaOutY = (deltaInY / deltaInX) * deltaOutX;

		key.outPercent = key.inPercent;
		key.outValue = key.value - deltaOutY;

		updateBezier(key);
	}

	public void setFrame(int frame, int newFrame)
	{
		//don't overwrite a key that already exists
		if (getKey(newFrame) != null)
			return;

		KeyFrame key = getKey(frame);
		if (key != null)
		{
			KeyFrame p = key.predecessor;
			KeyFrame s = key.successor;
			if ((p == null || newFrame > p.frame)
					&& (s == null || newFrame < s.frame))
			{
				//doesn't change position
				key.frame = newFrame;
				updateBezier(p);
				updateBezier(key);
			}
			else
			{
				keys.remove(key);
				key.frame = newFrame;
				keys.add(key);

				setPredecessorAndSuccessor(p);
				updateBezier(p);

				setPredecessorAndSuccessor(key);
				updateBezier(key.predecessor);
				updateBezier(key);
			}
			map.remove(frame);
			map.put(newFrame, key);
		}
	}

	private void updateBezier(KeyFrame key)
	{
		if (key == null)
			return;
		KeyFrame end = key.successor;
		if (end == null)
			return;
		double sf = (end.frame - key.frame) * clampPercent(1 - key.outPercent)
				+ key.frame;
		double ef = (end.frame - key.frame) * clampPercent(end.inPercent)
				+ key.frame;
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
				if (vLast == null)
				{
					key.values[current] = vNext.y;
				}
				else
				{
					double percent = ((current + key.frame) - vLast.x)
							/ (vNext.x - vLast.x);
					key.values[current] = vLast.y + (vNext.y - vLast.y)
							* percent;
				}
				current++;
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

	private double clampPercent(double value)
	{
		return Math.min(1, Math.max(0, value));
	}

	private KeyFrame getKey(int frame)
	{
		return map.get(frame);
	}

	private KeyFrame getPreviousKey(int frame)
	{
		SortedSet<KeyFrame> head = keys.headSet(new KeyFrame(frame + 1));
		if (head.isEmpty())
			return null;
		return head.last();
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
		private KeyFrame predecessor;
		private KeyFrame successor;

		private KeyFrame(int frame)
		{
			this.frame = frame;
		}

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

		parameter.setLockInOut(100, true);
		parameter.setInPercent(100, 0.1, 150);

		for (int i = parameter.getFirstFrame(); i <= parameter.getLastFrame(); i++)
		{
			System.out.println(parameter.getValue(i));
		}
	}
}
