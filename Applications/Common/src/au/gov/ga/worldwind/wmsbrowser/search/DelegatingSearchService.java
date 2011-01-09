package au.gov.ga.worldwind.wmsbrowser.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import au.gov.ga.worldwind.common.util.LenientReadWriteLock;

/**
 * A base class for search services that wish to delegate to a list of 
 * search services in some way.
 */
public abstract class DelegatingSearchService implements WmsServerSearchService
{
	private List<WmsServerSearchService> searchServices = new ArrayList<WmsServerSearchService>();
	private ReadWriteLock servicesLock = new LenientReadWriteLock();
	
	protected void lockServices()
	{
		servicesLock.readLock().lock();
	}
	
	protected void unlockServices()
	{
		servicesLock.readLock().unlock();
	}
	
	protected List<WmsServerSearchService> getServices()
	{
		return searchServices;
	}
	
	public void addService(WmsServerSearchService service)
	{
		try
		{
			servicesLock.writeLock().lock();
			
			if (searchServices.contains(service))
			{
				return;
			}
			searchServices.add(service);
		}
		finally
		{
			servicesLock.writeLock().unlock();
		}
	}
	
	public void addServices(Collection<WmsServerSearchService> services)
	{
		try
		{
			servicesLock.writeLock().lock();
			searchServices.addAll(services);
		}
		finally
		{
			servicesLock.writeLock().unlock();
		}
	}
	
	public void setServices(Collection<WmsServerSearchService> services)
	{
		try
		{
			servicesLock.writeLock().lock();
			searchServices.clear();
			searchServices.addAll(services);
		}
		finally
		{
			servicesLock.writeLock().unlock();
		}
	}
}
