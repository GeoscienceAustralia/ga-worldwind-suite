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

import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;

import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.EnableRemoteViewMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.RemoteViewMessage;
import au.gov.ga.worldwind.viewer.stereo.StereoSceneController;

import com.jogamp.common.nio.Buffers;

/**
 * {@link SceneController} implementation used for sending a remote view of the
 * globe to the Android device (if enabled and connected correctly).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RemoteViewSceneController extends StereoSceneController implements CommunicatorListener
{
	private static final float IMAGE_SCALE = 1f / 2f;
	private Communicator communicator;
	private WorldWindow wwd;
	private boolean remoteViewEnabled = false;
	private int bands = 3;
	private int desiredWidth, desiredHeight;
	private int width, height;
	private ByteBuffer buffer;
	private BufferedImage[] images = new BufferedImage[2];
	private Thread thread;
	private Object semaphore = new Object();
	private int lastImage = 0;
	private boolean sending = false;

	private BlockingQueue<BufferedImage> imageSendQueue = new ArrayBlockingQueue<BufferedImage>(1);

	public void setup(Communicator communicator, WorldWindow wwd)
	{
		this.communicator = communicator;
		this.wwd = wwd;
		communicator.addListener(this);
	}

	@Override
	protected void draw(DrawContext dc)
	{
		if (communicator != null && communicator.getState() == State.CONNECTED && remoteViewEnabled && buffer != null
				&& imageSendQueue.isEmpty() && !sending)
		{
			synchronized (semaphore)
			{
				GL2 gl = dc.getGL();
				Rectangle oldViewport = view.getViewport();

				gl.glViewport(0, 0, desiredWidth, desiredHeight);
				this.applyView(dc);
				gl.glViewport(0, 0, width, height);
				super.draw(dc);

				//GL.GL_BGRA is the fastest for glReadPixels
				int readType = bands == 4 ? GL2.GL_BGRA : GL2.GL_BGR;
				gl.glReadPixels(0, 0, width, height, readType, GL2.GL_UNSIGNED_BYTE, buffer.rewind());

				lastImage = (lastImage + 1) % images.length;
				BufferedImage image = images[lastImage];
				byte[] dst = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

				//flip the image:
				buffer.rewind();
				for (int i = 0; i < height; i++)
				{
					buffer.get(dst, (height - i - 1) * width * bands, width * bands);
				}
				sendImage(image);

				gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
				gl.glViewport(oldViewport.x, oldViewport.y, oldViewport.width, oldViewport.height);
				this.applyView(dc);
			}
		}
		super.draw(dc);
	}

	protected void sendImage(BufferedImage image)
	{
		imageSendQueue.add(image);

		if (thread == null)
		{
			thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							BufferedImage image = imageSendQueue.take();
							sending = true;

							//QuantizeFilter filter = new QuantizeFilter();
							//filter.setNumColors(100);
							//image = filter.filter(image, null);

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(image, "jpg", baos);
							ByteBuffer jpegBuffer = ByteBuffer.wrap(baos.toByteArray());
							baos.close();
							communicator.sendMessage(new RemoteViewMessage(jpegBuffer));
							sending = false;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public void stateChanged(State newState)
	{
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof EnableRemoteViewMessage)
		{
			synchronized (semaphore)
			{
				EnableRemoteViewMessage enable = (EnableRemoteViewMessage) message;
				remoteViewEnabled = enable.enabled;
				if (enable.enabled)
				{
					//ensure width/height are divisible by 4, otherwise JVM crash!
					this.width = ((int) (enable.width * IMAGE_SCALE) / 4) * 4;
					this.height = ((int) (enable.height * IMAGE_SCALE) / 4) * 4;
					this.desiredWidth = enable.width;
					this.desiredHeight = enable.height;
					buffer = Buffers.newDirectByteBuffer(width * height * bands);
					int imageType = bands == 4 ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;
					images[0] = new BufferedImage(width, height, imageType);
					images[1] = new BufferedImage(width, height, imageType);
				}

				if (wwd != null)
				{
					wwd.redraw();
				}
			}
		}
	}
}
