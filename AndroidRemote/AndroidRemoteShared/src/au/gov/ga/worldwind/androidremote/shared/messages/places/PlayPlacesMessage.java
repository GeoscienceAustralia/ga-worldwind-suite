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
package au.gov.ga.worldwind.androidremote.shared.messages.places;

import java.nio.ByteBuffer;

import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageId;

/**
 * Message notifying the Server to start/stop place list playback.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlayPlacesMessage implements Message<PlayPlacesMessage>
{
	/**
	 * Should the places start playback?
	 */
	public final boolean play;

	@SuppressWarnings("unused")
	private PlayPlacesMessage()
	{
		this(false);
	}

	public PlayPlacesMessage(boolean play)
	{
		this.play = play;
	}

	@Override
	public MessageId getId()
	{
		return MessageId.PLAY_PLACES;
	}

	@Override
	public int getLength()
	{
		return 1;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		buffer.put((byte) (play ? 1 : 0));
	}

	@Override
	public PlayPlacesMessage fromBytes(ByteBuffer buffer)
	{
		boolean play = buffer.get() != 0;
		return new PlayPlacesMessage(play);
	}
}
