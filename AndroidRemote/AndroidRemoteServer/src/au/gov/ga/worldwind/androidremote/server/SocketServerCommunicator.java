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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import au.gov.ga.worldwind.androidremote.shared.AbstractCommunicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.messages.ExitMessage;

/**
 * {@link Communicator} server implementation that uses sockets for
 * sending/receiving messages. Used for the remote view communication.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SocketServerCommunicator extends AbstractCommunicator
{
	public static SocketServerCommunicator INSTANCE;

	static
	{
		INSTANCE = new SocketServerCommunicator();
		INSTANCE.listen();
	}

	private SocketServerCommunicator()
	{
	}

	private static final int PORT = 23549;
	private ServerSocket provider;
	private Socket connection;

	private boolean providerClosed = false;
	private boolean connectionClosed = false;
	private OutputStream os;
	private InputStream is;

	public void listen()
	{
		close();

		try
		{
			provider = new ServerSocket(PORT);
			providerClosed = false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// waiting for connection
				while (true)
				{
					try
					{
						System.out.println("Listening for connection...");
						Socket connection = provider.accept(); //blocking
						connect(connection);
					}
					catch (IOException e)
					{
						if (!providerClosed)
						{
							e.printStackTrace();
						}
						return;
					}
				}
			}
		});
		thread.setName("Incoming connection listener");
		thread.setDaemon(true);
		thread.start();
		setState(State.LISTEN);
	}

	private void connect(Socket connection)
	{
		closeConnection();
		this.connection = connection;
		connectionClosed = false;

		try
		{
			is = new BufferedInputStream(connection.getInputStream());
			os = new BufferedOutputStream(connection.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			closeConnection();
			return;
		}

		Thread thread = new Thread(new Runnable()
		{
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
							setState(State.LISTEN);
							System.out.println("Exit message received, connection closed");
							return;
						}
						fireReceivedMessage(message);
					}
					catch (IOException e)
					{
						if (!connectionClosed)
						{
							e.printStackTrace();
							setState(State.LISTEN);
						}
						return;
					}
				}
			}
		});
		thread.setName("Connection reader");
		thread.setDaemon(true);
		thread.start();
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
		if (provider != null)
		{
			providerClosed = true;
			try
			{
				provider.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			provider = null;
		}
	}
}
