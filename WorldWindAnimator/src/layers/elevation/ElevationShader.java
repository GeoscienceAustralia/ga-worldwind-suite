package layers.elevation;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import javax.media.opengl.GL;

public class ElevationShader extends AbstractLayer
{
	private SurfaceTile tile;
	private int shaderprogram = -1;

	private int equatorialRadiusUniform;
	private int eccentricitySquaredUniform;
	private int minElevationUniform;
	private int maxElevationUniform;
	private int referenceCenterUniform;
	private int opacityUniform;

	public ElevationShader()
	{
		this(Sector.FULL_SPHERE);
	}

	public ElevationShader(Sector sector)
	{
		tile = new Tile(sector);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		GL gl = dc.getGL();
		View viewBackup = dc.getView();
		View viewProxy = ViewProxy.newInstance(viewBackup, this);
		dc.setView(viewProxy);

		if (shaderprogram == -1)
		{
			setupShader(dc);
			System.out.println("shaderprogram = " + shaderprogram);
		}

		try
		{
			if (!dc.isPickingMode())
			{
				gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT);
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			}
			else
			{
				gl.glPushAttrib(GL.GL_POLYGON_BIT);
			}

			gl.glUseProgram(shaderprogram);

			/*Sector visibleSector = dc.getVisibleSector();
			double[] extremeElevations = dc.getGlobe().getElevationModel()
					.getExtremeElevations(visibleSector);
			gl.glUniform1f(minElevationUniform, (float) extremeElevations[0]);
			gl.glUniform1f(maxElevationUniform, (float) extremeElevations[1]);*/

			double minElevation = ((ElevationTesselator) dc.getGlobe()
					.getTessellator()).getMinElevation();
			double maxElevation = ((ElevationTesselator) dc.getGlobe()
					.getTessellator()).getMaxElevation();

			gl.glUniform1f(minElevationUniform, (float) minElevation);
			gl.glUniform1f(maxElevationUniform, (float) maxElevation);

			gl.glUniform1f(equatorialRadiusUniform, (float) dc.getGlobe()
					.getEquatorialRadius());
			gl.glUniform1f(eccentricitySquaredUniform, (float) dc.getGlobe()
					.getEccentricitySquared());

			gl.glUniform1f(opacityUniform, (float) getOpacity());

			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glCullFace(GL.GL_BACK);

			dc.getGeographicSurfaceTileRenderer().renderTile(dc, tile);

			gl.glUseProgram(0);
		}
		finally
		{
			gl.glPopAttrib();
			dc.setView(viewBackup);
		}
	}

	private void setupShader(DrawContext dc)
	{
		GL gl = dc.getGL();
		int v = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		String vsrc = "", fsrc = "", line;

		try
		{
			BufferedReader brv = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream("vertexshader.glsl")));
			while ((line = brv.readLine()) != null)
			{
				vsrc += line + "\n";
			}

			BufferedReader brf = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream("fragmentshader.glsl")));
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
		equatorialRadiusUniform = gl.glGetUniformLocation(shaderprogram,
				"equatorialRadius");
		eccentricitySquaredUniform = gl.glGetUniformLocation(shaderprogram,
				"eccentricitySquared");
		minElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"minElevation");
		maxElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"maxElevation");
		referenceCenterUniform = gl.glGetUniformLocation(shaderprogram,
				"referenceCenter");
		opacityUniform = gl.glGetUniformLocation(shaderprogram, "opacity");

		/*gl.glUniform1f(equatorialRadiusUniform, (float) dc.getGlobe()
				.getEquatorialRadius());
		gl.glUniform1f(eccentricitySquaredUniform, (float) dc.getGlobe()
				.getEccentricitySquared());
		gl.glUniform1f(minElevationUniform, (float) dc.getGlobe()
				.getMinElevation());
		gl.glUniform1f(maxElevationUniform, (float) dc.getGlobe()
				.getMaxElevation());*/
	}

	private void pushReferenceCenter(DrawContext dc, Vec4 referenceCenter)
	{
		GL gl = dc.getGL();
		gl.glUniform3f(referenceCenterUniform, (float) referenceCenter.x,
				(float) referenceCenter.y, (float) referenceCenter.z);
	}

	private static class Tile implements SurfaceTile
	{
		private Sector sector;

		public Tile(Sector sector)
		{
			this.sector = sector;
		}

		public void applyInternalTransform(DrawContext dc)
		{
		}

		public boolean bind(DrawContext dc)
		{
			return true;
		}

		public Extent getExtent(DrawContext dc)
		{
			return null;
		}

		public Sector getSector()
		{
			return sector;
		}

		public List<? extends LatLon> getCorners()
		{
			return null;
		}
	}

	private static class ViewProxy implements InvocationHandler
	{
		public static View newInstance(View view, ElevationShader shader)
		{
			return (View) Proxy.newProxyInstance(view.getClass()
					.getClassLoader(), view.getClass().getInterfaces(),
					new ViewProxy(view, shader));
		}

		private static Method pushReferenceCenterMethod;

		static
		{
			try
			{
				pushReferenceCenterMethod = View.class.getMethod(
						"pushReferenceCenter", new Class[] { DrawContext.class,
								Vec4.class });
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private View view;
		private ElevationShader shader;

		private ViewProxy(View view, ElevationShader shader)
		{
			this.view = view;
			this.shader = shader;
		}

		private void pushReferenceCenter(DrawContext dc, Vec4 referenceCenter)
		{
			shader.pushReferenceCenter(dc, referenceCenter);
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable
		{
			Object result;
			try
			{
				result = method.invoke(view, args);
				if (method.equals(pushReferenceCenterMethod)
						&& args.length == 2 && args[0] instanceof DrawContext
						&& args[1] instanceof Vec4)
				{
					pushReferenceCenter((DrawContext) args[0], (Vec4) args[1]);
				}
			}
			catch (InvocationTargetException e)
			{
				throw e.getTargetException();
			}
			catch (Exception e)
			{
				throw new RuntimeException("unexpected invocation exception: "
						+ e.getMessage());
			}
			finally
			{
			}
			return result;
		}
	}
}
