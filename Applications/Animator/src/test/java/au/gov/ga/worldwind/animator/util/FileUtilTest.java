package au.gov.ga.worldwind.animator.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the animator {@link FileUtil} class
 */
public class FileUtilTest
{

	@Test
	public void testStripSequenceNumberEmpty()
	{
		assertEquals("", FileUtil.stripSequenceNumber(""));
	}
	
	@Test
	public void testStripSequenceNumberBlank()
	{
		assertEquals("   ", FileUtil.stripSequenceNumber("   "));
	}
	
	@Test
	public void testStripSequenceNoSequence()
	{
		assertEquals("filename", FileUtil.stripSequenceNumber("filename"));
	}
	
	@Test
	public void testStripSequenceShortSequence()
	{
		assertEquals("filename", FileUtil.stripSequenceNumber("filename01"));
	}
	
	@Test
	public void testStripSequenceLongSequence()
	{
		assertEquals("filename", FileUtil.stripSequenceNumber("filename0005471"));
	}
	
	@Test
	public void testStripSequenceNonSequenceDigits()
	{
		assertEquals("file01nam7e", FileUtil.stripSequenceNumber("file01nam7e0005471"));
	}
	
}
