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
package au.gov.ga.worldwind.androidremote.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import au.gov.ga.worldwind.androidremote.shared.AbstractCommunicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.messages.ExitMessage;

/**
 * {@link Communicator} server implementation that uses Bluetooth for
 * sending/receiving messages.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ServerCommunicator extends AbstractCommunicator
{
	public static ServerCommunicator INSTANCE;

	static
	{
		INSTANCE = new ServerCommunicator();
		INSTANCE.listen();
	}

	private ServerCommunicator()
	{
	}

	private final UUID uuid = new UUID("0000110100001000800000805F9B34FB", false); //SPP UUID

	private boolean connectionClosed = false;
	private boolean notifierClosed = false;
	private StreamConnectionNotifier notifier;
	private StreamConnection connection;
	private OutputStream os;
	private InputStream is;
	private ListenThread listenThread;
	private ConnectThread connectThread;

	public void listen()
	{
		close();

		try
		{
			LocalDevice local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);

			System.out.println("Opening UUID: " + uuid.toString());

			String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
			notifier = (StreamConnectionNotifier) Connector.open(url);
			notifierClosed = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		listenThread = new ListenThread();
		listenThread.setName("Incoming connection listener");
		listenThread.setDaemon(true);
		listenThread.start();
		setState(State.LISTEN);
	}

	private void connect(StreamConnection connection)
	{
		closeConnection();
		this.connection = connection;
		connectionClosed = false;

		try
		{
			os = connection.openOutputStream();
			is = connection.openInputStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			closeConnection();
			return;
		}

		connectThread = new ConnectThread();
		connectThread.setName("Connection reader");
		connectThread.setDaemon(true);
		connectThread.start();
		setState(State.CONNECTED);
	}

	@Override
	public void sendMessage(Message<?> message)
	{
		if (os != null)
		{
			try
			{
				MessageIO.writeMessage(message, os);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void close()
	{
		closeConnection();
		closeListener();
		setState(State.NONE);
	}

	private void closeConnection()
	{
		if (connectThread != null)
		{
			connectThread.cancel();
			connectThread = null;
		}
		if (connection != null)
		{
			connectionClosed = true;
			try
			{
				is.close();
				os.close();
				connection.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			is = null;
			os = null;
			connection = null;
		}
	}

	private void closeListener()
	{
		if (listenThread != null)
		{
			listenThread.cancel();
			listenThread = null;
		}
		if (notifier != null)
		{
			notifierClosed = true;
			try
			{
				notifier.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			notifier = null;
		}
	}

	private class ListenThread extends Thread
	{
		private boolean cancelled = false;

		@Override
		public void run()
		{
			// waiting for connection
			while (true)
			{
				try
				{
					if (notifier == null)
					{
						return;
					}
					System.out.println("Listening for connection...");
					StreamConnection connection = notifier.acceptAndOpen(); //blocking
					connect(connection);
				}
				catch (IOException e)
				{
					if (notifierClosed || cancelled)
					{
						return;
					}
					e.printStackTrace();
				}
			}
		}

		public void cancel()
		{
			cancelled = true;
		}
	}

	private class ConnectThread extends Thread
	{
		private boolean cancelled = false;

		@Override
		public void run()
		{
			System.out.println("Waiting for input...");
			while (true)
			{
				try
				{
					Message<?> message = MessageIO.readMessage(is);
					if (message instanceof ExitMessage)
					{
						closeConnection();
						setState(ServerCommunicator.State.LISTEN);
						System.out.println("Exit message received, connection closed");
						return;
					}
					fireReceivedMessage(message);
				}
				catch (IOException e)
				{
					if (connectionClosed || cancelled)
					{
						return;
					}
					e.printStackTrace();
					setState(ServerCommunicator.State.LISTEN);
				}
			}
		}

		public void cancel()
		{
			cancelled = true;
		}
	}
}
