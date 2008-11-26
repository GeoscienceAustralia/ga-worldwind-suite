package camera;

import java.io.Serializable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import camera.bezier.LatLonBezier;
import camera.motion.Motion;
import camera.motion.MotionParams;
import camera.params.Heading;
import camera.params.LatLon;
import camera.params.Pitch;
import camera.params.Zoom;

public class CameraPath implements Serializable
{
	private SortedMap<Double, MotionAndObject<LatLonBezier>> centers = new TreeMap<Double, MotionAndObject<LatLonBezier>>();
	private SortedMap<Double, MotionAndObject<Zoom>> zooms = new TreeMap<Double, MotionAndObject<Zoom>>();
	private SortedMap<Double, MotionAndObject<Heading>> headings = new TreeMap<Double, MotionAndObject<Heading>>();
	private SortedMap<Double, MotionAndObject<Pitch>> pitchs = new TreeMap<Double, MotionAndObject<Pitch>>();

	private boolean centersDirty = true;
	private boolean zoomsDirty = true;
	private boolean headingsDirty = true;
	private boolean pitchsDirty = true;
	private double time = 0;

	public CameraPath(LatLon initialCenter, LatLon initialOut,
			Zoom initialZoom, Heading initialHeading, Pitch initialPitch)
	{
		centers.put(0d, new MotionAndObject<LatLonBezier>(new LatLonBezier(
				initialCenter, null, initialOut), null));
		zooms.put(0d, new MotionAndObject<Zoom>(initialZoom, null));
		headings.put(0d, new MotionAndObject<Heading>(initialHeading, null));
		pitchs.put(0d, new MotionAndObject<Pitch>(initialPitch, null));
	}

	public void addCenter(LatLon center, LatLon in, LatLon out, double time,
			MotionParams motion)
	{
		centers.put(time, new MotionAndObject<LatLonBezier>(new LatLonBezier(
				center, in, out), new Motion(motion)));
		centersDirty = true;
	}

	public void addZoom(Zoom zoom, double time, MotionParams motion)
	{
		zooms.put(time, new MotionAndObject<Zoom>(zoom, new Motion(motion)));
		zoomsDirty = true;
	}

	public void addHeading(Heading heading, double time, MotionParams motion)
	{
		headings.put(time, new MotionAndObject<Heading>(heading, new Motion(
				motion)));
		headingsDirty = true;
	}

	public void addPitch(Pitch pitch, double time, MotionParams motion)
	{
		pitchs.put(time, new MotionAndObject<Pitch>(pitch, new Motion(motion)));
		pitchsDirty = true;
	}

	public LatLon getCenter(double time)
	{
		refreshIfDirty();

		SortedMap<Double, MotionAndObject<LatLonBezier>> head = centers
				.headMap(time);
		SortedMap<Double, MotionAndObject<LatLonBezier>> tail = centers
				.tailMap(time);

		if (head.isEmpty())
			return centers.get(centers.firstKey()).object.latlon;
		if (tail.isEmpty())
			return centers.get(centers.lastKey()).object.latlon;

		double firstTime = head.lastKey();
		double lastTime = tail.firstKey();

		if (firstTime > time || lastTime < time)
			throw new IllegalStateException();

		//MotionAndObject<LatLonBezier> firstPoint = centers.get(firstTime);
		MotionAndObject<LatLonBezier> lastPoint = centers.get(lastTime);

		double percent = lastPoint.motion.getPercent(time - firstTime);
		return lastPoint.object.getLatLon(percent);
	}

	public Zoom getZoom(double time)
	{
		refreshIfDirty();

		SortedMap<Double, MotionAndObject<Zoom>> head = zooms.headMap(time);
		SortedMap<Double, MotionAndObject<Zoom>> tail = zooms.tailMap(time);

		if (head.isEmpty())
			return zooms.get(zooms.firstKey()).object;
		if (tail.isEmpty())
			return zooms.get(zooms.lastKey()).object;

		double firstTime = head.lastKey();
		double lastTime = tail.firstKey();

		if (firstTime > time || lastTime < time)
			throw new IllegalStateException();

		MotionAndObject<Zoom> firstPoint = zooms.get(firstTime);
		MotionAndObject<Zoom> lastPoint = zooms.get(lastTime);

		double percent = lastPoint.motion.getPercent(time - firstTime);
		return Zoom.interpolate(firstPoint.object, lastPoint.object, percent);
	}

