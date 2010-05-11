package au.gov.ga.worldwind.downloader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferRetrievalResult implements RetrievalResult
{
	private final ByteBuffer buffer;

	public ByteBufferRetrievalResult(ByteBuffer buffer)
	{
		this.buffer = buffer;
	}

	@Override
	public ByteBuffer getAsBuffer()
	{
		return buffer;
	}

	@Override
	public InputStream getAsInputStream()
	{
		if(buffer == null)
			return null;
		return new ByteArrayInputStream(getArray());
	}

	@Override
	public String getAsString()
	{
		if(buffer == null)
			return null;
		return new String(getArray());
	}
	
	private byte[] getArray()
	{
		byte[] array;
		if (buffer.hasArray())
			array = buffer.array();
		else
		{
			array = new byte[buffer.limit()];
			buffer.rewind();
			buffer.get(array);
		}
		return array;
	}

	@Override
	public boolean hasData()
	{
		return buffer != null;
	}
}
