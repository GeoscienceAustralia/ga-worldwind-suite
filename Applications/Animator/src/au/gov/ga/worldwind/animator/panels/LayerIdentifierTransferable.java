package au.gov.ga.worldwind.animator.panels;

import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.*;

import java.awt.datatransfer.DataFlavor;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;

import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A transferable object used to transfer {@link LayerIdentifier}s.
 * <p/>
 * Supports:
 * <ul>
 * 	<li>the {@link String} flavor (returns the layer name), 
 * 	<li>the {@link URL} flavor (returns the layer URL}, and 
 * 	<li>the {@link LayerIdentifier} flavor (returns the identifer itself)
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class LayerIdentifierTransferable implements Transferable
{
	private static final DataFlavor[] SUPPORTED_FLAVORS = new DataFlavor[]{getStringFlavor(), getURLFlavor(), getLayerIdentifierFlavor()};
	
	private LayerIdentifier identifier;
	
	public LayerIdentifierTransferable(LayerIdentifier identifier)
	{
		Validate.notNull(identifier, "An identifier is required");
		this.identifier = identifier;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return SUPPORTED_FLAVORS;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		for (DataFlavor supportedFlavor : SUPPORTED_FLAVORS)
		{
			if (supportedFlavor.equals(flavor))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (getStringFlavor().equals(flavor))
		{
			return identifier.getName();
		}
		
		if (getURLFlavor().equals(flavor))
		{
			return new URL(identifier.getLocation());
		}
		
		if (getLayerIdentifierFlavor().equals(flavor))
		{
			return identifier;
		}
		
		return null;
	}

}
