package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.layers.Bounded;
import au.gov.ga.worldwind.common.layers.Wireframeable;

import com.sun.opengl.util.BufferUtil;

/**
 * The FastShape class is a representation of a piece of geometry. It is useful
 * for meshes or points or lines with a large number of vertices, as the vertex
 * positions aren't updated every frame (instead they are updated in a vertex
 * updater thread).
 * 
 * @author Michael de Hoog
 */
public class FastShape implements Renderable, Cacheable, Bounded, Wireframeable
{
	//TODO add VBO support

	protected final ReadWriteLock frontLock = new ReentrantReadWriteLock();

	protected List<Position> positions;

	protected FloatBuffer colorBuffer;
	protected int colorBufferElementSize = 3;
	protected IntBuffer indices;
	protected int mode;
	protected String name = "Shape";

	protected DoubleBuffer vertexBuffer;
	protected DoubleBuffer modVertexBuffer;
	protected DoubleBuffer normalBuffer;
	protected DoubleBuffer modNormalBuffer;
	protected Sphere boundingSphere;
	protected Sphere modBoundingSphere;
	protected Sector sector;

	protected IntBuffer sortedIndices;
	protected IntBuffer modSortedIndices;

	protected Color color = Color.white;
	protected double opacity = 1;
	protected boolean followTerrain = false;

	protected double lastVerticalExaggeration = -1;
	protected Globe lastGlobe = null;
	protected boolean verticesDirty = true;
	protected Vec4 lastEyePoint = null;

	protected double elevation = 0d;
	protected boolean calculateNormals = false;
	protected boolean fogEnabled = false;
	protected boolean wireframe = false;
	protected boolean lighted = false;
	protected boolean sortTransparentTriangles = true;
	protected boolean backfaceCulling = false;
	protected boolean enabled = true;

	public FastShape(List<Position> positions, int mode)
	{
		this(positions, null, mode);
	}

	public FastShape(List<Position> positions, IntBuffer indices, int mode)
	{
		setPositions(positions);
		setIndices(indices);
		setMode(mode);
	}

	@Override
	public void render(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		double alpha = getOpacity();
		if (dc.getCurrentLayer() != null)
		{
			alpha *= dc.getCurrentLayer().getOpacity();
		}

		recalculateIfRequired(dc, alpha);

		frontLock.readLock().lock();
		try
		{
			if (vertexBuffer == null || vertexBuffer.limit() <= 0)
				return;

			if (boundingSphere == null || !dc.getView().getFrustumInModelCoordinates().intersects(boundingSphere))
				return;

			GL gl = dc.getGL();
			OGLStackHandler stack = new OGLStackHandler();

			try
			{
				boolean willUseSortedIndices = sortTransparentTriangles && alpha < 1.0 && sortedIndices != null;

				int attributesToPush = GL.GL_CURRENT_BIT;
				if (!fogEnabled)
				{
					attributesToPush |= GL.GL_FOG_BIT;
				}
				if (wireframe || backfaceCulling)
				{
					attributesToPush |= GL.GL_POLYGON_BIT;
				}
				if (lighted)
				{
					attributesToPush |= GL.GL_LIGHTING_BIT;
				}
				if (willUseSortedIndices)
				{
					attributesToPush |= GL.GL_DEPTH_BUFFER_BIT;
				}

				stack.pushAttrib(gl, attributesToPush);
				stack.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);
				Vec4 referenceCenter = boundingSphere.getCenter();
				dc.getView().pushReferenceCenter(dc, referenceCenter);

				if (!fogEnabled)
				{
					gl.glDisable(GL.GL_FOG);
				}
				if (wireframe)
				{
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
				}
				if (backfaceCulling)
				{
					gl.glEnable(GL.GL_CULL_FACE);
					gl.glCullFace(GL.GL_BACK);
				}
				if (lighted)
				{
					Vec4 cameraPosition = dc.getView().getEyePoint();
					Vec4 lightPos = cameraPosition.subtract3(referenceCenter);
					float[] lightPosition = { (float) lightPos.x, (float) lightPos.y, (float) lightPos.z, 1.0f };
					float[] lightAmbient = { 0.0f, 0.0f, 0.0f, 1.0f };
					float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
					float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };
					float[] modelAmbient = { 0.3f, 0.3f, 0.3f, 1.0f };
					gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, modelAmbient, 0);
					gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
					gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
					gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
					gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);
					gl.glDisable(GL.GL_LIGHT0);
					gl.glEnable(GL.GL_LIGHT1);
					gl.glEnable(GL.GL_LIGHTING);

