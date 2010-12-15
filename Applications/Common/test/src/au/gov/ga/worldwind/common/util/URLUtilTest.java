package au.gov.ga.worldwind.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URL;

import org.junit.Test;

/**
 * Unit tests for the {@link URLUtil} class
 */
public class URLUtilTest
{

	// stripQuery()

	@Test
	public void testStripQueryWithNull()
	{
		URL url = null;

		URL result = URLUtil.stripQuery(url);

		assertNull(result);
	}

	@Test
	public void testStripQueryWithNoQuery() throws Exception
	{
		URL url = new URL("http://www.some.url.com/without/query.html");

		URL result = URLUtil.stripQuery(url);

		assertNotNull(result);
		assertEquals("http://www.some.url.com/without/query.html", result.toExternalForm());
	}

	@Test
	public void testStripQueryWithQuery() throws Exception
	{
		URL url = new URL("http://www.some.url.com/with/query.html?param1=value1&param2=value2");

		URL result = URLUtil.stripQuery(url);

		assertNotNull(result);
		assertEquals("http://www.some.url.com/with/query.html", result.toExternalForm());
	}

	
	// urlToFile()
	@Test
	public void testUrlToFileWithNull()
	{
		File result = URLUtil.urlToFile(null);
		
		assertEquals(null, result);
	}
	
	@Test
	public void testUrlToFileWithFileUrl() throws Exception
	{
		File result = URLUtil.urlToFile(new URL("file://c:/this/is/a/file.extension"));
		
		assertEquals("C:\\this\\is\\a\\file.extension", result.getAbsolutePath());
	}
	
	@Test
	public void testUrlToFileWithHttpUrl() throws Exception
	{
		File result = URLUtil.urlToFile(new URL("http://this/is/not/a/file.html"));
		
		assertEquals(null, result);
	}
}
