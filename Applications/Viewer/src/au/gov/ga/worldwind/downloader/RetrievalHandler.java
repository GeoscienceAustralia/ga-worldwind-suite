package au.gov.ga.worldwind.downloader;

public interface RetrievalHandler
{
	public void handle(RetrievalResult result, boolean cached);
}
