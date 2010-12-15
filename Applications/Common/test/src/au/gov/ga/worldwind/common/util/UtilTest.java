package au.gov.ga.worldwind.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;

import java.net.URL;

import org.junit.Test;

/**
 * Unit tests for the {@link Util} class
 */
public class UtilTest
{
	private static final double ALLOWABLE_DOUBLE_ERROR = 0.0001;

	// stripQuery()
	
	@Test
	public void testStripQueryWithNull()
	{
		URL url = null;
		
		URL result = Util.stripQuery(url);
		
		assertNull(result);
	}
	
	@Test
	public void testStripQueryWithNoQuery() throws Exception
	{
		URL url = new URL("http://www.some.url.com/without/query.html");
		
		URL result = Util.stripQuery(url);
		
		assertNotNull(result);
		assertEquals("http://www.some.url.com/without/query.html", result.toExternalForm());
	}
	
	@Test
	public void testStripQueryWithQuery() throws Exception
	{
		URL url = new URL("http://www.some.url.com/with/query.html?param1=value1&param2=value2");
		
		URL result = Util.stripQuery(url);
		
		assertNotNull(result);
		assertEquals("http://www.some.url.com/with/query.html", result.toExternalForm());
	}
	
	// computeVec4FromString()
	
	@Test
	public void testComputeVec4FromStringWithNull() throws Exception
	{
		String vectorString = null;
		
		Vec4 result = Util.computeVec4FromString(vectorString);
		
		assertNull(result);
	}
	
	@Test
	public void testComputeVec4FromStringWithBlank() throws Exception
	{
		String vectorString = "";
		
		Vec4 result = Util.computeVec4FromString(vectorString);
		
		assertNull(result);
	}
	
