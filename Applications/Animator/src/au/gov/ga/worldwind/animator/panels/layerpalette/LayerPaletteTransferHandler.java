package au.gov.ga.worldwind.animator.panels.layerpalette;

import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getFileListFlavor;
import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getLayerIdentifierFlavor;
import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getURLFlavor;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifierFactory;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.panels.LayerIdentifierTransferable;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A transfer handler for the {@link LayerPalettePanel}
 * <p/>
 * Supports transfers of:
 * <ul>
 * 	<li>Layer identifiers from within the panel (i.e. changing the order of a layer identifier)
 * 	<li>A layer definition file from an external source (i.e. adding new layers to the layer palette)
 * 	<li>TODO Exporting a layer identifier from the palette (e.g. adding a layer to the animation browser panel)
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayerPaletteTransferHandler extends TransferHandler
{
	private static final long serialVersionUID = 20100923L;

	private JList layerList;
	
	public LayerPaletteTransferHandler(JList layerList)
	{
		Validate.notNull(layerList, "A layer list is required");
		
		this.layerList = layerList;
	}
	
	@Override
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		if (c != layerList)
		{
			return null;
		}
		
		return new LayerIdentifierTransferable((LayerIdentifier)layerList.getSelectedValue());
	}
	
	@Override
	public boolean importData(TransferSupport support)
	{
		if (!canImport(support))
		{
			return false;
		}
		
		try
		{
			// Try each flavor in turn until we succeed
			if (support.isDataFlavorSupported(getFileListFlavor()))
			{
				importLayersFromFiles(support);
				return true;
			}
			
			if (support.isDataFlavorSupported(getLayerIdentifierFlavor()))
			{
				importLayerFromIdentifier(support);
				return true;
			}
			
			return false;
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
			return false;
		}
	}
	
	private void importLayersFromFiles(TransferSupport support) throws Exception
	{
		List<?> files = (List<?>)support.getTransferable().getTransferData(getFileListFlavor());
		for (Object fileObject : files)
		{
			if (fileObject instanceof File)
			{
				URL layerUrl = ((File)fileObject).toURI().toURL();
				LayerIdentifier identifier = LayerIdentifierFactory.createFromDefinition(layerUrl);
				
				addIdentifierToList(identifier, getDropIndex(support));
			}
		}
		updateKnownLayers();
	}

	private void importLayerFromIdentifier(TransferSupport support) throws Exception
	{
		LayerIdentifier identifier = (LayerIdentifier)support.getTransferable().getTransferData(getLayerIdentifierFlavor());
		addIdentifierToList(identifier, getDropIndex(support));
	}
	
	private int getDropIndex(TransferSupport support)
	{
		JList.DropLocation dropLocation = (JList.DropLocation)support.getDropLocation();
		return dropLocation.getIndex();
	}
	
	/**
	 * Add the provided identifier to the layer list, removing any existing duplicates
	 */
	private void addIdentifierToList(LayerIdentifier identifier, int index)
	{
		if (identifier == null)
		{
			return;
		}
		
		@SuppressWarnings("unchecked")
		ListBackedModel<LayerIdentifier> listModel = (ListBackedModel<LayerIdentifier>)layerList.getModel();
		
		// Remove existing (duplicate) identifiers and adjust the index accordingly
		int oldIndex = listModel.indexOf(identifier);
		if (oldIndex != -1)
		{
			listModel.remove(oldIndex);
			if (oldIndex < index)
			{
				index--;
			}
		}
		
		listModel.add(index, identifier);
	}

	private void updateKnownLayers()
	{
		@SuppressWarnings("unchecked")
		ListBackedModel<LayerIdentifier> listModel = (ListBackedModel<LayerIdentifier>)layerList.getModel();
		
		Settings.get().setKnownLayers(Arrays.asList(listModel.toArray(new LayerIdentifier[0])));
	}
	
	@Override
	public boolean canImport(TransferSupport support)
	{
		return support.isDrop() &&
				isTargetingLayerList(support) &&
				isSupportedFlavor(support);
	}

	private boolean isTargetingLayerList(TransferSupport support)
	{
		return layerList == support.getComponent();
	}

	private boolean isSupportedFlavor(TransferSupport support)
	{
		return support.isDataFlavorSupported(getURLFlavor()) ||
				support.isDataFlavorSupported(getLayerIdentifierFlavor()) ||
				support.isDataFlavorSupported(getFileListFlavor());
	}
}
