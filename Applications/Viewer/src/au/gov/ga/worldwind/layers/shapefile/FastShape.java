package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

public class FastShape implements Renderable, Cacheable
{
	protected List<? extends LatLon> positions;
	protected Object positionLock = new Object();

	protected DoubleBuffer colorBuffer;
	protected IntBuffer[] indices;
	protected int mode;

	protected DoubleBuffer vertexBuffer;
	protected DoubleBuffer modVertexBuffer;
	protected Object vertexLock = new Object();

	protected Color color = Color.white;
	protected double opacity = 1;
	protected boolean followTerrain = false;

	protected double lastVerticalExaggeration = -1;
	protected Globe lastGlobe = null;
	protected boolean verticesDirty = true;

	public FastShape(List<? extends LatLon> positions, int mode)
	{
		this(positions, null, null, mode);
	}

	public FastShape(List<? extends LatLon> positions, IntBuffer[] indices, int mode)
	{
		this(positions, indices, null, mode);
	}

	public FastShape(List<? extends LatLon> positions, DoubleBuffer colorBuffer, int mode)
	{
		this(positions, null, colorBuffer, mode);
	}

	public FastShape(List<? extends LatLon> positions, IntBuffer[] indices,
			DoubleBuffer colorBuffer, int mode)
	{
		setPositions(positions);
		setColorBuffer(colorBuffer);
		setIndices(indices);
		setMode(mode);
	}

	public void render(DrawContext dc)
	{
		boolean recalculate =
				followTerrain || lastVerticalExaggeration != dc.getVerticalExaggeration();
		boolean recalculateNow = verticesDirty || lastGlobe != dc.getGlobe();
		if (recalculate || recalculateNow)
		{
			lastVerticalExaggeration = dc.getVerticalExaggeration();
			lastGlobe = dc.getGlobe();
			recalculateVertices(dc, recalculateNow);
			verticesDirty = false;
		}

		GL gl = dc.getGL();

		int push = GL.GL_CLIENT_VERTEX_ARRAY_BIT;
		if (colorBuffer != null)
		{
			push |= GL.GL_COLOR_BUFFER_BIT;
		}
		if (getOpacity() < 1.0)
		{
			push |= GL.GL_CURRENT_BIT;
		}
		gl.glPushClientAttrib(push);

		if (colorBuffer != null)
		{
			gl.glEnableClientState(GL.GL_COLOR_ARRAY);
			gl.glColorPointer(4, GL.GL_DOUBLE, 0, colorBuffer.rewind());
		}

		double alpha = getOpacity();
		if (dc.getCurrentLayer() != null)
		{
			alpha *= dc.getCurrentLayer().getOpacity();
		}
		gl.glColor4d(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d, alpha);
		if (alpha < 1.0)
		{
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		}

		gl.glPointSize(2f);

		synchronized (vertexLock)
		{
			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			gl.glVertexPointer(3, GL.GL_DOUBLE, 0, vertexBuffer.rewind());

			if (indices == null)
			{
				gl.glDrawArrays(mode, 0, vertexBuffer.limit() / 3);
			}
			else
			{
				for (IntBuffer ind : indices)
				{
					gl.glDrawElements(mode, ind.limit(), GL.GL_UNSIGNED_INT, ind.rewind());
				}
			}
		}

		gl.glPointSize(1f);

		gl.glColor4d(1, 1, 1, 1);
		gl.glPopClientAttrib();
	}

