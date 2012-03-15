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
package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * A base implementation of the {@link AnimationEvent} interface.
 * <p/>
 * In most cases you should use a sub-class to create specialised events for a given
 * animation object class (e.g. key frames, parameter values etc.).
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimationEventImpl implements AnimationEvent
{

	private AnimationEvent cause;
	
	private Type type;
	
	private Object owner;
	
	private Object value;
	
	/**
	 * @param owner Mandatory.
	 * @param eventType Optional. If not provided, the <code>type</code> of the provided <code>cause</code> will be used.
	 * @param cause Optional. If not provided, a <code>type</code> must be.
	 */
	public AnimationEventImpl(Object owner, Type eventType, AnimationEvent cause, Object value)
	{
		Validate.notNull(owner, "An owner must be provided");
		Validate.isTrue(eventType != null || cause != null, "If no cause is provided, a type must be.");
		
		this.owner = owner;
		this.type = eventType;
		this.cause = cause;
		this.value = value;
		
		if (this.type == null && this.cause != null)
		{
			this.type = cause.getType();
		}
	}
	
	@Override
	public AnimationEvent getCause()
	{
		return cause;
	}

	@Override
	public AnimationEvent getCauseOfClass(Class<? extends AnimationEvent> clazz)
	{
		if (clazz.isAssignableFrom(getClass()))
		{
			return this;
		}
		if (cause == null)
		{
			return null;
		}
		return cause.getCauseOfClass(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectInChainOfType(Class<T> clazz)
	{
		if (clazz.isAssignableFrom(getClass()))
		{
			return (T)this;
		}
		if (owner != null && clazz.isAssignableFrom(owner.getClass()))
		{
			return (T)owner;
		}
		if (value != null && clazz.isAssignableFrom(value.getClass()))
		{
			return (T)value;
		}
		if (cause != null)
		{
			return cause.getObjectInChainOfType(clazz);
		}
		return null;
	}
	
	@Override
	public boolean hasOwnerInChainOfType(Class<?> clazz)
	{
		if (owner.getClass().isAssignableFrom(clazz))
		{
			return true;
		}
		if (cause == null)
		{
			return false;
		}
		return cause.hasOwnerInChainOfType(clazz);
	}
	
	@Override
	public AnimationEvent getRootCause()
	{
		if (cause == null)
		{
			return this;
		}
		return cause.getRootCause();
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	public Object getOwner()
	{
		return owner;
	}
	
	@Override
	public Object getValue()
	{
		return value;
	}
	
	@Override
	public boolean isOfType(Type type)
	{
		return this.type == type;
	}
	
	@Override
	public String toString()
	{
		return "Event{" + getClass().getSimpleName() + ", " + type + ", " + owner + ", " + value + "}\n\t Caused by: " + cause + ""; 
	}

}
