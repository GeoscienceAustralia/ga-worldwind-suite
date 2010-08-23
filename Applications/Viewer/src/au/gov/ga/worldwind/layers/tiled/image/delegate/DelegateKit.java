package au.gov.ga.worldwind.layers.tiled.image.delegate;

public class DelegateKit
{
	private TileFactoryDelegate factoryDelegate;
	private TileRequesterDelegate requesterDelegate;
	private ImageReaderDelegate readerDelegate;
	private ImageTransformerDelegate transformerDelegate;
	private RetrieverFactoryDelegate retrieverDelegate;

	public TileFactoryDelegate getFactoryDelegate()
	{
		return factoryDelegate;
	}

	public void setFactoryDelegate(TileFactoryDelegate factoryDelegate)
	{
		this.factoryDelegate = factoryDelegate;
	}

	public TileRequesterDelegate getRequesterDelegate()
	{
		return requesterDelegate;
	}

	public void setRequesterDelegate(TileRequesterDelegate requesterDelegate)
	{
		this.requesterDelegate = requesterDelegate;
	}

	public ImageReaderDelegate getReaderDelegate()
	{
		return readerDelegate;
	}

	public void setReaderDelegate(ImageReaderDelegate readerDelegate)
	{
		this.readerDelegate = readerDelegate;
	}

	public ImageTransformerDelegate getTransformerDelegate()
	{
		return transformerDelegate;
	}

	public void setTransformerDelegate(ImageTransformerDelegate transformerDelegate)
	{
		this.transformerDelegate = transformerDelegate;
	}

	public RetrieverFactoryDelegate getRetrieverDelegate()
	{
		return retrieverDelegate;
	}

	public void setRetrieverDelegate(RetrieverFactoryDelegate retrieverDelegate)
	{
		this.retrieverDelegate = retrieverDelegate;
	}

	public static DelegateKit defaultDelegateKit()
	{
		DelegateKit kit = new DelegateKit();
		kit.setFactoryDelegate(new TextureTileFactoryDelegate());
		kit.setRequesterDelegate(new URLRequesterDelegate());
		kit.setRetrieverDelegate(new IgnoreZipRetrieverFactoryDelegate());
		return kit;
	}
}
