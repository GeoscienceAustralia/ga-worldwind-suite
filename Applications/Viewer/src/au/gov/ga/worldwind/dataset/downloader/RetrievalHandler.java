package au.gov.ga.worldwind.dataset.downloader;

public interface RetrievalHandler
{
	public void handle(RetrievalResult result, boolean cached);
}