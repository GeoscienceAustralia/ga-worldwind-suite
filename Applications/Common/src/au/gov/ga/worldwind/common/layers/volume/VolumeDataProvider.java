package au.gov.ga.worldwind.common.layers.volume;

import java.awt.Rectangle;

import au.gov.ga.worldwind.common.layers.data.DataProvider;
import au.gov.ga.worldwind.common.util.FastShape;

public interface VolumeDataProvider extends DataProvider<VolumeLayer>
{
	int getXSize();
	int getYSize();
	int getZSize();
	double getDepth();
	double getTop();
	float getValue(int x, int y, int z);
	float getNoDataValue();
	FastShape createHorizontalSurface(float maxVariance, Rectangle rectangle);
	TopBottomFastShape createLatitudeCurtain(int x);
	TopBottomFastShape createLongitudeCurtain(int y);
}