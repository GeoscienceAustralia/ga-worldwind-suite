package au.gov.ga.worldwind.animator.layers.accessible;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;

/**
 * {@link BasicTiledImageLayer} subclass that provides access to its internal
 * filelock object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AccessibleBasicTiledImageLayer extends BasicTiledImageLayer
{
	private final FileLockAccessor fileLockAccessor = new FileLockAccessor(this);

	public AccessibleBasicTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	public AccessibleBasicTiledImageLayer(AVList params)
	{
		super(params);
	}

	public AccessibleBasicTiledImageLayer(String stateInXml)
	{
		super(stateInXml);
	}

	protected Object getFileLock()
	{
		return fileLockAccessor.getFileLock();
	}

	protected static class FileLockAccessor extends DownloadPostProcessor
	{
		public FileLockAccessor(BasicTiledImageLayer layer)
		{
			super(null, layer);
		}

		@Override
		public Object getFileLock()
		{
			return super.getFileLock();
		}
	}
}
