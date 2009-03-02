package path;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.OrbitView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SimpleAnimation implements Serializable
{
	private Parameter eyeLat = new Parameter();
	private Parameter eyeLon = new Parameter();
	private Parameter eyeZoom = new Parameter();
	private Parameter centerLat = new Parameter();
	private Parameter centerLon = new Parameter();
	private Parameter centerZoom = new Parameter();

	public SimpleAnimation()
	{
	}

	public void applyFrame(OrbitView view, int frame)
	{
		Position eye = Position.fromDegrees(eyeLat.getInterpolatedValue(frame), eyeLon
				.getInterpolatedValue(frame), z2c(eyeZoom.getInterpolatedValue(frame)));
		Position center = Position.fromDegrees(centerLat.getInterpolatedValue(frame),
				centerLon.getInterpolatedValue(frame), z2c(centerZoom.getInterpolatedValue(frame)));
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

	public static double c2z(double camera)
	{
		return Math.log(camera + 1);
	}

	public static double z2c(double zoom)
	{
		return Math.pow(Math.E, zoom) - 1;
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
