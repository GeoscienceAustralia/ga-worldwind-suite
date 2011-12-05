package au.gov.ga.worldwind.common.util;

import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getProxyTestUrlKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;

import au.gov.ga.worldwind.common.downloader.Downloader;

public class Proxy
{
	public enum ProxyType implements Serializable
	{
		HTTP("HTTP", "Proxy.Type.Http"), SOCKS("SOCKS", "Proxy.Type.SOCKS");

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

	public boolean test()
	{
		String oldhttpHost = System.getProperty("http.proxyHost");
		String oldHttpPort = System.getProperty("http.proxyPort");
		String oldHttpNonProxyHosts = System.getProperty("http.nonProxyHosts");
		String oldSocksHost = System.getProperty("socksProxyHost");
		String oldSocksPort = System.getProperty("socksProxyPort");
		String oldConfigurationHost = Configuration.getStringValue(AVKey.URL_PROXY_HOST);
		String oldConfigurationPort = Configuration.getStringValue(AVKey.URL_PROXY_PORT);
		String oldConfigurationType = Configuration.getStringValue(AVKey.URL_PROXY_TYPE);

		try
		{
			//set the proxy values
			set();

			if (enabled)
			{
				try
				{
					URL url = new URL(getMessage(getProxyTestUrlKey()));
					Downloader.downloadImmediately(url, false, true);
				}
				catch (Exception e)
				{
					//proxy was wrong, so reset to default and disable
					return false;
				}
			}
			else
			{
				Configuration.removeKey(AVKey.URL_PROXY_HOST);
				System.clearProperty("http.proxyHost");
				System.clearProperty("socksProxyHost");
			}

			return true;
		}
		finally
		{
			//restore old values
			System.setProperty("http.proxyHost", oldhttpHost);
			System.setProperty("http.proxyPort", oldHttpPort);
			System.setProperty("http.nonProxyHosts", oldHttpNonProxyHosts);
			System.setProperty("socksProxyHost", oldSocksHost);
			System.setProperty("socksProxyPort", oldSocksPort);
			Configuration.setValue(AVKey.URL_PROXY_HOST, oldConfigurationHost);
			Configuration.setValue(AVKey.URL_PROXY_PORT, oldConfigurationPort);
			Configuration.setValue(AVKey.URL_PROXY_TYPE, oldConfigurationType);
		}
	}

	public void set()
	{
		if (enabled)
		{
			Configuration.setValue(AVKey.URL_PROXY_HOST, host);
			Configuration.setValue(AVKey.URL_PROXY_PORT, port);
			Configuration.setValue(AVKey.URL_PROXY_TYPE, type.getType());

			if (type == ProxyType.HTTP)
			{
				System.setProperty("http.proxyHost", host);
				System.setProperty("http.proxyPort", String.valueOf(port));
				System.setProperty("http.nonProxyHosts", nonProxyHosts);
				System.clearProperty("socksProxyHost");
			}
			else
			{
				System.clearProperty("http.proxyHost");
				System.setProperty("socksProxyHost", host);
				System.setProperty("socksProxyPort", String.valueOf(port));
			}
		}
		else
		{
			Configuration.removeKey(AVKey.URL_PROXY_HOST);
			System.clearProperty("http.proxyHost");
			System.clearProperty("socksProxyHost");
		}
	}

	public static Proxy guess()
	{
		try
		{
			String hostname = InetAddress.getLocalHost().getCanonicalHostName();
			int indexOfDot = hostname.indexOf('.');
			//if the canonical host name has a dot, we could possibly assume it is part of a domain
			//eg: PC-00000.agso.gov.au is part of domain agso.gov.au:
			if (indexOfDot >= 0)
			{
				Proxy proxy = new Proxy();
				proxy.setEnabled(true);
				
				//example: change PC-00000.agso.gov.au to proxy.agso.gov.au:
				proxy.setHost("proxy" + hostname.substring(indexOfDot));
				proxy.setPort(8080);
				proxy.setNonProxyHosts("localhost|*" + hostname.substring(indexOfDot));

				if (proxy.test())
				{
					return proxy;
				}
			}
		}
		catch (Exception e)
		{
			//ignore
		}

		return new Proxy();
	}
}
