package animation;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import math.vector.Vector2;
import math.vector.Vector3;
import nasa.worldwind.util.RestorableSupport;
import util.FileUtil;

public class Animation implements Serializable, ChangeListener,
		Restorable
{
	private Parameter eyeLat = new Parameter();
	private Parameter eyeLon = new Parameter();
	private Parameter eyeZoom = new Parameter();
	private Parameter centerLat = new Parameter();
	private Parameter centerLon = new Parameter();
	private Parameter centerZoom = new Parameter();
	private int frameCount = 100;
	private boolean ignoreChange = false;
	private int width = 1024;
	private int height = 576;
	private boolean scaledZoom = true;

	private transient List<ChangeListener> changeListeners;

	public Animation()
	{
		addChangeListener();

		//TODO put somewhere else!
		/*JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setLayout(new GridLayout(0, 1));
		ParameterEditor editor;

		editor = new ParameterEditor(eyeLat);
		editor.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.add(editor);
		editor = new ParameterEditor(eyeLon);
		editor.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.add(editor);
		editor = new ParameterEditor(eyeZoom);
		editor.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.add(editor);
		editor = new ParameterEditor(centerLat);
		editor.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.add(editor);
		editor = new ParameterEditor(centerLon);
		editor.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.add(editor);
		editor = new ParameterEditor(centerZoom);
		editor.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.add(editor);

		frame.setSize(640, 480);
		frame.setVisible(true);*/
	}

	private void addChangeListener()
	{
		eyeLat.addChangeListener(this);
		eyeLon.addChangeListener(this);
		eyeZoom.addChangeListener(this);
		centerLat.addChangeListener(this);
		centerLon.addChangeListener(this);
		centerZoom.addChangeListener(this);
	}

	public synchronized void applyFrame(OrbitView view, int frame)
	{
		double zoom = eyeZoom.getInterpolatedValue(frame);
		Position eye = Position.fromDegrees(eyeLat.getInterpolatedValue(frame),
				eyeLon.getInterpolatedValue(frame),
				scaledZoom ? fromScaledZoom(zoom) : zoom);
		zoom = centerZoom.getInterpolatedValue(frame);
		Position center = Position.fromDegrees(centerLat
				.getInterpolatedValue(frame), centerLon
				.getInterpolatedValue(frame), scaledZoom ? fromScaledZoom(zoom)
				: zoom);
		view.stopMovement();
		view.setOrientation(eye, center);
	}

	public void addFrame(int frame, OrbitView view)
	{
		Position eye = view.getEyePosition();
		Position center = view.getCenterPosition();
		addFrame(frame, eye, center);
	}

	public synchronized void addFrame(int frame, Position eye, Position center)
	{
		ignoreChange = true;

		eyeLat.addKey(frame, eye.getLatitude().degrees);
		eyeLon.addKey(frame, eye.getLongitude().degrees);
		eyeZoom.addKey(frame, scaledZoom ? toScaledZoom(eye.getElevation())
				: eye.getElevation());
		centerLat.addKey(frame, center.getLatitude().degrees);
		centerLon.addKey(frame, center.getLongitude().degrees);
		centerZoom.addKey(frame, scaledZoom ? toScaledZoom(center
				.getElevation()) : center.getElevation());

		int index = eyeLat.indexOf(frame);
		smoothAll(index);

		frameCount = Math.max(frame, frameCount);

		ignoreChange = false;
		notifyChange();
	}

	private void smoothAll(int index)
	{
		if (index >= 0 && index < size())
		{
			smooth(index);
			if (index > 0)
				smooth(index - 1);
			if (index < size() - 1)
				smooth(index + 1);
		}
	}

	private void smooth(int index)
	{
		eyeLat.smooth(index);
		eyeLon.smooth(index);
		eyeZoom.smooth(index);
		centerLat.smooth(index);
		centerLon.smooth(index);
		centerZoom.smooth(index);
	}

	public synchronized void removeFrame(int index)
	{
		ignoreChange = true;

		eyeLat.removeKey(index);
		eyeLon.removeKey(index);
		eyeZoom.removeKey(index);
		centerLat.removeKey(index);
		centerLon.removeKey(index);
		centerZoom.removeKey(index);
		smoothAll(index);

		ignoreChange = false;
		notifyChange();
	}

	public synchronized int getFrame(int index)
	{
		return eyeLat.getFrame(index);
	}

	public synchronized void setFrame(int index, int frame)
	{
		ignoreChange = true;

		eyeLat.setFrame(index, frame);
		eyeLon.setFrame(index, frame);
		eyeZoom.setFrame(index, frame);
		centerLat.setFrame(index, frame);
		centerLon.setFrame(index, frame);
		centerZoom.setFrame(index, frame);
		smoothAll(index);

		ignoreChange = false;
		notifyChange();
	}

	public synchronized int indexOf(int frame)
	{
		return eyeLat.indexOf(frame);
	}

	public synchronized int size()
	{
		return eyeLat.size();
	}

	public synchronized int getFirstFrame()
	{
		return eyeLat.getFirstFrame();
	}

	public synchronized int getLastFrame()
	{
		return eyeLat.getLastFrame();
	}

	public synchronized void scale(double scale)
	{
		ignoreChange = true;

		int newFrameCount = (int) Math.ceil(scale * getLastFrame() - scale
				* getFirstFrame());
		if (getFrameCount() < newFrameCount)
		{
			setFrameCount(newFrameCount);
		}

		int size = size();
		int[] frames = new int[size];
		for (int i = 0; i < size; i++)
		{
			frames[i] = (int) Math.round(eyeLat.getFrame(i) * scale);
		}
		for (int i = 0; i < frames.length - 1; i++)
		{
			if (frames[i] >= frames[i + 1])
				frames[i + 1] = frames[i] + 1;
		}

		eyeLat.setFrames(frames);
		eyeLon.setFrames(frames);
		eyeZoom.setFrames(frames);
		centerLat.setFrames(frames);
		centerLon.setFrames(frames);
		centerZoom.setFrames(frames);

		for (int i = 0; i < size; i++)
		{
			smooth(i);
		}

		ignoreChange = false;
		notifyChange();
	}

	public synchronized void scaleHeight(double scale)
	{
		ignoreChange = true;
		eyeZoom.scaleValues(scale);
		centerZoom.scaleValues(scale);
		ignoreChange = false;
		notifyChange();
	}

	public synchronized void smoothEyeSpeed()
	{
		ignoreChange = true;

		int size = size();
		if (size > 1)
		{
			double[] cumulativeDistance = new double[size - 1];
			for (int i = 0; i < size - 1; i++)
			{
				int firstFrame = getFrame(i);
				int lastFrame = getFrame(i + 1);
				cumulativeDistance[i] = i == 0 ? 0 : cumulativeDistance[i - 1];
				Vector3 vStart = null;

				for (int frame = firstFrame; frame <= lastFrame; frame++)
				{
					double x = eyeLat.getInterpolatedValue(frame);
					double y = eyeLon.getInterpolatedValue(frame);
					double z = eyeZoom.getInterpolatedValue(frame);
					Vector3 vEnd = new Vector3(x, y, z);
					if (vStart != null)
					{
						cumulativeDistance[i] += vStart.subtract(vEnd)
								.distance();
					}
					vStart = vEnd;
				}
			}

			int[] frames = new int[size];
			int first = getFirstFrame();
			int last = getLastFrame();
			frames[0] = first;
			frames[size - 1] = last;

			for (int i = 1; i < size - 1; i++)
			{
				frames[i] = (int) Math
						.round((getLastFrame() - getFirstFrame() + 1)
								* cumulativeDistance[i - 1]
								/ cumulativeDistance[size - 2]);
			}

			//fix any frames that are equal
			for (int i = 0; i < frames.length - 1; i++)
			{
				if (frames[i] >= frames[i + 1])
					frames[i + 1] = frames[i] + 1;
			}
			if (frames[size - 1] != last)
			{
				frames[size - 1] = last;
				for (int i = frames.length - 1; i > 0; i--)
				{
					if (frames[i] <= frames[i - 1])
						frames[i - 1] = frames[i] - 1;
				}
			}
			if (frames[0] != first)
				throw new IllegalStateException();

			eyeLat.setFrames(frames);
			eyeLon.setFrames(frames);
			eyeZoom.setFrames(frames);
			centerLat.setFrames(frames);
			centerLon.setFrames(frames);
			centerZoom.setFrames(frames);

			for (int i = 0; i < size; i++)
			{
				smooth(i);
			}
		}

		ignoreChange = false;
		notifyChange();
	}

	public synchronized void setScaledZoom(boolean scaledZoom)
	{
		if (this.scaledZoom != scaledZoom)
		{
			int size = size();
			for (int i = 0; i < size; i++)
			{
				Vector2 in, out;
				double z;

				z = eyeZoom.getValue(i);
				z = scaledZoom ? toScaledZoom(z) : fromScaledZoom(z);
				in = eyeZoom.getInPercent(i);
				in.y = scaledZoom ? toScaledZoom(in.y) : fromScaledZoom(in.y);
				out = eyeZoom.getOutPercent(i);
				out.y = scaledZoom ? toScaledZoom(out.y)
						: fromScaledZoom(out.y);

				eyeZoom.setValue(i, z);
				eyeZoom.setInPercent(i, in.x, in.y);
				eyeZoom.setOutPercent(i, out.x, out.y);

				z = centerZoom.getValue(i);
				z = scaledZoom ? toScaledZoom(z) : fromScaledZoom(z);
				in = centerZoom.getInPercent(i);
				in.y = scaledZoom ? toScaledZoom(in.y) : fromScaledZoom(in.y);
				out = centerZoom.getOutPercent(i);
				out.y = scaledZoom ? toScaledZoom(out.y)
						: fromScaledZoom(out.y);

				centerZoom.setValue(i, z);
				centerZoom.setInPercent(i, in.x, in.y);
				centerZoom.setOutPercent(i, out.x, out.y);
			}
			
			for (int i = 0; i < size; i++)
			{
				smooth(i);
			}
		}
		this.scaledZoom = scaledZoom;
	}

	public boolean isScaledZoom()
	{
		return scaledZoom;
	}

	public static double toScaledZoom(double camera)
	{
		return Math.log(Math.max(0, camera) + 1);
	}

	public static double fromScaledZoom(double zoom)
	{
		return Math.pow(Math.E, zoom) - 1;
	}

	public int getFrameCount()
	{
		return frameCount;
	}

	public void setFrameCount(int frameCount)
	{
		this.frameCount = frameCount;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
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

	public void load(File file) throws IOException
	{
		String string = FileUtil.readFileAsString(file);
		restoreState(string);
	}

	public void save(File file) throws IOException
	{
		String string = getRestorableState();
		FileUtil.writeStringToFile(string, file);
	}

	public void stateChanged(ChangeEvent e)
	{
		if (!ignoreChange)
			notifyChange();
	}

	public String getRestorableState()
	{
		RestorableSupport restorableSupport = RestorableSupport
				.newRestorableSupport();
		if (restorableSupport == null)
			return null;

		restorableSupport.addStateValueAsInteger("frameCount", frameCount);
		restorableSupport.addStateValueAsInteger("width", width);
		restorableSupport.addStateValueAsInteger("height", height);
		restorableSupport.addStateValueAsBoolean("scaledZoom", scaledZoom);
		restorableSupport.addStateValueAsRestorable("eyeLat", eyeLat);
		restorableSupport.addStateValueAsRestorable("eyeLon", eyeLon);
		restorableSupport.addStateValueAsRestorable("eyeZoom", eyeZoom);
		restorableSupport.addStateValueAsRestorable("centerLat", centerLat);
		restorableSupport.addStateValueAsRestorable("centerLon", centerLon);
		restorableSupport.addStateValueAsRestorable("centerZoom", centerZoom);

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

		frameCount = restorableSupport.getStateValueAsInteger("frameCount");

		Integer width = restorableSupport.getStateValueAsInteger("width");
		if (width != null)
			this.width = width;
		Integer height = restorableSupport.getStateValueAsInteger("height");
		if (height != null)
			this.height = height;
		Boolean scaledZoom = restorableSupport
				.getStateValueAsBoolean("scaledZoom");
		if (scaledZoom != null)
			this.scaledZoom = scaledZoom;

		eyeLat = restorableSupport.getStateValueAsRestorable("eyeLat", eyeLat);
		eyeLon = restorableSupport.getStateValueAsRestorable("eyeLon", eyeLon);
		eyeZoom = restorableSupport.getStateValueAsRestorable("eyeZoom",
				eyeZoom);
		centerLat = restorableSupport.getStateValueAsRestorable("centerLat",
				centerLat);
		centerLon = restorableSupport.getStateValueAsRestorable("centerLon",
				centerLon);
		centerZoom = restorableSupport.getStateValueAsRestorable("centerZoom",
				centerZoom);

		/*if (eyeLat != null && eyeLon != null && eyeZoom != null
				&& centerLat != null && centerLon != null && centerZoom != null)
		{
			this.eyeLat = eyeLat;
			this.eyeLon = eyeLon;
			this.eyeZoom = eyeZoom;
			this.centerLat = centerLat;
			this.centerLon = centerLon;
			this.centerZoom = centerZoom;
			addChangeListener();
		}*/

		notifyChange();
	}
}
