package au.gov.ga.worldwind.application;

import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sun.opengl.impl.NativeLibLoader;

public class NativeJOGLLibs
{
	private final static String BASE_DIR = "/jogl/";

	public static void init()
	{
		String osName = System.getProperty("os.name").toLowerCase();
		String osArch = System.getProperty("os.arch").toLowerCase();
		String directory, prefix, suffix;
		String[] libraries = new String[] { "jogl", "jogl_awt", "jogl_cg",
				"gluegen-rt" };
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		boolean macosx = false;

		if (osName.startsWith("wind"))
		{
			prefix = "";
			suffix = ".dll";
			if (osArch.startsWith("x86_64") || osArch.startsWith("amd64"))
			{
				directory = "windows-amd64";
			}
			else
			{
				directory = "windows-i586";
			}
		}
		else if (osName.startsWith("mac os x"))
		{
			macosx = true;
			prefix = "lib";
			suffix = ".jnilib";
			if (osArch.startsWith("ppc"))
			{
				directory = "macosx-ppc";
			}
			else
			{
				directory = "macosx-universal";
			}
		}
		else if (osName.startsWith("linux"))
		{
			prefix = "lib";
			suffix = ".so";
			if (osArch.startsWith("x86_64") || osArch.startsWith("amd64"))
			{
				directory = "linux-amd64";
			}
			else
			{
				directory = "linux-i586";
			}
		}
		else if (osName.startsWith("sun") || osName.startsWith("solaris"))
		{
			prefix = "lib";
			suffix = ".so";
			if (osArch.startsWith("sparcv9"))
			{
				directory = "solaris-sparcv9";
			}
			else if (osArch.startsWith("sparc"))
			{
				directory = "solaris-sparc";
			}
			else if (osArch.startsWith("x86_64") || osArch.startsWith("amd64"))
			{
				directory = "solaris-amd64";
			}
			else
			{
				directory = "solaris-i586";
			}
		}
		else
		{
			return;
		}

		for (String lib : libraries)
		{
			String filename = prefix + lib + suffix;
			String library = BASE_DIR + directory + "/" + filename;
			InputStream is = NativeJOGLLibs.class.getResourceAsStream(library);
			if (is != null)
			{
				File file = new File(tmpdir, filename);
				writeStreamToFile(is, file);
				if (file.exists())
				{
					file.deleteOnExit();

					//carry out preloads
					//see NativeLibLoader for description of this
					try
					{
						if (lib.equals("jogl_awt"))
						{
							Toolkit.getDefaultToolkit();
							if (!macosx)
							{
								System.loadLibrary("jawt");
							}
						}
						else if (lib.equals("jogl_cg"))
						{
							System.loadLibrary("cg");
							System.loadLibrary("cgGL");
						}
					}
					catch (Error e)
					{
					}
					catch (Exception e)
					{
					}

					//load library
					try
					{
						System.load(file.getAbsolutePath());
						NativeLibLoader.disableLoading();
					}
					catch (Error e)
					{
					}
					catch (Exception e)
					{
					}
				}
			}
		}
	}

	private static void writeStreamToFile(InputStream is, File file)
	{
		byte[] buffer = new byte[512];
		BufferedOutputStream out = null;
		try
		{
			try
			{
				out = new BufferedOutputStream(new FileOutputStream(file));
				while (true)
				{
					int read = is.read(buffer);
					if (read > -1)
					{
						out.write(buffer, 0, read);
					}
					else
					{
						break;
					}
				}
			}
			finally
			{
				if (out != null)
				{
					out.close();
				}
			}
		}
		catch (IOException e)
		{
		}
	}
}
