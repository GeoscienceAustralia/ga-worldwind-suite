package au.gov.ga.worldwind.common.layers.curtain.delegate;

import au.gov.ga.worldwind.common.layers.delegate.AbstractDelegateFactory;

/**
 * Factory which creates delegates for the {@link DelegatorTiledCurtainLayer}.
 * 
 * @author Michael de Hoog
 */
public class CurtainDelegateFactory extends AbstractDelegateFactory
{
	private static CurtainDelegateFactory instance = new CurtainDelegateFactory();

	public static CurtainDelegateFactory get()
	{
		return instance;
	}

	private CurtainDelegateFactory()
	{
		super();

		//register the specific delegates applicable to this Image factory
		registerDelegate(CurtainURLRequesterDelegate.class);
		registerDelegate(CurtainLocalRequesterDelegate.class);
		registerDelegate(CurtainTextureTileFactoryDelegate.class);
	}
}
