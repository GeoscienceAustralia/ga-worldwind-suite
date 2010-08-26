package au.gov.ga.worldwind.viewer.downloader;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

public interface RetrievalResult
{
	public URL getSourceURL();
	public boolean hasData();
	public ByteBuffer getAsBuffer();
	public String getAsString();
	public InputStream getAsInputStream();
	public boolean isFromCache();
	public boolean isNotModified();
	public Exception getError();
}
