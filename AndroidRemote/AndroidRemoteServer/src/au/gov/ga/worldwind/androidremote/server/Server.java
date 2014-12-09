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

import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import au.gov.ga.worldwind.androidremote.server.listeners.DatasetLayerConnector;
import au.gov.ga.worldwind.androidremote.server.listeners.PlaceConnector;
import au.gov.ga.worldwind.androidremote.server.view.orbit.AndroidInputProvider;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.FlyHomeMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.IpAddressesMessage;
import au.gov.ga.worldwind.common.input.OrbitInputProviderManager;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.HtmlViewer;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.application.Application;
import au.gov.ga.worldwind.viewer.application.Application.QuitListener;
import au.gov.ga.worldwind.viewer.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.panels.places.PlacesPanel;
import au.gov.ga.worldwind.viewer.theme.Theme;

/**
 * Main class for the Android Remote Server.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Server
{
	public static void main(String[] args)
	{
		Application.setSceneControllerClass(RemoteViewSceneController.class);
		final AndroidInputProvider inputProvider = new AndroidInputProvider();
		OrbitInputProviderManager.getInstance().addProvider(inputProvider);
		final Application application = Application.startWithArgs(args);

		application.addQuitListener(new QuitListener()
		{
			@Override
			public boolean shouldQuit()
			{
				return true;
			}

			@Override
			public void willQuit()
			{
				//LocationLogger.close();
				//RiftLogger.close();
			}
		});

		Theme theme = application.getTheme();
		DatasetPanel datasetPanel = theme.getDatasetPanel();
		LayersPanel layersPanel = theme.getLayersPanel();
		PlacesPanel placesPanel = theme.getPlacesPanel();

		final Communicator communicator = ServerCommunicator.INSTANCE;
		new IpAddressSender(communicator);

		communicator.addListener(new CommunicatorListener()
		{
			@Override
			public void stateChanged(State newState)
			{
			}

			@Override
			public void receivedMessage(Message<?> message)
			{
				if (message instanceof FlyHomeMessage)
				{
					inputProvider.stopGesture();
					application.resetView();
				}
			}
		});

		new VerticalExaggerationSender(application.getWwd(), communicator);

		((RemoteViewSceneController) application.getWwd().getSceneController()).setup(
				SocketServerCommunicator.INSTANCE, application.getWwd());

		DatasetLayerConnector datasetLayerConnector =
				new DatasetLayerConnector(communicator, datasetPanel, layersPanel);
		communicator.addListener(datasetLayerConnector);

		PlaceConnector placeConnector = new PlaceConnector(communicator, placesPanel, inputProvider);
		communicator.addListener(placeConnector);

		//limit zoom out
		double radius = application.getWwd().getModel().getGlobe().getRadius();
		((OrbitView) application.getWwd().getView()).getOrbitViewLimits().setZoomLimits(0, radius * 10);

		JMenu helpMenu = null;
		JMenuBar menu = application.getFrame().getJMenuBar();
		for (int i = 0; i < menu.getMenuCount(); i++)
		{
			JMenu m = menu.getMenu(i);
			if (m.getText().equalsIgnoreCase("help"))
			{
				helpMenu = m;
				break;
			}
		}
		if (helpMenu != null)
		{
			BasicAction action = new BasicAction("Android help", Icons.help.getIcon());
			action.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					JDialog dialog =
							new HtmlViewer(application.getFrame(), "Android Remote", false, "/html/android.html", true);
					dialog.setResizable(false);
					dialog.setSize(640, 480);
					dialog.setLocationRelativeTo(application.getFrame());
					dialog.setVisible(true);
				}
			});
			helpMenu.add(action);
		}
	}

	private static class IpAddressSender implements CommunicatorListener
	{
		private final Communicator communicator;

		public IpAddressSender(Communicator communicator)
		{
			this.communicator = communicator;
			communicator.addListener(this);
		}

		@Override
		public void stateChanged(State newState)
		{
			if (newState == State.CONNECTED)
			{
				try
				{
					List<String> ipAddressList = new ArrayList<String>();
					Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
					while (n.hasMoreElements())
					{
						NetworkInterface e = n.nextElement();
						Enumeration<InetAddress> a = e.getInetAddresses();
						while (a.hasMoreElements())
						{
							InetAddress addr = a.nextElement();
							String ip = addr.getHostAddress();
							if (ip != null)
							{
								ip = ip.trim();
								if (ip.length() > 0 && !"127.0.0.1".equals(ip))
								{
									ipAddressList.add(ip);
								}
							}
						}
					}

					String[] ipAddresses = ipAddressList.toArray(new String[ipAddressList.size()]);
					communicator.sendMessage(new IpAddressesMessage(ipAddresses));
				}
				catch (SocketException e)
				{
					e.printStackTrace();
				}
			}
		}

		@Override
		public void receivedMessage(Message<?> message)
		{
		}
	}
}
