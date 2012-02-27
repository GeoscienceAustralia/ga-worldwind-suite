/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
