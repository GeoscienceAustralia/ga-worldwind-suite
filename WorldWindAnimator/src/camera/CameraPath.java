package camera;

import java.io.Serializable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import camera.bezier.LatLonBezier;
import camera.interpolate.Interpolatable;
import camera.interpolate.InterpolatableHeading;
import camera.interpolate.InterpolatableLatLon;
import camera.interpolate.InterpolatablePitch;
import camera.interpolate.InterpolatableZoom;
import camera.motion.Motion;
import camera.motion.MotionParams;
import camera.params.Heading;
import camera.params.LatLon;
import camera.params.Pitch;
import camera.params.Zoom;

public class CameraPath implements Serializable
{
	private SortedMap<Double, MotionAndObject<InterpolatableLatLon>> centers = new TreeMap<Double, MotionAndObject<InterpolatableLatLon>>();
	private SortedMap<Double, MotionAndObject<InterpolatableZoom>> zooms = new TreeMap<Double, MotionAndObject<InterpolatableZoom>>();
	private SortedMap<Double, MotionAndObject<InterpolatableHeading>> headings = new TreeMap<Double, MotionAndObject<InterpolatableHeading>>();
	private SortedMap<Double, MotionAndObject<InterpolatablePitch>> pitchs = new TreeMap<Double, MotionAndObject<InterpolatablePitch>>();

	private boolean centersDirty = true;
	private boolean zoomsDirty = true;
	private boolean headingsDirty = true;
	private boolean pitchsDirty = true;
	private double time = 0;

	public CameraPath(LatLon initialCenter, LatLon initialOut,
			Zoom initialZoom, Heading initialHeading, Pitch initialPitch)
	{
		centers.put(0d, new MotionAndObject<InterpolatableLatLon>(
				new InterpolatableLatLon(new LatLonBezier(initialCenter, null,
						initialOut)), null));
		zooms.put(0d, new MotionAndObject<InterpolatableZoom>(
				new InterpolatableZoom(initialZoom), null));
		headings.put(0d, new MotionAndObject<InterpolatableHeading>(
				new InterpolatableHeading(initialHeading), null));
		pitchs.put(0d, new MotionAndObject<InterpolatablePitch>(
				new InterpolatablePitch(initialPitch), null));
	}

	public void addCenter(LatLon center, LatLon in, LatLon out, double time,
			MotionParams motion)
	{
		LatLonBezier llb = new LatLonBezier(center, in, out);
		centers.put(time, new MotionAndObject<InterpolatableLatLon>(
				new InterpolatableLatLon(llb), new Motion(motion)));
		centersDirty = true;
	}

	public void addZoom(Zoom zoom, double time, MotionParams motion)
	{
		zooms.put(time, new MotionAndObject<InterpolatableZoom>(
				new InterpolatableZoom(zoom), new Motion(motion)));
		zoomsDirty = true;
	}

	public void addHeading(Heading heading, double time, MotionParams motion)
	{
		headings.put(time, new MotionAndObject<InterpolatableHeading>(
				new InterpolatableHeading(heading), new Motion(motion)));
		headingsDirty = true;
	}

	public void addPitch(Pitch pitch, double time, MotionParams motion)
	{
		pitchs.put(time, new MotionAndObject<InterpolatablePitch>(
				new InterpolatablePitch(pitch), new Motion(motion)));
		pitchsDirty = true;
	}

	public LatLon getCenter(double time)
	{
		return new ValueGetter<LatLon, InterpolatableLatLon>().getValue(time,
				centers);
	}

	public Zoom getZoom(double time)
	{
		return new ValueGetter<Zoom, InterpolatableZoom>()
				.getValue(time, zooms);
	}

	public Heading getHeading(double time)
	{
		return new ValueGetter<Heading, InterpolatableHeading>().getValue(time,
				headings);
	}

	public Pitch getPitch(double time)
	{
		return new ValueGetter<Pitch, InterpolatablePitch>().getValue(time,
				pitchs);
	}

	public double getTime()
	{
		refreshIfDirty();
		return time;
	}

	private void refreshIfDirty()
	{
		this.time = 0;
		double time = 0;

		if (centersDirty)
		{
			time = new MapRefresher<InterpolatableLatLon>().refreshMap(centers);
			this.time = Math.max(this.time, time);
			centersDirty = false;
		}
		if (zoomsDirty)
		{
			time = new MapRefresher<InterpolatableZoom>().refreshMap(zooms);
			this.time = Math.max(this.time, time);
			zoomsDirty = false;
		}
		if (headingsDirty)
		{
			time = new MapRefresher<InterpolatableHeading>()
					.refreshMap(headings);
			this.time = Math.max(this.time, time);
			headingsDirty = false;
		}
		if (pitchsDirty)
		{
			time = new MapRefresher<InterpolatablePitch>().refreshMap(pitchs);
			this.time = Math.max(this.time, time);
			pitchsDirty = false;
		}
	}

	private static class ValueGetter<T, E extends Interpolatable<?, T>>
	{
		public T getValue(double time, SortedMap<Double, MotionAndObject<E>> map)
		{
			SortedMap<Double, MotionAndObject<E>> head = map.headMap(time);
			SortedMap<Double, MotionAndObject<E>> tail = map.tailMap(time);

			if (head.isEmpty())
				return map.get(map.firstKey()).object.getEnd();
			if (tail.isEmpty())
				return map.get(map.lastKey()).object.getEnd();

			double firstTime = head.lastKey();
			double lastTime = tail.firstKey();

			if (firstTime > time || lastTime < time)
				throw new IllegalStateException();

			MotionAndObject<E> lastPoint = map.get(lastTime);
			double percent = lastPoint.motion.getPercent(time - firstTime);
			return lastPoint.object.interpolate(percent);
		}
	}

	private static class MapRefresher<E extends Interpolatable<?, ?>>
	{
		public double refreshMap(SortedMap<Double, MotionAndObject<E>> map)
		{
			double cumulativeTime = 0;
			Iterator<Entry<Double, MotionAndObject<E>>> iterator = map
					.entrySet().iterator();
			Entry<Double, MotionAndObject<E>> entry1 = iterator.next(), entry2 = null;
			while (iterator.hasNext())
			{
				entry2 = iterator.next();

				double time = entry2.getKey() - entry1.getKey();
				entry2.getValue().object.setPrevious(entry1.getValue().object);
				double length = entry2.getValue().object.length();

				Motion motion2 = entry2.getValue().motion;
				if (motion2.params.usePreviousForIn)
				{
					Motion motion1 = entry1.getValue().motion;
					double v1 = 0;
					if (motion1 != null)
					{
						v1 = motion1.getOutVelocity();
					}
					motion2.setInVelocity(v1);
				}

				motion2.setTimeAndDistance(time, length);
				cumulativeTime += time;

				entry1 = entry2;
			}
			return cumulativeTime;
		}
	}

	private static class MotionAndObject<E extends Interpolatable<?, ?>>
	{
		public Motion motion;
		public E object;

		public MotionAndObject(E object, Motion motion)
		{
			this.object = object;
			this.motion = motion;
		}
	}
}
