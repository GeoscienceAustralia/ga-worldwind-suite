package au.gov.ga.worldwind.common.layers.point;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.render.Material;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link Style} class
 */
public class StyleTest
{
	private static final double ALLOWABLE_ERROR = 0.0001;
	private Style classToTest;

	@Before
	public void setup()
	{
		classToTest = new Style("myStyle", true);
	}
	
	@Test
	public void testSetPropertiesFromAttributesWithSimpleAttributes()
	{
		classToTest.addProperty("littleIInteger", "1", null);
		classToTest.addProperty("bigIInteger", "2", null);
		classToTest.addProperty("littleLLong", "3", null);
		classToTest.addProperty("bigLLong", "0xFF", null);
		classToTest.addProperty("littleDDouble", "5.5", null);
		classToTest.addProperty("bigDDouble", "6E6", null);
		classToTest.addProperty("littleFFloat", "7.7f", null);
		classToTest.addProperty("bigFFloat", "8E-1", null);
		classToTest.addProperty("littleBBoolean", "true", null);
		classToTest.addProperty("bigBBoolean", "TRUE", null);
		classToTest.addProperty("string", "This is a String!", null);
		classToTest.addProperty("littleCCharacter", "y", null);
		classToTest.addProperty("bigCCharacter", "&", null);
		classToTest.addProperty("littleBByte", "0xF", null);
		classToTest.addProperty("bigBByte", "8", null);
		
		SimpleDummyBean dummyBean = new SimpleDummyBean();
		classToTest.setPropertiesFromAttributes(null, null, dummyBean);
		
		assertEquals(1, dummyBean.getLittleIInteger());
		assertEquals((Integer)2, dummyBean.getBigIInteger());
		assertEquals(3, dummyBean.getLittleLLong());
		assertEquals((Long)255L, dummyBean.getBigLLong());
		assertEquals(5.5, dummyBean.getLittleDDouble(), ALLOWABLE_ERROR);
		assertEquals(6000000, dummyBean.getBigDDouble(), ALLOWABLE_ERROR);
		assertEquals(7.7, dummyBean.getLittleFFloat(), ALLOWABLE_ERROR);
		assertEquals(0.8, dummyBean.getBigFFloat(), ALLOWABLE_ERROR);
		assertEquals(true, dummyBean.isLittleBBoolean());
		assertEquals(true, dummyBean.getBigBBoolean());
		assertEquals("This is a String!", dummyBean.getString());
		assertEquals('y', dummyBean.getLittleCCharacter());
		assertEquals((Character)'&', dummyBean.getBigCCharacter());
		assertEquals(0xF, dummyBean.getLittleBByte());
		assertEquals((Byte)(byte)8, dummyBean.getBigBByte());
	}
	
	@Test
	public void testSetPropertiesFromAttributesWithComplexAttributes()
	{
		classToTest.addProperty("material", "255,0,0", null);
		classToTest.addProperty("color", "0,255,0", null);
		classToTest.addProperty("dimension", "100,200", null);
		classToTest.addProperty("point", "300,400", null);
		classToTest.addProperty("font", "Arial-BOLD-18", null);
		
		ComplexDummyBean dummyBean = new ComplexDummyBean();
		classToTest.setPropertiesFromAttributes(null, null, dummyBean);
		
		assertEquals(new Material(new Color(255,0,0)), dummyBean.getMaterial());
		assertEquals(new Color(0,255,0), dummyBean.getColor());
		assertEquals(new Dimension(100,200), dummyBean.getDimension());
		assertEquals(new Point(300,400), dummyBean.getPoint());
		assertEquals(new Font("Arial", Font.BOLD, 18), dummyBean.getFont());
	}
	
