package au.gov.ga.worldwind.androidremote.shared.messages;

import java.nio.ByteBuffer;

import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.MessageId;

/**
 * GPS location message.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LocationMessage implements Message<LocationMessage>
{
	public final double latitude;
	public final double longitude;
	public final double altitude;
	public final float accuracy;
	public final float bearing;

	@SuppressWarnings("unused")
	private LocationMessage()
	{
		this(0, 0, 0, 0, 0);
	}

	public LocationMessage(double latitude, double longitude, double altitude, float accuracy, float bearing)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.accuracy = accuracy;
		this.bearing = bearing;
	}

	@Override
	public MessageId getId()
	{
		return MessageId.LOCATION;
	}

	@Override
	public int getLength()
	{
		return 32;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		MessageIO.doubleToBytes(latitude, buffer);
		MessageIO.doubleToBytes(longitude, buffer);
		MessageIO.doubleToBytes(altitude, buffer);
		MessageIO.floatToBytes(accuracy, buffer);
		MessageIO.floatToBytes(bearing, buffer);
	}

	@Override
	public LocationMessage fromBytes(ByteBuffer buffer)
	{
		double latitude = MessageIO.bytesToDouble(buffer);
		double longitude = MessageIO.bytesToDouble(buffer);
		double altitude = MessageIO.bytesToDouble(buffer);
		float accuracy = MessageIO.bytesToFloat(buffer);
		float bearing = MessageIO.bytesToFloat(buffer);
		return new LocationMessage(latitude, longitude, altitude, accuracy, bearing);
	}
}
