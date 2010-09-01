package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

/**
 * Defines a 'kit' of delegates, and also implements all the delegate interfaces
 * and acts as a proxy class for the delegates stored in the global variables.
 * 
 * @author Michael de Hoog
 */
public class DelegateKit implements TileRequesterDelegate, RetrieverFactoryDelegate,
		TileFactoryDelegate, ImageReaderDelegate, ImageTransformerDelegate
{
	/**
	 * Stores the definitions of the default delegates. See
	 * createDelegateElements() for usage.
	 */
	private final static Collection<String> defaultDelegateDefinitions = new HashSet<String>();

	static
	{
		Collection<Delegate> defaultDelegates = new DelegateKit().getDelegates();
		for (Delegate delegate : defaultDelegates)
		{
			defaultDelegateDefinitions.add(delegate.toDefinition());
		}
	}

	//default delegate implementations
	private TileRequesterDelegate requesterDelegate = new URLRequesterDelegate();
	private RetrieverFactoryDelegate retrieverDelegate = new HttpRetrieverFactoryDelegate();
	private TileFactoryDelegate factoryDelegate = new TextureTileFactoryDelegate();
	private final List<ImageReaderDelegate> readerDelegates = new ArrayList<ImageReaderDelegate>();
	private final List<ImageTransformerDelegate> transformerDelegates =
			new ArrayList<ImageTransformerDelegate>();

	/**
	 * Set the {@link TileRequesterDelegate}.
	 * 
	 * @param requesterDelegate
	 */
	public void setRequesterDelegate(TileRequesterDelegate requesterDelegate)
	{
		this.requesterDelegate = requesterDelegate;
	}

	/**
	 * Set the {@link RetrieverFactoryDelegate}.
	 * 
	 * @param retrieverDelegate
	 */
	public void setRetrieverDelegate(RetrieverFactoryDelegate retrieverDelegate)
	{
		this.retrieverDelegate = retrieverDelegate;
	}

	/**
	 * Set the {@link TileFactoryDelegate}.
	 * 
	 * @param factoryDelegate
	 */
	public void setFactoryDelegate(TileFactoryDelegate factoryDelegate)
	{
		this.factoryDelegate = factoryDelegate;
	}

	/**
	 * Add an {@link ImageReaderDelegate}.
	 * 
	 * @param readerDelegate
	 */
	public void addReaderDelegate(ImageReaderDelegate readerDelegate)
	{
		readerDelegates.add(readerDelegate);
	}

	/**
	 * Add an {@link ImageTransformerDelegate}.
	 * 
	 * @param transformerDelegate
	 */
	public void addTransformerDelegate(ImageTransformerDelegate transformerDelegate)
	{
		transformerDelegates.add(transformerDelegate);
	}

	/**
	 * Create a new {@link DelegateKit} from an XML element.
	 * 
	 * @param domElement
	 *            XML element
	 * @return New {@link DelegateKit}
	 */
	public static DelegateKit createFromXML(Element domElement)
	{
		DelegateKit kit = new DelegateKit();

		XPath xpath = WWXML.makeXPath();
		Element delegatesElement = WWXML.getElement(domElement, "Delegates", xpath);
		if (delegatesElement != null)
		{
			Element[] elements = WWXML.getElements(delegatesElement, "Delegate", xpath);
			if (elements != null)
			{
				for (Element element : elements)
				{
					String definition = element.getTextContent();
					if (definition != null && definition.length() > 0)
					{
						Delegate delegate = DelegateFactory.createDelegate(definition);
						kit.setOrAddDelegate(delegate);
					}
				}
			}
		}
		return kit;
	}

	/**
	 * Save the provided {@link DelegateKit} to XML.
	 * 
	 * @param kit
	 *            {@link DelegateKit} to save
	 * @param context
	 *            XML element under which to add the elements.
	 * @return context
	 */
	public static Element createDelegateElements(DelegateKit kit, Element context)
	{
		Collection<Delegate> delegates = kit.getDelegates();
		Element delegatesElement = WWXML.appendElement(context, "Delegates");
		for (Delegate delegate : delegates)
		{
			String definition = delegate.toDefinition();
			//only append the XML element if the delegate is not one of the defaults
			if (!defaultDelegateDefinitions.contains(definition))
			{
				WWXML.appendText(delegatesElement, "Delegate", definition);
			}
		}
		return context;
	}

	/**
	 * Helper method to set or add a delegate, depending on it's type.
	 * 
	 * @param delegate
	 */
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

	/**
	 * @return A Collection of delegates for this kit
	 */
	private Collection<Delegate> getDelegates()
	{
		Set<Delegate> delegates = new HashSet<Delegate>();
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
	public TileKey transformTileKey(TileKey tileKey)
	{
		return factoryDelegate.transformTileKey(tileKey);
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
	public URL getLocalTileURL(TextureTile tile, DelegatorTiledImageLayer layer,
			boolean searchClassPath)
	{
		return requesterDelegate.getLocalTileURL(tile, layer, searchClassPath);
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
