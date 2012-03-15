package au.gov.ga.worldwind.animator.animation.io;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

/**
 * Test the transformation of V1 files to V2
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class V1ToV2TransformerTest
{

	@Test
	public void testTranformation() throws Exception
	{
		// Transform the file using the xslt stylesheet
		Source v1FileSource = new StreamSource(getClass().getResourceAsStream("v1AnimationFile.xml"));
		Source xsltSource = new StreamSource(getClass().getResourceAsStream("v1ToV2Transformer.xsl"));
		
		ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
		Result transformationResult = new StreamResult(resultStream);
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
		
		transformer.transform(v1FileSource, transformationResult);
		
		// Check the results
		String resultString = resultStream.toString();
		String expectedString =  readStream(getClass().getResourceAsStream("transformationExpectedOutput.xml"));
		
		assertEquals(expectedString, resultString);
	}

	private String readStream(InputStream stream) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer result = new StringBuffer();
		String readLine = null;
		while((readLine = reader.readLine()) != null)
		{
			result.append(readLine);
		}
		reader.close();
		return result.toString();
	}
}
