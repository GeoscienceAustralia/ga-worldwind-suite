package au.gov.ga.worldwind.common.layers.model.gocad;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import au.gov.ga.worldwind.common.util.FastShape;

public class GocadFactoryTest
{
	@Test
	public void testTSurf() throws IOException
	{
		URL url = this.getClass().getResource("tsurf.ts");
		InputStream is = url.openStream();
		GocadReaderParameters parameters = new GocadReaderParameters();
		List<FastShape> shapes = GocadFactory.read(is, url, parameters);
		assertEquals(1, shapes.size());
		assertEquals(3, shapes.get(0).getPositions().size());
	}
	
	@Test
	public void testPLine() throws IOException
	{
		URL url = this.getClass().getResource("pline.gp");
		InputStream is = url.openStream();
		GocadReaderParameters parameters = new GocadReaderParameters();
		List<FastShape> shapes = GocadFactory.read(is, url, parameters);
		assertEquals(1, shapes.size());
		assertEquals(2, shapes.get(0).getPositions().size());
	}
}
