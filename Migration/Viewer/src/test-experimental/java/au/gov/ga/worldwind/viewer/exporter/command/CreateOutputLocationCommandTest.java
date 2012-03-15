package au.gov.ga.worldwind.viewer.exporter.command;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.exporter.ExporterMessageConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.viewer.exporter.command.CommandExecutionResult.Status;
import au.gov.ga.worldwind.viewer.exporter.parameters.SnapshotExportParameters;

/**
 * Unit tests for the {@link CreateOutputLocationCommand} class
 */
public class CreateOutputLocationCommandTest
{
	private CreateOutputLocationCommand classUnderTest;
	
	@BeforeClass
	public static void setupMessages()
	{
		MessageSourceAccessor.addBundle("au.gov.ga.worldwind.viewer.exporter.exporterMessages");
	}
	
	@Before
	public void setup()
	{
		this.classUnderTest = new CreateOutputLocationCommand();
	}
	
	@Test
	public void testExecuteWithNullParameters()
	{
		SnapshotExportParameters parameters = null;
		
		CommandExecutionResult result = classUnderTest.execute(parameters);
		
		assertNotNull(result);
		assertEquals(Status.HALT, result.getStatus());
		assertEquals(getMessage(getExporterMissingParametersErrorMsgKey()), result.getMessage());
	}
	
	@Test
	public void testExecuteWithIllegalPath()
	{
		SnapshotExportParameters parameters = new SnapshotExportParameters();
		parameters.outputLocation = new File("a:/imaginary/location");
		
		CommandExecutionResult result = classUnderTest.execute(parameters);
		
		assertNotNull(result);
		assertEquals(Status.HALT, result.getStatus());
		assertEquals(getMessage(getExporterCreateOutputLocationErrorMsgKey(), "a:/imaginary/location"), result.getMessage());
	}
	
	@Test
	public void testExecuteWithExistingPath()
	{
		File outputdir = new File(System.getProperty("java.io.tmpdir"), "exportTestDir");
		outputdir.mkdirs();
		outputdir.deleteOnExit();
		
		SnapshotExportParameters parameters = new SnapshotExportParameters();
		parameters.outputLocation = outputdir;
		
		CommandExecutionResult result = classUnderTest.execute(parameters);
		
		assertNotNull(result);
		assertEquals(Status.WARN, result.getStatus());
		assertEquals(getMessage(getExporterOutputLocationExistsMsgKey(), outputdir.getAbsolutePath()), result.getMessage());
		
		outputdir.delete();
	}
	
	@Test
	public void testExecuteWithNonExistingPath()
	{
		File outputdir = new File(System.getProperty("java.io.tmpdir"), "exportTestDir");
		
		SnapshotExportParameters parameters = new SnapshotExportParameters();
		parameters.outputLocation = outputdir;
		
		CommandExecutionResult result = classUnderTest.execute(parameters);
		
		assertNotNull(result);
		assertEquals(Status.CONTINUE, result.getStatus());
		assertEquals(null, result.getMessage());
		
		outputdir.delete();
		outputdir.deleteOnExit();
	}
}
