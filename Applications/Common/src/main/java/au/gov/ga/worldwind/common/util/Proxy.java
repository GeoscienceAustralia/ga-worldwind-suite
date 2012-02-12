package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import java.io.Serializable;

/**
 * Helper class that stores proxy information, and sets up the Java proxy.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Proxy implements Serializable
{
	public enum ProxyType implements Serializable
	{
		HTTP("HTTP", "Proxy.Type.Http"),
		SOCKS("SOCKS", "Proxy.Type.SOCKS");

		private String pretty;
		private String type;

		ProxyType(String pretty, String type)
		{
			this.pretty = pretty;
			this.type = type;
		}

		@Override
		public String toString()
		{
			return pretty;
		}

		public String getType()
		{
			return type;
		}

		public static ProxyType fromString(String proxyType)
		{
			if (proxyType != null)
			{
				for (ProxyType type : ProxyType.values())
				{
					if (type.pretty.equalsIgnoreCase(proxyType))
						return type;
					if (type.type.equalsIgnoreCase(proxyType))
						return type;
				}
			}
			return null;
		}

		static
		{
			EnumPersistenceDelegate.installFor(values());
		}
	}

	private boolean enabled = true;
	private boolean useSystem = true;
	private String host = null;
	private int port = 80;
	private ProxyType type = ProxyType.HTTP;
	private String nonProxyHosts = "localhost";

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isUseSystem()
	{
		return useSystem;
	}

	public void setUseSystem(boolean useSystem)
	{
		this.useSystem = useSystem;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public ProxyType getType()
	{
		return type;
	}

	public void setType(ProxyType type)
	{
		this.type = type;
	}

	public String getNonProxyHosts()
	{
		return nonProxyHosts;
	}

	public void setNonProxyHosts(String nonProxyHosts)
	{
		this.nonProxyHosts = nonProxyHosts;
	}

	public void set()
	{
		Configuration.removeKey(AVKey.URL_PROXY_HOST);
		System.clearProperty("http.proxyHost");
		System.clearProperty("socksProxyHost");
		System.clearProperty("java.net.useSystemProxies");

		if (enabled)
		{
			if (useSystem)
			{
				System.setProperty("java.net.useSystemProxies", "true");
			}
			else if (host != null)
			{
				Configuration.setValue(AVKey.URL_PROXY_HOST, host);
				Configuration.setValue(AVKey.URL_PROXY_PORT, port);
				Configuration.setValue(AVKey.URL_PROXY_TYPE, type.getType());

				if (type == ProxyType.HTTP)
				{
					System.setProperty("http.proxyHost", host);
					System.setProperty("http.proxyPort", String.valueOf(port));
					System.setProperty("http.nonProxyHosts", nonProxyHosts);
				}
				else
				{
					System.setProperty("socksProxyHost", host);
					System.setProperty("socksProxyPort", String.valueOf(port));
				}
			}
		}
	}
}
