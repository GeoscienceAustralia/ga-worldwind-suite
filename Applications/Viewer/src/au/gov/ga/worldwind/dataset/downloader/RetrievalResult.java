package au.gov.ga.worldwind.dataset.downloader;

import java.io.InputStream;
import java.nio.ByteBuffer;

public interface RetrievalResult
{
	public boolean hasData();
	public ByteBuffer getAsBuffer();
	public String getAsString();
	public InputStream getAsInputStream();
}
