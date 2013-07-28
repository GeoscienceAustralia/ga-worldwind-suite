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
package au.gov.ga.worldwind.androidremote.client.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.client.Remote;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.RemoteViewMessage;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment shown on the Control tab. Displays a {@link RemoteView} which allows
 * for viewing the globe remotely on the device. Actual finger processing for
 * camera control occurs in the {@link Remote} activity.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressLint({ "ValidFragment" })
public class ControlFragment extends SherlockFragment implements CommunicatorListener
{
	public static ControlFragment newInstance(Communicator communicator)
	{
		ControlFragment fragment = new ControlFragment(communicator);
		communicator.addListener(fragment);
		return fragment;
	}

	private final Communicator communicator;
	private boolean remoteEnabled = false;
	private Bitmap bitmap;

	private ControlFragment(Communicator communicator)
	{
		this.communicator = communicator;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.control_fragment, container, false);
		RemoteView remoteView = getRemoteView(view);
		remoteView.setCommunicator(communicator);
		remoteView.setEnabled(remoteEnabled);
		remoteView.updateBitmap(bitmap);
		return view;
	}

	/**
	 * A new bitmap has been received by the server; update the RemoteView to
	 * display it.
	 * 
	 * @param bitmap
	 */
	protected void updateBitmap(Bitmap bitmap)
	{
		if (this.bitmap != null)
		{
			this.bitmap.recycle();
		}
		this.bitmap = bitmap;
		RemoteView remoteView = getRemoteView(getView());
		if (remoteView != null)
		{
			remoteView.updateBitmap(bitmap);
		}
	}

	/**
	 * @return Is remote view enabled?
	 */
	public boolean isRemoteEnabled()
	{
		return remoteEnabled;
	}

	/**
	 * Enable/disable remote view.
	 * 
	 * @param remoteEnabled
	 */
	public void setRemoteEnabled(boolean remoteEnabled)
	{
		this.remoteEnabled = remoteEnabled;
		RemoteView remoteView = getRemoteView(getView());
		if (remoteView != null)
		{
			remoteView.setEnabled(remoteEnabled);
		}
	}

	protected RemoteView getRemoteView(View view)
	{
		if (view != null)
		{
			return (RemoteView) view.findViewById(R.id.remoteView);
		}
		return null;
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof RemoteViewMessage)
		{
			RemoteViewMessage remoteView = (RemoteViewMessage) message;
			byte[] array = remoteView.buffer.array();
			final Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
			updateBitmap(bitmap);
		}
	}

	@Override
	public void stateChanged(final State newState)
	{
		bitmap = null;
		Activity activity = getActivity();
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (newState == State.CONNECTED)
					{
						setRemoteEnabled(remoteEnabled);
					}
					else
					{
						updateBitmap(null);
					}
				}
			});
		}
	}
}
