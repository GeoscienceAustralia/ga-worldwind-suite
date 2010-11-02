package au.gov.ga.worldwind.common.view.stereo;

import gov.nasa.worldwind.geom.Matrix;
import au.gov.ga.worldwind.common.view.state.ViewStateBasicFlyView;

public class StereoFlyView extends ViewStateBasicFlyView implements StereoView
{
	private StereoViewHelper helper = new StereoViewHelper();

	@Override
	public void setup(boolean stereo, Eye eye)
	{
		helper.setup(stereo, eye);
	}
	
	@Override
	public boolean isStereo()
	{
		return helper.isStereo();
	}

	@Override
	public Eye getEye()
	{
		return helper.getEye();
	}

	@Override
	public StereoViewParameters getParameters()
	{
		return helper.getParameters();
	}

	@Override
	public void setParameters(StereoViewParameters parameters)
	{
		helper.setParameters(parameters);
	}
	
	@Override
	public double getCurrentFocalLength()
	{
		return helper.getCurrentFocalLength();
	}
	
	@Override
	public double getCurrentEyeSeparation()
	{
		return helper.getCurrentEyeSeparation();
	}

	@Override
	public Matrix calculateProjectionMatrix(double nearDistance, double farDistance)
	{
		return helper.calculateProjectionMatrix(this, nearDistance, farDistance);
	}

	@Override
	public void beforeComputeMatrices()
	{
		super.beforeComputeMatrices();
		helper.beforeComputeMatrices(this);
	}

	@Override
	public Matrix computeModelView()
	{
		Matrix matrix = super.computeModelView();
		return helper.transformModelView(matrix);
	}

	@Override
	public Matrix computeProjection()
	{
		return helper.computeProjection(this);
	}
}
