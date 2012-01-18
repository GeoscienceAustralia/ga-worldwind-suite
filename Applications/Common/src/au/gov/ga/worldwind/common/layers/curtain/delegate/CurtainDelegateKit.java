package au.gov.ga.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.avlist.AVList;

import java.util.Collection;
import java.util.HashSet;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.curtain.CurtainLevel;
import au.gov.ga.worldwind.common.layers.curtain.Segment;
import au.gov.ga.worldwind.common.layers.delegate.AbstractDelegateKit;
import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IDelegateFactory;
import au.gov.ga.worldwind.common.layers.delegate.IDelegateKit;
import au.gov.ga.worldwind.common.layers.delegate.IRetrieverFactoryDelegate;
import au.gov.ga.worldwind.common.layers.delegate.retriever.HttpRetrieverFactoryDelegate;

/**
 * {@link IDelegateKit} implementation for providing specific delegates to the
 * {@link DelegatorTiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainDelegateKit extends AbstractDelegateKit<DelegatorCurtainTextureTile, Segment, CurtainLevel>
{
	private final static Collection<String> defaultDelegateDefinitions = new HashSet<String>();

	static
	{
		Collection<IDelegate> defaultDelegates = new CurtainDelegateKit().getDelegates();
		for (IDelegate delegate : defaultDelegates)
		{
			defaultDelegateDefinitions.add(delegate.toDefinition(null));
		}
	}

	public CurtainDelegateKit()
	{
		requesterDelegate =
				(CurtainURLRequesterDelegate) getFactory().createDelegate(
						new CurtainURLRequesterDelegate().toDefinition(null), null, null);
		retrieverDelegate =
				(IRetrieverFactoryDelegate) getFactory().createDelegate(
						new HttpRetrieverFactoryDelegate().toDefinition(null), null, null);
		factoryDelegate =
				(CurtainTextureTileFactoryDelegate) getFactory().createDelegate(
						new CurtainTextureTileFactoryDelegate().toDefinition(null), null, null);
	}

	@Override
	public CurtainDelegateKit createFromXML(Element domElement, AVList params)
	{
		return (CurtainDelegateKit) super.createFromXML(domElement, params);
	}

	@Override
	protected AbstractDelegateKit<DelegatorCurtainTextureTile, Segment, CurtainLevel> createNewInstance()
	{
		return new CurtainDelegateKit();
	}

	@Override
	public boolean isDefault(String definition)
	{
		return defaultDelegateDefinitions.contains(definition);
	}

	@Override
	protected boolean trySetOrAddDelegate(IDelegate delegate)
	{
		if (super.trySetOrAddDelegate(delegate))
		{
			return true;
		}
		if (delegate instanceof ICurtainTileRequesterDelegate)
		{
			setTileRequesterDelegate((ICurtainTileRequesterDelegate) delegate);
			return true;
		}
		if (delegate instanceof ICurtainTileFactoryDelegate)
		{
			setTileFactoryDelegate((ICurtainTileFactoryDelegate) delegate);
			return true;
		}
		return false;
	}

	@Override
	protected IDelegateFactory getFactory()
	{
		return CurtainDelegateFactory.get();
	}
}