					float[] materialColor =
							{ color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (float) alpha };
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, materialColor, 0);
					gl.glEnable(GL.GL_COLOR_MATERIAL);
				}

				if (colorBuffer != null)
				{
					gl.glEnableClientState(GL.GL_COLOR_ARRAY);
					gl.glColorPointer(colorBufferElementSize, GL.GL_FLOAT, 0, colorBuffer.rewind());
				}

				gl.glColor4d(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d, alpha);
				if (alpha < 1.0)
				{
					gl.glEnable(GL.GL_BLEND);
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				}

				gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
				gl.glVertexPointer(3, GL.GL_DOUBLE, 0, vertexBuffer.rewind());

				if (willCalculateNormals())
				{
					gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
					gl.glNormalPointer(GL.GL_DOUBLE, 0, normalBuffer.rewind());
				}

				if (willUseSortedIndices)
				{
					gl.glDepthMask(false);
					gl.glDrawElements(mode, sortedIndices.limit(), GL.GL_UNSIGNED_INT, sortedIndices.rewind());
				}
				else if (indices != null)
				{
					gl.glDrawElements(mode, indices.limit(), GL.GL_UNSIGNED_INT, indices.rewind());
				}
				else
				{
					gl.glDrawArrays(mode, 0, vertexBuffer.limit() / 3);
				}
			}
			finally
			{
				stack.pop(gl);
				dc.getView().popReferenceCenter(dc);
			}
		}
		finally
		{
			frontLock.readLock().unlock();
		}
	}

	protected void recalculateIfRequired(DrawContext dc, double alpha)
	{
		boolean recalculateVertices = followTerrain || lastVerticalExaggeration != dc.getVerticalExaggeration();
		boolean recalculateVerticesNow = verticesDirty || lastGlobe != dc.getGlobe();
		if (recalculateVertices || recalculateVerticesNow)
		{
			lastVerticalExaggeration = dc.getVerticalExaggeration();
			lastGlobe = dc.getGlobe();
			recalculateVertices(dc, recalculateVerticesNow);
			verticesDirty = false;
		}

		Vec4 eyePoint = dc.getView().getEyePoint();
		boolean recalculateIndices =
				sortTransparentTriangles && mode == GL.GL_TRIANGLES && alpha < 1.0 && !eyePoint.equals(lastEyePoint);
		if (recalculateIndices)
		{
			lastEyePoint = eyePoint;
			resortIndices(dc, lastEyePoint);
		}
	}

	protected synchronized void recalculateVertices(final DrawContext dc, boolean runNow)
	{
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				frontLock.readLock().lock();
				try
				{
					int size = positions.size() * 3;
					if (modVertexBuffer == null || modVertexBuffer.limit() != size)
					{
						modVertexBuffer = BufferUtil.newDoubleBuffer(size);
					}
					if (willCalculateNormals() && (modNormalBuffer == null || modNormalBuffer.limit() != size))
					{
						modNormalBuffer = BufferUtil.newDoubleBuffer(size);
					}

					calculateVertices(dc);
					calculateNormals();
				}
				finally
				{
					frontLock.readLock().unlock();
				}

				frontLock.writeLock().lock();
				try
				{
					DoubleBuffer temp = vertexBuffer;
					vertexBuffer = modVertexBuffer;
					modVertexBuffer = temp;
					Sphere tempe = boundingSphere;
					boundingSphere = modBoundingSphere;
					modBoundingSphere = tempe;
					if (willCalculateNormals())
					{
						temp = normalBuffer;
						normalBuffer = modNormalBuffer;
						modNormalBuffer = temp;
					}
				}
				finally
				{
					frontLock.writeLock().unlock();
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

	protected void calculateVertices(DrawContext dc)
	{
		modVertexBuffer.rewind();
		Globe globe = dc.getGlobe();
		for (LatLon position : positions)
		{
			double elevation = this.elevation;
			if (followTerrain)
			{
				elevation += globe.getElevation(position.getLatitude(), position.getLongitude());
			}
			if (position instanceof Position)
			{
				elevation += ((Position) position).getElevation();
			}
			elevation *= dc.getVerticalExaggeration();
			elevation = Math.max(elevation, -dc.getGlobe().getMaximumRadius());
			Vec4 v = dc.getGlobe().computePointFromPosition(position.getLatitude(), position.getLongitude(), elevation);
			modVertexBuffer.put(v.x).put(v.y).put(v.z);
		}

		modVertexBuffer.rewind();
		BufferWrapper wrapper = new BufferWrapper.DoubleBufferWrapper(modVertexBuffer);
		modBoundingSphere = Sphere.createBoundingSphere(wrapper);

		modVertexBuffer.rewind();
		for (int i = 0; modVertexBuffer.remaining() >= 3; i += 3)
		{
			modVertexBuffer.put(i + 0, modVertexBuffer.get() - modBoundingSphere.getCenter().x);
			modVertexBuffer.put(i + 1, modVertexBuffer.get() - modBoundingSphere.getCenter().y);
			modVertexBuffer.put(i + 2, modVertexBuffer.get() - modBoundingSphere.getCenter().z);
		}
	}

	protected void calculateNormals()
	{
		if (willCalculateNormals())
		{
			int size = modNormalBuffer.limit() / 3;
			int[] count = new int[size];
			Vec4[] vertices = new Vec4[size];
			Vec4[] normals = new Vec4[size];

			int j = 0;
			modVertexBuffer.rewind();
			while (modVertexBuffer.hasRemaining())
			{
				vertices[j] = new Vec4(modVertexBuffer.get(), modVertexBuffer.get(), modVertexBuffer.get());
				normals[j] = new Vec4(0);
				j++;
			}

			boolean hasIndices = indices != null;
			int triangleCountBy3 = hasIndices ? indices.limit() : size;
			for (int i = 0; i < triangleCountBy3; i += 3)
			{
				//don't touch indices's position/mark, because it may currently be in use by OpenGL thread
				int index0 = hasIndices ? indices.get(i + 0) : i + 0;
				int index1 = hasIndices ? indices.get(i + 1) : i + 1;
				int index2 = hasIndices ? indices.get(i + 2) : i + 2;
				Vec4 v0 = vertices[index0];
				Vec4 v1 = vertices[index1];
				Vec4 v2 = vertices[index2];

				Vec4 e1 = v1.subtract3(v0);
				Vec4 e2 = v2.subtract3(v0);
				Vec4 N = e1.cross3(e2).normalize3(); // if N is 0, the triangle is degenerate

				if (N.getLength3() > 0)
				{
					normals[index0] = normals[index0].add3(N);
					normals[index1] = normals[index1].add3(N);
					normals[index2] = normals[index2].add3(N);

					count[index0]++;
					count[index1]++;
					count[index2]++;
				}
			}

			j = 0;
			modNormalBuffer.rewind();
			while (modNormalBuffer.hasRemaining())
			{
				int c = count[j] > 0 ? count[j] : 1; //prevent divide by zero
				modNormalBuffer.put(normals[j].x / c);
				modNormalBuffer.put(normals[j].y / c);
				modNormalBuffer.put(normals[j].z / c);
				j++;
			}
		}
	}

	protected synchronized void resortIndices(final DrawContext dc, final Vec4 eyePoint)
	{
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				frontLock.readLock().lock();
				try
				{
					int size = indices != null ? indices.limit() : vertexBuffer.limit() / 3;
					if (modSortedIndices == null || modSortedIndices.limit() != size)
					{
						modSortedIndices = BufferUtil.newIntBuffer(size);
					}

					sortIndices(dc, eyePoint);
				}
				finally
				{
					frontLock.readLock().unlock();
				}

				frontLock.writeLock().lock();
				try
				{
					IntBuffer temp = sortedIndices;
					sortedIndices = modSortedIndices;
					modSortedIndices = temp;
				}
				finally
				{
					frontLock.writeLock().unlock();
				}
			}
		};

		IndexUpdater.run(this, runnable);
	}

	protected void sortIndices(DrawContext dc, Vec4 eyePoint)
	{
		int size = vertexBuffer.limit() / 3;
		Vec4[] vertices = new Vec4[size];

		for (int i = 0; i < size; i++)
		{
			vertices[i] =
					new Vec4(vertexBuffer.get(i * 3 + 0), vertexBuffer.get(i * 3 + 1), vertexBuffer.get(i * 3 + 2));
		}

		boolean hasIndices = indices != null;
		int triangleCountBy3 = hasIndices ? indices.limit() : size;
		TriangleDistance[] distances = new TriangleDistance[triangleCountBy3 / 3];

		for (int i = 0; i < triangleCountBy3; i += 3)
		{
			int index0 = hasIndices ? indices.get(i + 0) : i + 0;
			int index1 = hasIndices ? indices.get(i + 1) : i + 1;
			int index2 = hasIndices ? indices.get(i + 2) : i + 2;
			Vec4 v0 = vertices[index0];
			Vec4 v1 = vertices[index1];
			Vec4 v2 = vertices[index2];
			double distance =
					v0.distanceToSquared3(eyePoint) + v1.distanceToSquared3(eyePoint) + v2.distanceToSquared3(eyePoint);
			distances[i / 3] = new TriangleDistance(distance, i);
		}

		Arrays.sort(distances);

		modSortedIndices.rewind();
		for (TriangleDistance distance : distances)
		{
			modSortedIndices.put(hasIndices ? indices.get(distance.triangleIndex + 0) : distance.triangleIndex + 0);
			modSortedIndices.put(hasIndices ? indices.get(distance.triangleIndex + 1) : distance.triangleIndex + 1);
			modSortedIndices.put(hasIndices ? indices.get(distance.triangleIndex + 2) : distance.triangleIndex + 2);
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		frontLock.writeLock().lock();
		try
		{
			this.color = color;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public FloatBuffer getColorBuffer()
	{
		return colorBuffer;
	}

	public void setColorBuffer(FloatBuffer colorBuffer)
	{
		frontLock.writeLock().lock();
		try
		{
			this.colorBuffer = colorBuffer;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public int getColorBufferElementSize()
	{
		return colorBufferElementSize;
	}

	public void setColorBufferElementSize(int colorBufferElementSize)
	{
		frontLock.writeLock().lock();
		try
		{
			this.colorBufferElementSize = colorBufferElementSize;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public double getOpacity()
	{
		return opacity;
	}

	public void setOpacity(double opacity)
	{
		frontLock.writeLock().lock();
		try
		{
			this.opacity = opacity;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public List<? extends LatLon> getPositions()
	{
		return positions;
	}

	public void setPositions(List<Position> positions)
	{
		frontLock.writeLock().lock();
		try
		{
			this.positions = positions;
			verticesDirty = true;

			sector = null;
			for (Position position : positions)
			{
				sector =
						sector != null ? sector.union(position.latitude, position.longitude) : new Sector(
								position.latitude, position.latitude, position.longitude, position.longitude);
			}
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public IntBuffer getIndices()
	{
		return indices;
	}

	public void setIndices(IntBuffer indices)
	{
		frontLock.writeLock().lock();
		try
		{
			this.indices = indices;
			verticesDirty = true;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public boolean isFollowTerrain()
	{
		return followTerrain;
	}

	public void setFollowTerrain(boolean followTerrain)
	{
		frontLock.writeLock().lock();
		try
		{
			this.followTerrain = followTerrain;
			verticesDirty = true;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		frontLock.writeLock().lock();
		try
		{
			this.mode = mode;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public double getElevation()
	{
		return elevation;
	}

	public void setElevation(double elevation)
	{
		frontLock.writeLock().lock();
		try
		{
			this.elevation = elevation;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public boolean isCalculateNormals()
	{
		return calculateNormals;
	}

	public void setCalculateNormals(boolean calculateNormals)
	{
		frontLock.writeLock().lock();
		try
		{
			this.calculateNormals = calculateNormals;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	protected boolean willCalculateNormals()
	{
		return isCalculateNormals() && getMode() == GL.GL_TRIANGLES;
	}

	public boolean isFogEnabled()
	{
		return fogEnabled;
	}

	public void setFogEnabled(boolean fogEnabled)
	{
		frontLock.writeLock().lock();
		try
		{
			this.fogEnabled = fogEnabled;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	@Override
	public boolean isWireframe()
	{
		return wireframe;
	}

	@Override
	public void setWireframe(boolean wireframe)
	{
		frontLock.writeLock().lock();
		try
		{
			this.wireframe = wireframe;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public boolean isBackfaceCulling()
	{
		return backfaceCulling;
	}

	public void setBackfaceCulling(boolean backfaceCulling)
	{
		frontLock.writeLock().lock();
		try
		{
			this.backfaceCulling = backfaceCulling;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public boolean isLighted()
	{
		return lighted;
	}

	public void setLighted(boolean lighted)
	{
		frontLock.writeLock().lock();
		try
		{
			this.lighted = lighted;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	@Override
	public long getSizeInBytes()
	{
		//very approximate, measured by checking JVM memory usage over many object creations
		return 500 + 80 * getPositions().size();
	}

	/**
	 * @return The extent of this shape. This is calculated by
	 *         {@link FastShape#render(DrawContext)}, so don't use this for
	 *         frustum culling.
	 */
	public Extent getExtent()
	{
		frontLock.readLock().lock();
		try
		{
			return boundingSphere;
		}
		finally
		{
			frontLock.readLock().unlock();
		}
	}

	@Override
	public Sector getSector()
	{
		frontLock.readLock().lock();
		try
		{
			return sector;
		}
		finally
		{
			frontLock.readLock().unlock();
		}
	}

	public static FloatBuffer color4ToFloatBuffer(List<Color> colors)
	{
		FloatBuffer buffer = BufferUtil.newFloatBuffer(colors.size() * 4);
		return color4ToFloatBuffer(colors, buffer);
	}

	public static FloatBuffer color4ToFloatBuffer(List<Color> colors, FloatBuffer buffer)
	{
		for (Color color : colors)
		{
			buffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f)
					.put(color.getAlpha() / 255f);
		}
		buffer.rewind();
		return buffer;
	}

	public static FloatBuffer color3ToFloatBuffer(List<Color> colors)
	{
		FloatBuffer buffer = BufferUtil.newFloatBuffer(colors.size() * 3);
		return color3ToFloatBuffer(colors, buffer);
	}

	public static FloatBuffer color3ToFloatBuffer(List<Color> colors, FloatBuffer buffer)
	{
		for (Color color : colors)
		{
			buffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f);
		}
		buffer.rewind();
		return buffer;
	}

	protected static class TriangleDistance implements Comparable<TriangleDistance>
	{
		public final double distance;
		public final int triangleIndex;

		public TriangleDistance(double distance, int triangleIndex)
		{
			this.distance = distance;
			this.triangleIndex = triangleIndex;
		}

		@Override
		public int compareTo(TriangleDistance o)
		{
			return -Double.compare(distance, o.distance);
		}
	}

	protected static class OwnerRunnable
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
			return super.equals(obj);
		}
	}

	protected static class VertexUpdater
	{
		private static BlockingQueue<OwnerRunnable> queue = new LinkedBlockingQueue<OwnerRunnable>();
		private static Set<OwnerRunnable> set = Collections.synchronizedSet(new HashSet<OwnerRunnable>());
		private static final int THREAD_COUNT = 1;

		static
		{
			for (int i = 0; i < THREAD_COUNT; i++)
			{
				Thread thread = new Thread(new Runnable()
				{
					@Override
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
	}

	protected static class IndexUpdater
	{
		private static BlockingQueue<OwnerRunnable> queue = new LinkedBlockingQueue<OwnerRunnable>();
		private static Set<OwnerRunnable> set = Collections.synchronizedSet(new HashSet<OwnerRunnable>());
		private static final int THREAD_COUNT = 1;

		static
		{
			for (int i = 0; i < THREAD_COUNT; i++)
			{
				Thread thread = new Thread(new Runnable()
				{
					@Override
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
	}
}
