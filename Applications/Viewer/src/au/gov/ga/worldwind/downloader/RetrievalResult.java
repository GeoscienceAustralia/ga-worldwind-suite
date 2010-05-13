package au.gov.ga.worldwind.downloader;

import java.io.InputStream;
import java.nio.ByteBuffer;

public interface RetrievalResult
{
	public boolean hasData();
	public ByteBuffer getAsBuffer();
	public String getAsString();
	public InputStream getAsInputStream();
	public boolean isFromCache();
	public boolean isNotModified();
	public Exception getError();
}
