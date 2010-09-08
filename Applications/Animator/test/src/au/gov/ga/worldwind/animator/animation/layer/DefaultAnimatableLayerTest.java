package au.gov.ga.worldwind.animator.animation.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerOpacityParameter;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueType;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;
import au.gov.ga.worldwind.test.util.TestUtils;

/**
 * Unit tests for the {@link DefaultAnimatableLayer} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class DefaultAnimatableLayerTest
{

	private Mockery mockContext;
	
	private Layer layer;

	private Animation animation;

	private List<LayerParameter> layerParameters;
	
	private StaticMessageSource messageSource;

	private DefaultAnimatableLayer classToBeTested;
	
	@Before
	public void setup()
	{
		intialiseMessageSource();
		
		mockContext = new Mockery();
		
		layer = mockContext.mock(Layer.class);
		
		animation = new WorldWindAnimationImpl(new WorldWindowGLCanvas());
		
		initialiseParameters();
		
		classToBeTested = new DefaultAnimatableLayer("testLayer", layer, layerParameters);
	}
	
	private void intialiseMessageSource()
	{
		messageSource = new StaticMessageSource();
		
		messageSource.addMessage(AnimationMessageConstants.getOpacityParameterNameKey(), "opacity");
		
		MessageSourceAccessor.set(messageSource);
	}

	private void initialiseParameters()
	{
		layerParameters = new ArrayList<LayerParameter>();
		layerParameters.add(new LayerOpacityParameter(animation, layer));
	}

	/**
	 * Tests the {@link DefaultAnimatableLayer#toXml()} method
	 */
	@Test
	public void testToXml() throws Exception
	{
		setLayerUrl("file://somefile/somewhere.layer");
		addKeyFrame(0, 0.1, layerParameters.get(0));
		addKeyFrame(10, 1.0, layerParameters.get(0));
		
		Document xmlDocument = WWXML.createDocumentBuilder(false).newDocument();
		Element xmlElement = classToBeTested.toXml(xmlDocument.createElement("test"), AnimationFileVersion.VERSION020);
		assertNotNull(xmlElement);
		xmlDocument.appendChild(xmlElement);
		
		ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
		XMLUtil.saveDocumentToFormattedStream(xmlDocument, resultStream );
		
		String result = normalise(new String(resultStream.toByteArray()));
		String expected = normalise(TestUtils.readStreamToString(getClass().getResourceAsStream("animatableLayerXmlSnippet.xml")));
		
		assertEquals(expected, result);
	}
	
	/**
	 * Tests the {@link DefaultAnimatableLayer#fromXml()} method
	 */
	@Test
	public void testFromXml() throws Exception
	{
		AVList context = new AVListImpl();
		AnimationFileVersion versionId = AnimationFileVersion.VERSION020;
		Element element = WWXML.openDocument(getClass().getResourceAsStream("animatableLayerXmlSnippet.xml")).getDocumentElement();
		
		DefaultAnimatableLayer result = (DefaultAnimatableLayer)classToBeTested.fromXml(element, versionId , context);
		
		assertNotNull(result);
	}

	private void addKeyFrame(int frame, double value, LayerParameter layerParameter)
	{
		ParameterValue paramValue = ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, layerParameter, value, frame);
		KeyFrame keyFrame = new KeyFrameImpl(frame, Arrays.asList(new ParameterValue[]{paramValue}));
		animation.insertKeyFrame(keyFrame);
	}

	private void setLayerUrl(final String urlString) throws Exception
	{
		mockContext.checking(new Expectations(){{
			atLeast(1).of(layer).getValue(with(AVKeyMore.CONTEXT_URL)); will(returnValue(new URL(urlString)));
		}});
	}
	
	private String normalise(String target)
	{
		return target.trim().replace("\r\n", "\n");
	}

}
