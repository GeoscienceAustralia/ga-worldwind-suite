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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;
import au.gov.ga.worldwind.androidremote.shared.AbstractCommunicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.messages.ExitMessage;

/**
 * {@link Communicator} implementation that uses Bluetooth to send/receive
 * messages to/from the server.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AndroidCommunicator extends AbstractCommunicator
{
	private static final String TAG = AndroidCommunicator.class.getSimpleName();
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //SPP UUID

	private final Activity context;
	private final BluetoothAdapter adapter;

	private BluetoothDevice device;

	private ConnectingThread connectingThread;
	private ConnectedThread connectedThread;

	private Toast toast;

	public AndroidCommunicator(Activity context, BluetoothAdapter adapter)
	{
		this.context = context;
		this.adapter = adapter;
	}

	public BluetoothDevice getDevice()
	{
		return device;
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

	public synchronized void connect(BluetoothDevice device)
	{
		if (getState() == State.CONNECTED)
		{
			sendMessage(new ExitMessage());
		}

		Log.d(TAG, "connect: " + device);
		this.device = device;

		stopThreads();

		// Start the thread to connect with the given device
		connectingThread = new ConnectingThread(device);
		connectingThread.start();
		setState(State.CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
	{
		Log.d(TAG, "connected");

		stopThreads();

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(socket);
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

	public void toast(final int stringResourceId)
	{
		toast(context.getString(stringResourceId));
	}

	public void toast(final String s)
	{
		context.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (toast == null)
				{
					toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
				}
				else
				{
					toast.setText(s);
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
		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		public ConnectingThread(BluetoothDevice device)
		{
			this.device = device;
			BluetoothSocket tmp = null;
			try
			{
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			}
			catch (IOException e)
			{
				Log.e(TAG, "create() failed", e);
			}
			socket = tmp;
		}

		@Override
		public void run()
		{
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			adapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try
			{
				// This is a blocking call and will only return on a
				// successful connection or an exception
				socket.connect();
			}
			catch (IOException e)
			{
				connectionFailed();
				// Close the socket
				try
				{
					socket.close();
				}
				catch (IOException e2)
				{
					Log.e(TAG, "unable to close() socket during connection failure", e2);
				}
				// Start the service over to restart listening mode
				AndroidCommunicator.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (AndroidCommunicator.this)
			{
				connectingThread = null;
			}

			// Start the connected thread
			connected(socket, device);
		}

		public void cancel()
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket socket;
		private final InputStream is;
		private final OutputStream os;

		public ConnectedThread(BluetoothSocket socket)
		{
			Log.d(TAG, "create ConnectedThread");
			this.socket = socket;
			InputStream tmpIs = null;
			OutputStream tmpOs = null;

			// Get the BluetoothSocket input and output streams
			try
			{
				tmpIs = socket.getInputStream();
				tmpOs = socket.getOutputStream();
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

			// Keep listening to the InputStream while connected
			while (true)
			{
				try
				{
					final Message<?> message = MessageIO.readMessage(is);
					context.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							fireReceivedMessage(message);
						}
					});
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
			try
			{
				MessageIO.writeMessage(message, os);
			}
			catch (IOException e)
			{
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel()
		{
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
