package au.gov.ga.worldwind.common.layers.tiled.image.elevation;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.media.opengl.GL;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IRenderDelegate;

public class ShaderRenderDelegate implements IRenderDelegate
{
	private final static String DEFINITION_STRING = "ShaderRender";

	private boolean calenderDirty = true;
	private Calendar calendar = new GregorianCalendar();

	protected double exaggeration = 10.0;
	protected Vec4 sunPosition = new Vec4(1, 1, 1);
	protected Vec4 sunPositionNormalized = sunPosition.normalize3();
	protected double bakedExaggeration = 100.0;

	private int shaderProgram = -1;
	private int exaggerationUniform;
	private int bakedExaggerationUniform;
	private int opacityUniform;
	private int eyePositionUniform;
	private int sunPositionUniform;
	private int oldModelViewInverseUniform;

	private boolean dialogSetup = false;

	@Override
	public void preRender(DrawContext dc)
	{
		setupDialog(dc.getModel());
		updateSunPosition(dc);
		setupShader(dc);
	}

	@Override
	public void postRender(DrawContext dc)
	{
		packupShader(dc);
	}

	protected void updateSunPosition(DrawContext dc)
	{
		if (calenderDirty)
		{
			LatLon latlon = SunCalculator.subsolarPoint(calendar);
			sunPosition = dc.getGlobe().computePointFromLocation(latlon);
			sunPositionNormalized = sunPosition.normalize3();
			calenderDirty = false;
		}
	}

	protected void setupDialog(final Model model)
	{
		if (!dialogSetup)
		{
			JFrame frame = new JFrame();
			frame.setLayout(new GridLayout(0, 1));

			JSlider slider = new JSlider(1, 100, (int) exaggeration);
			frame.add(slider);
			slider.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					exaggeration = ((JSlider) e.getSource()).getValue();
					model.firePropertyChange(DEFINITION_STRING, null, null);
				}
			});

			final JSlider sliderd = new JSlider(0, 365, calendar.get(Calendar.DAY_OF_YEAR));
			frame.add(sliderd);
			final JSlider sliderh =
					new JSlider(0, 24 * 60 - 1, calendar.get(Calendar.HOUR_OF_DAY) * 60
							+ calendar.get(Calendar.MINUTE));
			frame.add(sliderh);

			ChangeListener cl = new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					calendar.set(Calendar.DAY_OF_YEAR, sliderd.getValue());
					calendar.set(Calendar.HOUR_OF_DAY, sliderh.getValue() / 60);
					calendar.set(Calendar.MINUTE, sliderh.getValue() % 60);
					calenderDirty = true;
					model.firePropertyChange(DEFINITION_STRING, null, null);
				}
			};
			sliderd.addChangeListener(cl);
			sliderh.addChangeListener(cl);

			frame.pack();
			frame.setVisible(true);
			dialogSetup = true;
		}
	}

	protected void setupShader(DrawContext dc)
	{
		if (shaderProgram == -1)
		{
			initShader(dc);
		}

		GL gl = dc.getGL();
		gl.glUseProgram(shaderProgram);

		gl.glUniform1f(exaggerationUniform, (float) exaggeration);
		gl.glUniform1f(bakedExaggerationUniform, (float) bakedExaggeration);
		gl.glUniform1f(opacityUniform, (float) dc.getCurrentLayer().getOpacity());

		Matrix modelViewInv = dc.getView().getModelviewMatrix().getInverse();
		float[] modelViewInvArray =
				new float[] { (float) modelViewInv.m11, (float) modelViewInv.m21,
						(float) modelViewInv.m31, (float) modelViewInv.m41,
						(float) modelViewInv.m12, (float) modelViewInv.m22,
						(float) modelViewInv.m32, (float) modelViewInv.m42,
						(float) modelViewInv.m13, (float) modelViewInv.m23,
						(float) modelViewInv.m33, (float) modelViewInv.m43,
						(float) modelViewInv.m14, (float) modelViewInv.m24,
						(float) modelViewInv.m34, (float) modelViewInv.m44 };
		gl.glUniformMatrix4fv(oldModelViewInverseUniform, 1, false, modelViewInvArray, 0);

		Vec4 eye = dc.getView().getEyePoint();
		gl.glUniform3f(eyePositionUniform, (float) eye.x, (float) eye.y, (float) eye.z);
		gl.glUniform3f(sunPositionUniform, (float) sunPositionNormalized.x,
				(float) sunPositionNormalized.y, (float) sunPositionNormalized.z);
	}

	protected void initShader(DrawContext dc)
	{
		GL gl = dc.getGL();
		int v = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		String vsrc = "", fsrc = "", line;

		try
		{
			BufferedReader brv =
					new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
							"vertexshader.glsl")));
			while ((line = brv.readLine()) != null)
			{
				vsrc += line + "\n";
			}

			BufferedReader brf =
					new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
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

		gl.glShaderSource(v, 1, new String[] { vsrc }, new int[] { vsrc.length() }, 0);
		gl.glCompileShader(v);
		gl.glShaderSource(f, 1, new String[] { fsrc }, new int[] { fsrc.length() }, 0);
		gl.glCompileShader(f);

		shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, v);
		gl.glAttachShader(shaderProgram, f);
		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);

		gl.glUseProgram(shaderProgram);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex0"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex1"), 1);
		exaggerationUniform = gl.glGetUniformLocation(shaderProgram, "exaggeration");
		bakedExaggerationUniform = gl.glGetUniformLocation(shaderProgram, "bakedExaggeration");
		opacityUniform = gl.glGetUniformLocation(shaderProgram, "opacity");
		eyePositionUniform = gl.glGetUniformLocation(shaderProgram, "eyePosition");
		sunPositionUniform = gl.glGetUniformLocation(shaderProgram, "sunPosition");
		oldModelViewInverseUniform = gl.glGetUniformLocation(shaderProgram, "oldModelViewInverse");
	}

	protected void packupShader(DrawContext dc)
	{
		dc.getGL().glUseProgram(0);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
		{
			return new ShaderRenderDelegate();
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