	@Test
	public void testComputeVec4FromStringWith3VecBracesWhitespace() throws Exception
	{
		String vectorString = "(1, 2, 3)";
		
		Vec4 result = Util.computeVec4FromString(vectorString);
		
		assertNotNull(result);
		assertEquals(1, result.x, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(2, result.y, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(3, result.z, ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testComputeVec4FromStringWith4VecNoBracesNoWhitespace() throws Exception
	{
		String vectorString = "1,2,3,4";
		
		Vec4 result = Util.computeVec4FromString(vectorString);
		
		assertNotNull(result);
		assertEquals(1, result.x, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(2, result.y, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(3, result.z, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(4, result.w, ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testComputeVec4FromStringWith2Vec() throws Exception
	{
		String vectorString = "(1, 2)";
		
		Vec4 result = Util.computeVec4FromString(vectorString);
		
		assertNull(result);
	}
	
	@Test
	public void testComputeVec4FromStringWith4Vec() throws Exception
	{
		String vectorString = "(1, 2, 3, 4, 5)";
		
		Vec4 result = Util.computeVec4FromString(vectorString);
		
		assertNull(result);
	}
	
	@Test
	public void testComputeVec4FromStringWithInvalidString() throws Exception
	{
		String vectorString = "(1, a, 3)";
		
		Vec4 result = Util.computeVec4FromString(vectorString);
		
		assertNull(result);
	}
	
	// clamp() number methods
	
	@Test
	public void testClampWithIntegerValueLessThanMin()
	{
		assertEquals(5, Util.clamp(4, 5, 10));
	}
	
	@Test
	public void testClampWithIntegerValueEqualMin()
	{
		assertEquals(5, Util.clamp(5, 5, 10));
	}
	
	@Test
	public void testClampWithIntegerValueInRange()
	{
		assertEquals(7, Util.clamp(7, 5, 10));
	}
	
	@Test
	public void testClampWithIntegerValueEqualMax()
	{
		assertEquals(10, Util.clamp(10, 5, 10));
	}
	
	@Test
	public void testClampWithIntegerValueGreaterThanMax()
	{
		assertEquals(10, Util.clamp(11, 5, 10));
	}
	
	@Test
	public void testClampWithIntegerValueMinEqualMax()
	{
		assertEquals(10, Util.clamp(11, 10, 10));
	}
	
	@Test
	public void testClampWithIntegerValueMinGreaterThanMax()
	{
		try
		{
			Util.clamp(7, 10, 9);
			fail("Expected exception");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	@Test
	public void testClampWithDoubleValueLessThanMin()
	{
		assertEquals(5d, Util.clamp(4.9d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testClampWithDoubleValueEqualMin()
	{
		assertEquals(5d, Util.clamp(5d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testClampWithDoubleValueInRange()
	{
		assertEquals(7d, Util.clamp(7d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testClampWithDoubleValueEqualMax()
	{
		assertEquals(10d, Util.clamp(10d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testClampWithDoubleValueGreaterThanMax()
	{
		assertEquals(10d, Util.clamp(10.1d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testClampWithDoubleValueMinEqualMax()
	{
		assertEquals(10d, Util.clamp(11d, 10d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}
	
	@Test
	public void testClampWithDoubleValueMinGreaterThanMax()
	{
		try
		{
			Util.clamp(7d, 10d, 9d);
			fail("Expected exception");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	// clampLatLon()
	
	@Test
	public void testClampLatLonWithNullLatLon()
	{
		LatLon source = null;
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		LatLon result = Util.clampLatLon(source, extents);
		
		assertNull(result);
	}
	
	@Test
	public void testClampLatLonWithNullExtents()
	{
		LatLon source = new LatLon(Angle.NEG90, Angle.NEG90);
		Sector extents = null;
		
		LatLon result = Util.clampLatLon(source, extents);
		
		assertEquals(source, result);
	}
	
	@Test
	public void testClampLatLonWithSourceWithinExtents()
	{
		LatLon source = new LatLon(Angle.fromDegrees(-77.345), Angle.fromDegrees(12.56));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		LatLon result = Util.clampLatLon(source, extents);
		
		assertEquals(source, result);
	}
	
	@Test
	public void testClampLatLonWithSourceOutsideExtentsLat()
	{
		LatLon source = new LatLon(Angle.fromDegrees(-97.345), Angle.fromDegrees(12.56));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		LatLon result = Util.clampLatLon(source, extents);
		
		assertEquals(new LatLon(extents.getMinLatitude(), source.longitude), result);
	}
	
	@Test
	public void testClampLatLonWithSourceOutsideExtentsLon()
	{
		LatLon source = new LatLon(Angle.fromDegrees(-77.345), Angle.fromDegrees(112.56));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		LatLon result = Util.clampLatLon(source, extents);
		
		assertEquals(new LatLon(source.latitude, extents.getMaxLongitude()), result);
	}
	
	// clampSector()
	
	@Test
	public void testClampSectorWithNullSource()
	{
		Sector source = null;
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		Sector result = Util.clampSector(source, extents);
		
		assertEquals(null, result);
	}
	
	@Test
	public void testClampSectorWithNullExtents()
	{
		Sector source = new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(56.66), Angle.fromDegrees(1.01), Angle.fromDegrees(87.05));
		Sector extents = null;
		
		Sector result = Util.clampSector(source, extents);
		
		assertEquals(source, result);
	}
	
	@Test
	public void testClampSectorWithSourceWithinExtents()
	{
		Sector source = new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(56.66), Angle.fromDegrees(1.01), Angle.fromDegrees(87.05));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		Sector result = Util.clampSector(source, extents);
		
		assertEquals(source, result);
	}
	
	@Test
	public void testClampSectorWithSourceOverlapExtents()
	{
		Sector source = new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(96.66), Angle.fromDegrees(1.01), Angle.fromDegrees(97.05));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		Sector result = Util.clampSector(source, extents);
		
		Sector expected = new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(90), Angle.fromDegrees(1.01), Angle.fromDegrees(90));
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testClampSectorWithSourceOutsideExtents()
	{
		Sector source = new Sector(Angle.fromDegrees(91.34), Angle.fromDegrees(96.66), Angle.fromDegrees(91.01), Angle.fromDegrees(97.05));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);
		
		Sector result = Util.clampSector(source, extents);
		
		Sector expected = new Sector(Angle.fromDegrees(90), Angle.fromDegrees(90), Angle.fromDegrees(90), Angle.fromDegrees(90));
		
		assertEquals(expected, result);
	}
	
}
