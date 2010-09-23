package au.gov.ga.worldwind.animator.panels.layerpalette;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A transfer handler for the {@link LayerPalettePanel}
 * <p/>
 * Supports transfers of:
 * <ul>
 * 	<li>TODO Layer identifiers from within the panel (i.e. changing the order of a layer identifier)
 * 	<li>TODO A layer definition file from an external source (i.e. adding new layers to the layer palette)
 * 	<li>TODO Exporting a layer identifier from the palette (e.g. adding a layer to the animation browser panel)
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayerPaletteTransferHandler extends TransferHandler
{
	private static final long serialVersionUID = 20100923L;

	private Animation animation;
	
	private JList layerList;
	
	public LayerPaletteTransferHandler(Animation animation, JList layerList)
	{
		Validate.notNull(animation, "An animation is required");
		Validate.notNull(layerList, "A layer list is required");
		
		this.animation = animation;
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
		
		return super.createTransferable(c);
	}
	
}
