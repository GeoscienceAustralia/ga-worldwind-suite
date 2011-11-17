package gov.nasa.worldwind.custom.render;

import net.java.joglutils.model.geometry.Model;

public class PickableModel extends Model
{
	protected boolean renderPicker = false;
	
	public PickableModel(String source)
	{
		super(source);
	}

	public boolean isRenderPicker()
	{
		return renderPicker;
	}

	public void setRenderPicker(boolean renderPicker)
	{
		this.renderPicker = renderPicker;
	}
}
