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
package au.gov.ga.worldwind.androidremote.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import au.gov.ga.worldwind.androidremote.shared.AbstractCommunicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;

/**
 * {@link Communicator} implementation that uses sockets for sending/receiving
 * messages. Used for the remote view, as wifi/lan has higher bandwidth that HID
 * bluetooth.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SocketAndroidCommunicator extends AbstractCommunicator
{
	private static final String TAG = AndroidCommunicator.class.getSimpleName();
	private static final int PORT = 23549;

	private final Activity context;

	private ConnectingThread connectingThread;
	private ConnectedThread connectedThread;

	private Toast toast;

	@SuppressLint("NewApi")
	public SocketAndroidCommunicator(Activity context)
	{
		this.context = context;

		try
		{
			android.os.StrictMode.ThreadPolicy policy =
					new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
			android.os.StrictMode.setThreadPolicy(policy);
		}
		catch (Exception e)
		{
		}
	}

	public synchronized void start()
	{
		Log.d(TAG, "start");
		stopThreads();
		setState(State.LISTEN);
	}

	public synchronized void stop()
	{
		Log.d(TAG, "stop");
		stopThreads();
		setState(State.NONE);
	}

	private void stopThreads()
	{
		if (connectingThread != null)
		{
			connectingThread.cancel();
			connectingThread = null;
		}
		if (connectedThread != null)
		{
			connectedThread.cancel();
			connectedThread = null;
		}
	}

	public synchronized void connect(String[] hostnames)
	{
		Log.d(TAG, "connect: " + Arrays.toString(hostnames));

		stopThreads();

		// Start the thread to connect with the given device
		connectingThread = new ConnectingThread(hostnames);
		connectingThread.start();
		setState(State.CONNECTING);
	}

	public synchronized void connected(Socket connection)
	{
		Log.d(TAG, "connected");

		stopThreads();

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(connection);
		connectedThread.start();
		setState(State.CONNECTED);
	}

	private void connectionFailed()
	{
		setState(State.LISTEN);
		toast(R.string.connection_failed);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost()
	{
		setState(State.LISTEN);
		toast(R.string.connection_lost);
	}

	private void toast(final int stringResourceId)
	{
		context.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (toast == null)
				{
					toast = Toast.makeText(context, stringResourceId, Toast.LENGTH_SHORT);
				}
				else
				{
					toast.setText(stringResourceId);
				}
				toast.show();
			}
		});
	}

	@Override
	public void sendMessage(Message<?> message)
	{
		if (state == State.CONNECTED && connectedThread != null)
		{
			connectedThread.write(message);
		}
	}

	private class ConnectingThread extends Thread
	{
		private final String[] hostnames;

		public ConnectingThread(String[] hostnames)
		{
			this.hostnames = hostnames;
		}

		@Override
		public void run()
		{
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Make a connection to the BluetoothSocket
			Socket socket = null;
			boolean connected = false;
			for (String hostname : hostnames)
			{
				try
				{
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = new Socket();
					socket.connect(new InetSocketAddress(hostname, PORT));
					connected = true;
					break;
				}
				catch (IOException e)
				{
				}
			}

			if (!connected)
			{
				connectionFailed();
				// Start the service over to restart listening mode
				SocketAndroidCommunicator.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (SocketAndroidCommunicator.this)
			{
				connectingThread = null;
			}

			// Start the connected thread
			connected(socket);
		}

		public void cancel()
		{
		}
	}

	private class ConnectedThread extends Thread
	{
		private final Socket socket;
		private final InputStream is;
		private final OutputStream os;
		private BlockingQueue<Message<?>> messageQueue = new LinkedBlockingQueue<Message<?>>();
		private Thread outputThread;
		private boolean cancelled = false;

		public ConnectedThread(Socket socket)
		{
			Log.d(TAG, "create ConnectedThread");
			this.socket = socket;
			InputStream tmpIs = null;
			OutputStream tmpOs = null;

			// Get the BluetoothSocket input and output streams
			try
			{
				tmpIs = new BufferedInputStream(socket.getInputStream());
				tmpOs = new BufferedOutputStream(socket.getOutputStream());
			}
			catch (IOException e)
			{
				Log.e(TAG, "streams not created", e);
			}

			is = tmpIs;
			os = tmpOs;
		}

		@Override
		public void run()
		{
			Log.i(TAG, "begin ConnectedThread");

			outputThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (!cancelled)
					{
						Message<?> message;
						try
						{
							message = messageQueue.take();
						}
						catch (InterruptedException e)
						{
							break;
						}

						try
						{
							MessageIO.writeMessage(message, os);
						}
						catch (IOException e)
						{
							Log.e(TAG, "Exception during write", e);
						}
					}
				}
			});
			outputThread.setDaemon(true);
			outputThread.start();

			// Keep listening to the InputStream while connected
			while (true)
			{
				try
				{
					final Message<?> message = MessageIO.readMessage(is);
					/*context.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							fireReceivedMessage(message);
						}
					});*/
					fireReceivedMessage(message);
				}
				catch (IOException e)
				{
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(Message<?> message)
		{
			messageQueue.add(message);
		}

		public void cancel()
		{
			cancelled = true;
			outputThread.interrupt();
			try
			{
				//outStream.write(EXIT_CMD);
				socket.close();
			}
			catch (IOException e)
			{
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