	//TODO change back to protected
	public synchronized void recalculateVertices(final DrawContext dc, boolean runNow)
	{
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				calculateVertices(dc);
				synchronized (vertexLock)
				{
					DoubleBuffer temp = vertexBuffer;
					vertexBuffer = modVertexBuffer;
					modVertexBuffer = temp;
				}
			}
		};

		if (runNow)
		{
			runnable.run();
		}
		else
		{
			VertexUpdater.run(this, runnable);
		}
	}

	public DoubleBuffer calculateVertices(DrawContext dc)
	{
		synchronized (positionLock)
		{
			modVertexBuffer.rewind();
			Globe globe = dc.getGlobe();
			for (LatLon position : positions)
			{
				double elevation = 0;
				if (followTerrain)
				{
					elevation = globe.getElevation(position.getLatitude(), position.getLongitude());
				}
				if (position instanceof Position)
				{
					elevation += ((Position) position).getElevation();
				}
				elevation *= dc.getVerticalExaggeration();
				elevation = Math.max(elevation, -dc.getGlobe().getMaximumRadius());
				Vec4 v =
						dc.getGlobe().computePointFromPosition(position.getLatitude(),
								position.getLongitude(), elevation);
				modVertexBuffer.put(v.x).put(v.y).put(v.z);
			}
			return modVertexBuffer;
		}
	}

	public DoubleBuffer getColorBuffer()
	{
		return colorBuffer;
	}

	public void setColorBuffer(DoubleBuffer colorBuffer)
	{
		this.colorBuffer = colorBuffer;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public double getOpacity()
	{
		return opacity;
	}

	public void setOpacity(double opacity)
	{
		this.opacity = opacity;
	}

	public List<? extends LatLon> getPositions()
	{
		return positions;
	}

	public void setPositions(List<? extends LatLon> positions)
	{
		synchronized (vertexLock)
		{
			synchronized (positionLock)
			{
				this.positions = positions;
				vertexBuffer = BufferUtil.newDoubleBuffer(positions.size() * 3);
				modVertexBuffer = BufferUtil.newDoubleBuffer(positions.size() * 3);
				verticesDirty = true;
			}
		}
	}

	public IntBuffer[] getIndices()
	{
		return indices;
	}

	public void setIndices(IntBuffer[] indices)
	{
		this.indices = indices;
	}

	public boolean isFollowTerrain()
	{
		return followTerrain;
	}

	public void setFollowTerrain(boolean followTerrain)
	{
		synchronized (positionLock)
		{
			this.followTerrain = followTerrain;
		}
	}

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	private static class VertexUpdater
	{
		private static BlockingQueue<OwnerRunnable> queue =
				new LinkedBlockingQueue<OwnerRunnable>();
		private static Set<OwnerRunnable> set =
				Collections.synchronizedSet(new HashSet<OwnerRunnable>());
		private static final int THREAD_COUNT = 1;

		static
		{
			for (int i = 0; i < THREAD_COUNT; i++)
			{
				Thread thread = new Thread(new Runnable()
				{
					public void run()
					{
						while (true)
						{
							try
							{
								OwnerRunnable or = queue.take();
								or.runnable.run();
								set.remove(or);
							}
							catch (Throwable t)
							{
								t.printStackTrace();
							}
						}
					}
				});
				thread.setName(VertexUpdater.class.getName());
				thread.setDaemon(true);
				thread.start();
			}
		}

		public synchronized static void run(Object owner, Runnable runnable)
		{
			OwnerRunnable or = new OwnerRunnable(owner, runnable);
			if (!set.contains(or))
			{
				set.add(or);
				queue.add(or);
			}
		}

		private static class OwnerRunnable
		{
			public final Object owner;
			public final Runnable runnable;

			public OwnerRunnable(Object owner, Runnable runnable)
			{
				this.owner = owner;
				this.runnable = runnable;
			}

			@Override
			public int hashCode()
			{
				return owner.hashCode();
			}

			@Override
			public boolean equals(Object obj)
			{
				if (obj == null)
					return false;
				if (obj.equals(owner))
					return true;
				return obj.equals(this);
			}
		}
	}

	public static DoubleBuffer colorToDoubleBuffer(List<Color> colors)
	{
		DoubleBuffer cb = BufferUtil.newDoubleBuffer(colors.size() * 4);
		for (Color color : colors)
		{
			cb.put(color.getRed() / 255d).put(color.getGreen() / 255d).put(color.getBlue() / 255d)
					.put(color.getAlpha() / 255d);
		}
		cb.rewind();
		return cb;
	}

	@Override
	public long getSizeInBytes()
	{
		//very approximate, measured by checking JVM memory usage over many object creations
		return 500 + 80 * getPositions().size();
	}
}