	public Heading getHeading(double time)
	{
		refreshIfDirty();

		SortedMap<Double, MotionAndObject<Heading>> head = headings
				.headMap(time);
		SortedMap<Double, MotionAndObject<Heading>> tail = headings
				.tailMap(time);

		if (head.isEmpty())
			return headings.get(headings.firstKey()).object;
		if (tail.isEmpty())
			return headings.get(headings.lastKey()).object;

		double firstTime = head.lastKey();
		double lastTime = tail.firstKey();

		if (firstTime > time || lastTime < time)
			throw new IllegalStateException();

		MotionAndObject<Heading> firstPoint = headings.get(firstTime);
		MotionAndObject<Heading> lastPoint = headings.get(lastTime);

		double percent = lastPoint.motion.getPercent(time - firstTime);
		return Heading
				.interpolate(firstPoint.object, lastPoint.object, percent);
	}

	public Pitch getPitch(double time)
	{
		refreshIfDirty();

		SortedMap<Double, MotionAndObject<Pitch>> head = pitchs.headMap(time);
		SortedMap<Double, MotionAndObject<Pitch>> tail = pitchs.tailMap(time);

		if (head.isEmpty())
			return pitchs.get(pitchs.firstKey()).object;
		if (tail.isEmpty())
			return pitchs.get(pitchs.lastKey()).object;

		double firstTime = head.lastKey();
		double lastTime = tail.firstKey();

		if (firstTime > time || lastTime < time)
			throw new IllegalStateException();

		MotionAndObject<Pitch> firstPoint = pitchs.get(firstTime);
		MotionAndObject<Pitch> lastPoint = pitchs.get(lastTime);

		double percent = lastPoint.motion.getPercent(time - firstTime);
		return Pitch.interpolate(firstPoint.object, lastPoint.object, percent);
	}

	public double getTime()
	{
		refreshIfDirty();
		return time;
	}

	private void refreshIfDirty()
	{
		this.time = 0;
		double cumulativeTime = 0;

		if (centersDirty)
		{
			cumulativeTime = 0;
			Iterator<Entry<Double, MotionAndObject<LatLonBezier>>> iterator = centers
					.entrySet().iterator();
			Entry<Double, MotionAndObject<LatLonBezier>> entry1 = iterator
					.next(), entry2 = null;
			while (iterator.hasNext())
			{
				entry2 = iterator.next();

				double time = entry2.getKey() - entry1.getKey();
				entry2.getValue().object
						.setPreviousPoint(entry1.getValue().object);
				double distance = entry2.getValue().object.getDistance();

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

				motion2.setTimeAndDistance(time, distance);
				cumulativeTime += time;

				entry1 = entry2;
			}
			centersDirty = false;
			this.time = Math.max(this.time, cumulativeTime);
		}
		if (zoomsDirty)
		{
			cumulativeTime = 0;
			Iterator<Entry<Double, MotionAndObject<Zoom>>> iterator = zooms
					.entrySet().iterator();
			Entry<Double, MotionAndObject<Zoom>> entry1 = iterator.next(), entry2 = null;
			while (iterator.hasNext())
			{
				entry2 = iterator.next();

				double time = entry2.getKey() - entry1.getKey();
				double distance = Math.abs(Zoom.difference(
						entry2.getValue().object, entry1.getValue().object));
				cumulativeTime += time;

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
				motion2.setTimeAndDistance(time, distance);

				entry1 = entry2;
			}
			zoomsDirty = false;
			this.time = Math.max(this.time, cumulativeTime);
		}
		if (headingsDirty)
		{
			cumulativeTime = 0;
			Iterator<Entry<Double, MotionAndObject<Heading>>> iterator = headings
					.entrySet().iterator();
			Entry<Double, MotionAndObject<Heading>> entry1 = iterator.next(), entry2 = null;
			while (iterator.hasNext())
			{
				entry2 = iterator.next();

				double time = entry2.getKey() - entry1.getKey();
				double distance = Math.abs(Heading.difference(
						entry2.getValue().object, entry1.getValue().object));
				cumulativeTime += time;

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
				motion2.setTimeAndDistance(time, distance);

				entry1 = entry2;
			}
			headingsDirty = false;
			this.time = Math.max(this.time, cumulativeTime);
		}
		if (pitchsDirty)
		{
			cumulativeTime = 0;
			Iterator<Entry<Double, MotionAndObject<Pitch>>> iterator = pitchs
					.entrySet().iterator();
			Entry<Double, MotionAndObject<Pitch>> entry1 = iterator.next(), entry2 = null;
			while (iterator.hasNext())
			{
				entry2 = iterator.next();

				double time = entry2.getKey() - entry1.getKey();
				double distance = Math.abs(Pitch.difference(
						entry2.getValue().object, entry1.getValue().object));
				cumulativeTime += time;

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
				motion2.setTimeAndDistance(time, distance);

				entry1 = entry2;
			}
			pitchsDirty = false;
			this.time = Math.max(this.time, cumulativeTime);
		}
	}

	private static class MotionAndObject<E>
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