	@Test
	public void testSetPropertiesFromAttributesWithAttributeSubstitution()
	{
		classToTest.addProperty("littleIInteger", "%int%", null);
		classToTest.addProperty("material", "%material%", null);
		classToTest.addProperty("string", "%string%", null);
		classToTest.addProperty("point", "%point%", null);
		
		AVList attributeValues = new AVListImpl();
		attributeValues.setValue("int", "1");
		attributeValues.setValue("material", "0,0,255");
		attributeValues.setValue("string", "A simple String!");
		attributeValues.setValue("point", "500, 600");
		
		ComplexDummyBean complexBean = new ComplexDummyBean();
		SimpleDummyBean simpleBean = new SimpleDummyBean();
		
		classToTest.setPropertiesFromAttributes(null, attributeValues, simpleBean, complexBean);
		
		assertEquals(1, simpleBean.getLittleIInteger());
		assertEquals("A simple String!", simpleBean.getString());
		
		assertEquals(new Material(new Color(0,0,255)), complexBean.getMaterial());
		assertEquals(new Point(500,600), complexBean.getPoint());
	}
	
	
	/**
	 * A dummy bean used to test the setting of complex attribute values
	 */
	@SuppressWarnings("unused")
	private class ComplexDummyBean
	{
		Material material = null;
		Color color = null;
		Dimension dimension = null;
		Font font = null;
		Point point = null;
		public Material getMaterial()
		{
			return material;
		}
		public void setMaterial(Material material)
		{
			this.material = material;
		}
		public Color getColor()
		{
			return color;
		}
		public void setColor(Color color)
		{
			this.color = color;
		}
		public Dimension getDimension()
		{
			return dimension;
		}
		public void setDimension(Dimension dimension)
		{
			this.dimension = dimension;
		}
		public Font getFont()
		{
			return font;
		}
		public void setFont(Font font)
		{
			this.font = font;
		}
		public Point getPoint()
		{
			return point;
		}
		public void setPoint(Point point)
		{
			this.point = point;
		}
	}
	
	/**
	 * A dummy bean used to test the setting of simple attribute values
	 */
	@SuppressWarnings("unused")
	private class SimpleDummyBean
	{
		int littleIInteger = -1;
		Integer bigIInteger = null;
		long littleLLong = -1;
		Long bigLLong = null;
		double littleDDouble = -1;
		Double bigDDouble = null;
		float littleFFloat = -1;
		Float bigFFloat = null;
		boolean littleBBoolean = false;
		Boolean bigBBoolean = null;
		String string = null;
		char littleCCharacter = ' ';
		Character bigCCharacter = null;
		byte littleBByte = -1;
		Byte bigBByte = null;
		public int getLittleIInteger()
		{
			return littleIInteger;
		}
		public void setLittleIInteger(int littleIInteger)
		{
			this.littleIInteger = littleIInteger;
		}
		public Integer getBigIInteger()
		{
			return bigIInteger;
		}
		public void setBigIInteger(Integer bigIInteger)
		{
			this.bigIInteger = bigIInteger;
		}
		public long getLittleLLong()
		{
			return littleLLong;
		}
		public void setLittleLLong(long littleLLong)
		{
			this.littleLLong = littleLLong;
		}
		public Long getBigLLong()
		{
			return bigLLong;
		}
		public void setBigLLong(Long bigLLong)
		{
			this.bigLLong = bigLLong;
		}
		public double getLittleDDouble()
		{
			return littleDDouble;
		}
		public void setLittleDDouble(double littleDDouble)
		{
			this.littleDDouble = littleDDouble;
		}
		public Double getBigDDouble()
		{
			return bigDDouble;
		}
		public void setBigDDouble(Double bigDDouble)
		{
			this.bigDDouble = bigDDouble;
		}
		public float getLittleFFloat()
		{
			return littleFFloat;
		}
		public void setLittleFFloat(float littleFFloat)
		{
			this.littleFFloat = littleFFloat;
		}
		public Float getBigFFloat()
		{
			return bigFFloat;
		}
		public void setBigFFloat(Float bigFFloat)
		{
			this.bigFFloat = bigFFloat;
		}
		public boolean isLittleBBoolean()
		{
			return littleBBoolean;
		}
		public void setLittleBBoolean(boolean littleBBoolean)
		{
			this.littleBBoolean = littleBBoolean;
		}
		public Boolean getBigBBoolean()
		{
			return bigBBoolean;
		}
		public void setBigBBoolean(Boolean bigBBoolean)
		{
			this.bigBBoolean = bigBBoolean;
		}
		public String getString()
		{
			return string;
		}
		public void setString(String string)
		{
			this.string = string;
		}
		public char getLittleCCharacter()
		{
			return littleCCharacter;
		}
		public void setLittleCCharacter(char littleCCharacter)
		{
			this.littleCCharacter = littleCCharacter;
		}
		public Character getBigCCharacter()
		{
			return bigCCharacter;
		}
		public void setBigCCharacter(Character bigCCharacter)
		{
			this.bigCCharacter = bigCCharacter;
		}
		public byte getLittleBByte()
		{
			return littleBByte;
		}
		public void setLittleBByte(byte littleBByte)
		{
			this.littleBByte = littleBByte;
		}
		public Byte getBigBByte()
		{
			return bigBByte;
		}
		public void setBigBByte(Byte bigBByte)
		{
			this.bigBByte = bigBByte;
		}
	}
}
