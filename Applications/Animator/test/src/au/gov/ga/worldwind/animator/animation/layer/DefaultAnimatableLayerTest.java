package au.gov.ga.worldwind.animator.animation.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
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
import au.gov.ga.worldwind.animator.layers.AnimationLayerLoader;
import au.gov.ga.worldwind.animator.util.WorldWindowTestImpl;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.layers.LayerFactory;
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
	
	private MockLayerFactory layerFactory;
	
	@Before
	public void setup()
	{
		intialiseMessageSource();
		
		mockContext = new Mockery();
		
		layer = mockContext.mock(Layer.class);
		
		animation = new WorldWindAnimationImpl(new WorldWindowTestImpl());
		
		initialiseParameters();
		
		classToBeTested = new DefaultAnimatableLayer("testLayer", layer, layerParameters);
		
		setupLayerFactory();
	}
	
	@After
	public void tearDown()
	{
		AnimationLayerLoader.setLayerFactory(new LayerFactory());
	}
	
	private void setupLayerFactory()
	{
		layerFactory = new MockLayerFactory();
		AnimationLayerLoader.setLayerFactory(layerFactory);
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
		setLayerUrl("file://marl/sandpit/symbolic-links/world-wind/current/dataset/ga/gravity/edition3/gravity.xml");
		addKeyFrame(0, 0.1, layerParameters.get(0));
		addKeyFrame(10, 1.0, layerParameters.get(0));
		
		Document xmlDocument = WWXML.createDocumentBuilder(false).newDocument();
		Element animatableObjectsElement = xmlDocument.createElement("animatableObjects");
		Element xmlElement = classToBeTested.toXml(animatableObjectsElement, AnimationFileVersion.VERSION020);
		assertNotNull(xmlElement);
		animatableObjectsElement.appendChild(xmlElement);
		xmlDocument.appendChild(animatableObjectsElement);
		
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
		context.setValue(AnimationFileVersion.VERSION020.getConstants().getAnimationKey(), animation);
		
		AnimationFileVersion versionId = AnimationFileVersion.VERSION020;
		
		Document document = WWXML.openDocument(getClass().getResourceAsStream("animatableLayerXmlSnippet.xml"));
		Element layerElement = WWXML.getElement(document.getDocumentElement(), "//" + versionId.getConstants().getAnimatableLayerName(), null);
		
		setLayerUrlExpectation(new URL("file://marl/sandpit/symbolic-links/world-wind/current/dataset/ga/gravity/edition3/gravity.xml"));
		setLayerEnabledExpectation();
		layerFactory.setResult(layer);
		
		DefaultAnimatableLayer result = (DefaultAnimatableLayer)classToBeTested.fromXml(layerElement, versionId, context);
		
		// Check the layer was created
		assertNotNull(result);
		assertEquals(layer, result.getLayer());
		assertEquals("testLayer", result.getName());
		
		// Check the opacity parameter was created
		assertEquals(1, result.getParameters().size());
		
		LayerParameter opacityParameter = result.getParameterOfType(LayerParameter.Type.OPACITY);
		assertNotNull(opacityParameter);
		
		// Check the key frames were correctly created
		assertEquals(2, animation.getKeyFrameCount());
		
		ParameterValue v0 = animation.getKeyFrame(0).getValueForParameter(opacityParameter);
		assertNotNull(v0);
		assertEquals(0.1, v0.getValue(), 0.001);
		
		ParameterValue v10 = animation.getKeyFrame(10).getValueForParameter(opacityParameter);
		assertNotNull(v10);
		assertEquals(1.0, v10.getValue(), 0.001);
		
	}

	private void setLayerEnabledExpectation()
	{
		mockContext.checking(new Expectations(){{
			atLeast(1).of(layer).setEnabled(with(true));
		}});
		
	}

	private void setLayerUrlExpectation(final URL url) throws Exception
	{
		mockContext.checking(new Expectations(){{
			atLeast(1).of(layer).setValue(with(AVKeyMore.CONTEXT_URL), with(url));
		}});
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

	/**
	 * A mock layer factory that can have results set on it for testing purposes.
	 */
	private static class MockLayerFactory extends BasicLayerFactory
	{
		private Object result = null;
		
		@Override
		public Object createFromConfigSource(Object configSource, AVList params)
		{
			return result;
		}
		
		@Override
		public Object createFromCapabilities(String capsFileName, AVList params)
		{
			return result;
		}
	
		public void setResult(Object result)
		{
			this.result = result;
		}
		
	}
}
