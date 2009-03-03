package path;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.OrbitView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import camera.vector.Vector3;

public class SimpleAnimation implements Serializable
{
	private Parameter eyeLat = new Parameter();
	private Parameter eyeLon = new Parameter();
	private Parameter eyeZoom = new Parameter();
	private Parameter centerLat = new Parameter();
	private Parameter centerLon = new Parameter();
	private Parameter centerZoom = new Parameter();
	private int frameCount = 100;

	public SimpleAnimation()
	{
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

	public void applyFrame(OrbitView view, int frame)
	{
		Position eye = Position.fromDegrees(eyeLat.getInterpolatedValue(frame),
				eyeLon.getInterpolatedValue(frame), z2c(eyeZoom
						.getInterpolatedValue(frame)));
		Position center = Position.fromDegrees(centerLat
				.getInterpolatedValue(frame), centerLon
				.getInterpolatedValue(frame), z2c(centerZoom
				.getInterpolatedValue(frame)));
		view.stopMovement();
		view.setOrientation(eye, center);
	}

	public void addFrame(OrbitView view, int frame)
	{
		Position eye = view.getEyePosition();
		Position center = view.getCenterPosition();
		eyeLat.addKey(frame, eye.getLatitude().degrees);
		eyeLon.addKey(frame, eye.getLongitude().degrees);
		eyeZoom.addKey(frame, c2z(eye.getElevation()));
		centerLat.addKey(frame, center.getLatitude().degrees);
		centerLon.addKey(frame, center.getLongitude().degrees);
		centerZoom.addKey(frame, c2z(center.getElevation()));

		int index = eyeLat.indexOf(frame);
		smooth(index);
		if (index > 0)
			smooth(index - 1);
		if (index < size() - 1)
			smooth(index + 1);

		frameCount = Math.max(frame, frameCount);
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

	public void removeFrame(int index)
	{
		eyeLat.removeKey(index);
		eyeLon.removeKey(index);
		eyeZoom.removeKey(index);
		centerLat.removeKey(index);
		centerLon.removeKey(index);
		centerZoom.removeKey(index);
	}

	public int getFrame(int index)
	{
		return eyeLat.getFrame(index);
	}

	public int indexOf(int frame)
	{
		return eyeLat.indexOf(frame);
	}

	public int size()
	{
		return eyeLat.size();
	}

	public int getFirstFrame()
	{
		return eyeLat.getFirstFrame();
	}

	public int getLastFrame()
	{
		return eyeLat.getLastFrame();
	}

	public void smoothEyeSpeed()
	{
		int size = size();
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
					cumulativeDistance[i] += vStart.subtract(vEnd).distance();
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
			frames[i] = (int) Math.round((getLastFrame() - getFirstFrame() + 1)
					* cumulativeDistance[i - 1] / cumulativeDistance[size - 2]);
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
	}

	public static double c2z(double camera)
	{
		return Math.log(Math.max(0, camera) + 1);
	}

	public static double z2c(double zoom)
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

	public static SimpleAnimation load(File file)
	{
		SimpleAnimation sa = null;
		ObjectInputStream ois = null;
		try
		{
			ois = new ObjectInputStream(new FileInputStream(file));
			Object object = ois.readObject();
			if (object != null && object instanceof SimpleAnimation)
			{
				sa = (SimpleAnimation) object;
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			if (ois != null)
			{
				try
				{
					ois.close();
				}
				catch (Exception e)
				{
				}
			}
		}
		return sa;
	}

	public void save(File file)
	{
		ObjectOutputStream oos = null;
		try
		{
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (oos != null)
			{
				try
				{
					oos.close();
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
