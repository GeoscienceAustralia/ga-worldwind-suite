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
package au.gov.ga.worldwind.androidremote.shared.messages;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.MessageId;

/**
 * Message sent by the server to the client containing the IP addresses for the
 * server's enabled network interfaces. This is used so that the client knows
 * which IP address it should attempt to connect to for the remote view
 * communication.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IpAddressesMessage implements Message<IpAddressesMessage>
{
	private static final Charset charset = Charset.forName("UTF-8");
	
	/**
	 * IP addresses for the server's enabled network interfaces.
	 */
	public final String[] ipAddresses;

	@SuppressWarnings("unused")
	private IpAddressesMessage()
	{
		this(new String[0]);
	}

	public IpAddressesMessage(String[] ipAddresses)
	{
		this.ipAddresses = ipAddresses;
	}

	@Override
	public MessageId getId()
	{
		return MessageId.IP_ADDRESSES;
	}

	@Override
	public int getLength()
	{
		int length = 4;
		if (ipAddresses != null)
		{
			for (String s : ipAddresses)
			{
				length += 4 + charset.encode(s).limit();
			}
		}
		return length;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		MessageIO.intToBytes(ipAddresses.length, buffer);
		if (ipAddresses != null)
		{
			for (String s : ipAddresses)
			{
				if (s == null)
				{
					MessageIO.intToBytes(-1, buffer);
				}
				else
				{
					ByteBuffer sBuffer = charset.encode(s);
					MessageIO.intToBytes(sBuffer.limit(), buffer);
					buffer.put(sBuffer);
				}
			}
		}
	}

	@Override
	public IpAddressesMessage fromBytes(ByteBuffer buffer)
	{
		int length = MessageIO.bytesToInt(buffer);
		String[] ipAddresses = length < 0 ? null : new String[length];
		for (int i = 0; i < length; i++)
		{
			int sLength = MessageIO.bytesToInt(buffer);
			ipAddresses[i] = MessageIO.bytesToString(buffer, sLength, charset);
		}
		return new IpAddressesMessage(ipAddresses);
	}
}
