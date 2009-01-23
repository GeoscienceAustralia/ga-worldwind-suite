package layers.shader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.media.opengl.GL;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;

public class NightLightsLayer extends BasicTiledImageLayer
{
	private int shaderprogram = -1;
	private int eyeuniform = -1;
	private int sununiform = -1;

	public NightLightsLayer()
	{
		super(makeLevels());
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 1024);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, "GA/Night Lights");
		params.setValue(AVKey.SERVICE, "http://localhost/tiles/tiles.php");
		params.setValue(AVKey.DATASET_NAME, "sidebyside");
		params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
		params.setValue(AVKey.NUM_LEVELS, 3);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);

		return new LevelSet(params);
	}

	@Override
	public String toString()
	{
		return "Night lights";
	}

	@Override
	public void render(DrawContext dc)
	{
		GL gl = dc.getGL();

		if (shaderprogram == -1)
		{
			int v = gl.glCreateShader(GL.GL_VERTEX_SHADER);
			int f = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
			String vsrc = "", fsrc = "", line;

			try
			{
				BufferedReader brv = new BufferedReader(new InputStreamReader(
						this.getClass()
								.getResourceAsStream("vertexshader.glsl")));
				while ((line = brv.readLine()) != null)
				{
					vsrc += line + "\n";
				}

				BufferedReader brf = new BufferedReader(new InputStreamReader(
						this.getClass().getResourceAsStream(
								"fragmentshader.glsl")));
				while ((line = brf.readLine()) != null)
				{
					fsrc += line + "\n";
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			gl.glShaderSource(v, 1, new String[] { vsrc }, new int[] { vsrc
					.length() }, 0);
			gl.glCompileShader(v);
			gl.glShaderSource(f, 1, new String[] { fsrc }, new int[] { fsrc
					.length() }, 0);
			gl.glCompileShader(f);

			shaderprogram = gl.glCreateProgram();
			gl.glAttachShader(shaderprogram, v);
			gl.glAttachShader(shaderprogram, f);
			gl.glLinkProgram(shaderprogram);
			gl.glValidateProgram(shaderprogram);

			gl.glUseProgram(shaderprogram);
			gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex0"), 0);
			gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex1"), 1);
			eyeuniform = gl.glGetUniformLocation(shaderprogram, "eyePosition");
			sununiform = gl.glGetUniformLocation(shaderprogram, "sunPosition");
			
			System.out.println("Shader program = " + shaderprogram
					+ ", eye uniform = " + eyeuniform + ", sun uniform = "
					+ sununiform);

			Vec4 sunPosition = new Vec4(-15f, 10f, -40f);
			sunPosition = sunPosition.normalize3();
			gl.glUniform3f(sununiform, (float) sunPosition.x,
					(float) sunPosition.y, (float) sunPosition.z);
		}

		gl.glUseProgram(shaderprogram);
		Vec4 eye = dc.getView().getEyePoint();
		gl.glUniform3f(eyeuniform, (float) eye.x, (float) eye.y, (float) eye.z);
		super.render(dc);
		gl.glUseProgram(0);
	}
}
