package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

public class DelegateKit implements TileRequesterDelegate, RetrieverFactoryDelegate,
		TileFactoryDelegate, ImageReaderDelegate, ImageTransformerDelegate
{
	private TileRequesterDelegate requesterDelegate = new URLRequesterDelegate();
	private RetrieverFactoryDelegate retrieverDelegate =
			new PassThroughZipRetrieverFactoryDelegate();
	private TileFactoryDelegate factoryDelegate = new TextureTileFactoryDelegate();
	private final List<ImageReaderDelegate> readerDelegates = new ArrayList<ImageReaderDelegate>();
	private final List<ImageTransformerDelegate> transformerDelegates =
			new ArrayList<ImageTransformerDelegate>();

	public void setRequesterDelegate(TileRequesterDelegate requesterDelegate)
	{
		this.requesterDelegate = requesterDelegate;
	}

	public void setRetrieverDelegate(RetrieverFactoryDelegate retrieverDelegate)
	{
		this.retrieverDelegate = retrieverDelegate;
	}

	public void setFactoryDelegate(TileFactoryDelegate factoryDelegate)
	{
		this.factoryDelegate = factoryDelegate;
	}

	public void addReaderDelegate(ImageReaderDelegate readerDelegate)
	{
		readerDelegates.add(readerDelegate);
	}

	public void addTransformerDelegate(ImageTransformerDelegate transformerDelegate)
	{
		transformerDelegates.add(transformerDelegate);
	}

	public static DelegateKit createFromXML(Element element)
	{
		DelegateKit kit = new DelegateKit();
		Element[] elements = WWXML.getElements(element, "Delegate", null);
		for (Element elem : elements)
		{
			String definition = elem.getTextContent();
			if (definition != null && definition.length() > 0)
			{
				Delegate delegate = DelegateFactory.createDelegate(definition);
				kit.setOrAddDelegate(delegate);
			}
		}
		return kit;
	}

	public static Element createDelegateElements(DelegateKit kit, Element context)
	{
		List<Delegate> delegates = kit.getDelegates();
		for(Delegate delegate : delegates)
		{
			WWXML.appendText(context, "Delegate", delegate.toDefinition());
		}
		return context;
	}

	private void setOrAddDelegate(Delegate delegate)
	{
		boolean valid = false;
		if (delegate instanceof TileRequesterDelegate)
		{
			setRequesterDelegate((TileRequesterDelegate) delegate);
			valid = true;
		}
		if (delegate instanceof RetrieverFactoryDelegate)
		{
			setRetrieverDelegate((RetrieverFactoryDelegate) delegate);
			valid = true;
		}
		if (delegate instanceof TileFactoryDelegate)
		{
			setFactoryDelegate((TileFactoryDelegate) delegate);
			valid = true;
		}
		if (delegate instanceof ImageReaderDelegate)
		{
			addReaderDelegate((ImageReaderDelegate) delegate);
			valid = true;
		}
		if (delegate instanceof ImageTransformerDelegate)
		{
			addTransformerDelegate((ImageTransformerDelegate) delegate);
			valid = true;
		}
		if (!valid)
		{
			throw new IllegalArgumentException("Unrecognized delegate: " + delegate);
		}
	}

	private List<Delegate> getDelegates()
	{
		List<Delegate> delegates = new ArrayList<Delegate>();
		delegates.add(requesterDelegate);
		delegates.add(retrieverDelegate);
		delegates.add(factoryDelegate);
		delegates.addAll(readerDelegates);
		delegates.addAll(transformerDelegates);
		return delegates;
	}

	/* ******************************
	 * Delegate Interface functions *
	 ****************************** */

	@Override
	public BufferedImage transformImage(BufferedImage image)
	{
		for (ImageTransformerDelegate transformer : transformerDelegates)
		{
			image = transformer.transformImage(image);
		}
		return image;
	}

	@Override
	public BufferedImage readImage(URL url) throws IOException
	{
		for (ImageReaderDelegate reader : readerDelegates)
		{
			BufferedImage image = reader.readImage(url);
			if (image != null)
				return image;
		}
		return null;
	}

	@Override
	public TextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return factoryDelegate.createTextureTile(sector, level, row, col);
	}

	@Override
	public Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		return retrieverDelegate.createRetriever(url, postProcessor);
	}

	@Override
	public void forceTextureLoad(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		requesterDelegate.forceTextureLoad(tile, layer);
	}

	@Override
	public Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		return requesterDelegate.createRequestTask(tile, layer);
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		return null;
	}

	@Override
	public String toDefinition()
	{
		return null;
	}
}
