package au.gov.ga.worldwind.animator.application;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReadWriteLock;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationWriter;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationWriter;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.common.util.LenientReadWriteLock;

/**
 * A class that auto-saves animations according to user preferences.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AutoSaver implements ChangeOfAnimationListener
{
	private Animator targetApplication;
	
	private Animation currentAnimation;
	private ReadWriteLock animationLock = new LenientReadWriteLock();
	
	private Timer timer;
	private AnimationWriter writer = new XmlAnimationWriter();
	
	public AutoSaver(Animator targetApplication)
	{
		this.targetApplication = targetApplication;
	}
	
	@Override
	public void updateAnimation(Animation newAnimation)
	{
		try
		{
			animationLock.writeLock().lock();
			this.currentAnimation = newAnimation;
			activate();
		}
		finally
		{
			animationLock.writeLock().unlock();
		}
	}
	
	/**
	 * Begin performing auto-saves as per user preferences
	 */
	public void activate()
	{
		if (!Settings.get().isAutoSaveEnabled())
		{
			return;
		}
		restartTimer();
	}
	
	private void restartTimer()
	{
		if (timer != null)
		{
			timer.cancel();
			timer.purge();
		}
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run()
			{
				if (currentAnimation == null)
				{
					currentAnimation = targetApplication.getCurrentAnimation();
					if (currentAnimation == null)
					{
						return;
					}
				}
				saveCurrentAnimation();
				cleanUpExpiredSaves();
			}

		}, 
		Settings.get().getSaveIntervalInMilliseconds(), 
		Settings.get().getSaveIntervalInMilliseconds());
	}
	
	private void saveCurrentAnimation()
	{
		try
		{
			File saveFile = createAutoSaveFile();
			
			animationLock.readLock().lock();
			writer.writeAnimation(saveFile, currentAnimation);
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
		}
		finally
		{
			animationLock.readLock().unlock();
		}
		
	}
	
	private File createAutoSaveFile() throws IOException
	{
		File animationFile = targetApplication.getAnimationFile();
		if (animationFile == null)
		{
			animationFile = File.createTempFile("animation", ".xml");
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = dateFormat.format(new Date());
		
		return new File(animationFile.getParentFile(), animationFile.getName() + "." + timestamp);
	}

	private void cleanUpExpiredSaves()
	{
		if (targetApplication.getAnimationFile() == null)
		{
			return;
		}
		
		File saveDirectory = targetApplication.getAnimationFile().getParentFile();
		
		File[] candidateFiles = saveDirectory.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				String masterFileName = targetApplication.getAnimationFile().getName();
				return name.startsWith(masterFileName) && name.length() > masterFileName.length(); // Remove the master file name
			}
		});
		
		if (candidateFiles.length <= Settings.get().getMaxNumberOfAutoSaves())
		{
			return; //No files to remove
		}
		
		// Sort the array in ascending order of creation timestamp
		Arrays.sort(candidateFiles, new Comparator<File>(){
			@Override
			public int compare(File o1, File o2)
			{
				return (int)Math.signum(o1.lastModified() - o2.lastModified());
			}
		});
		
		// Remove expired files
		for (int i = 0; i < candidateFiles.length - Settings.get().getMaxNumberOfAutoSaves(); i++)
		{
			candidateFiles[i].delete();
			candidateFiles[i].deleteOnExit();
		}
	}
	
}
