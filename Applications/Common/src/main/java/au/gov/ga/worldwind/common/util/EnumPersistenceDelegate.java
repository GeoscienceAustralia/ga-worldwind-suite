package au.gov.ga.worldwind.common.util;

import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.IntrospectionException;
import java.beans.Introspector;

/**
 * Helper class for making enum classes persistent when serializing classes
 * containing an enum property. Each enum must be registered with the class
 * using the {@link EnumPersistenceDelegate#installFor(Enum[])} method.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EnumPersistenceDelegate extends DefaultPersistenceDelegate
{
	private static EnumPersistenceDelegate INSTANCE = new EnumPersistenceDelegate();

	/**
	 * Install the {@link EnumPersistenceDelegate} for each of the provided enum
	 * values.
	 * 
	 * @param values
	 */
	public static void installFor(Enum<?>[] values)
	{
		Class<?> declaringClass = values[0].getDeclaringClass();
		installFor(declaringClass);

		for (Enum<?> e : values)
			if (e.getClass() != declaringClass)
				installFor(e.getClass());
	}

	protected static void installFor(Class<?> enumClass)
	{
		try
		{
			BeanInfo info = Introspector.getBeanInfo(enumClass);
			info.getBeanDescriptor().setValue("persistenceDelegate", INSTANCE);
		}
		catch (IntrospectionException exception)
		{
			throw new RuntimeException("Unable to persist enumerated type " + enumClass, exception);
		}
	}

	@Override
	protected Expression instantiate(Object oldInstance, Encoder out)
	{
		Enum<?> e = (Enum<?>) oldInstance;
		return new Expression(e.getDeclaringClass(), "valueOf", new Object[] { e.name() });
	}

	@Override
	protected boolean mutatesTo(Object oldInstance, Object newInstance)
	{
		return oldInstance == newInstance;
	}
}
