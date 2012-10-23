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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.messages.EnableRemoteViewMessage;

/**
 * View that displays a remote view of the globe.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RemoteView extends SurfaceView implements SurfaceHolder.Callback
{
	private final SurfaceHolder holder;
	private Communicator communicator;
	private int width, height;
	private boolean created = false;

	public RemoteView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		holder = getHolder();
		holder.addCallback(this);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		setVisibility(enabled ? VISIBLE : INVISIBLE);
		updateServer();
	}

	protected void updateServer()
	{
		if (communicator != null && communicator.getState() == State.CONNECTED)
		{
			if (width > 0 && height > 0)
			{
				communicator.sendMessage(new EnableRemoteViewMessage(isEnabled(), width, height));
			}
		}
	}

	public Communicator getCommunicator()
	{
		return communicator;
	}

	public void setCommunicator(Communicator communicator)
	{
		this.communicator = communicator;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		this.width = width;
		this.height = height;
		updateServer();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		synchronized (holder)
		{
			created = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		synchronized (holder)
		{
			created = false;
		}
	}

	public void updateBitmap(Bitmap bitmap)
	{
		if (bitmap == null)
			return;

		synchronized (holder)
		{
			if (!created)
				return;

			Canvas c = null;
			try
			{
				c = holder.lockCanvas();
				c.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1), new Rect(0, 0,
						width - 1, height - 1), null);
			}
			finally
			{
				if (c != null)
				{
					holder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}
