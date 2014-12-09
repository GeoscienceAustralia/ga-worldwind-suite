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

import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.WindowManager;
import android.widget.Toast;
import au.gov.ga.worldwind.androidremote.client.state.DatasetModelState;
import au.gov.ga.worldwind.androidremote.client.state.ItemModelState;
import au.gov.ga.worldwind.androidremote.client.state.ItemModelStateProvider;
import au.gov.ga.worldwind.androidremote.client.state.LayerModelState;
import au.gov.ga.worldwind.androidremote.client.state.PlaceModelState;
import au.gov.ga.worldwind.androidremote.client.ui.ControlFragment;
import au.gov.ga.worldwind.androidremote.client.ui.ItemModelFragment;
import au.gov.ga.worldwind.androidremote.client.ui.VerticalExaggerationDialog;
import au.gov.ga.worldwind.androidremote.client.ui.menu.EmptyMenuProvider;
import au.gov.ga.worldwind.androidremote.client.ui.menu.ItemModelFragmentMenuProvider;
import au.gov.ga.worldwind.androidremote.client.ui.menu.PlacesMenuProvider;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.ExitMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.FlyHomeMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.IpAddressesMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.LocationMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.ShakeMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.DownMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.Finger;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.FingerMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.MoveMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.UpMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.ve.VerticalExaggerationMessage;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Main activity in the application. Contains the applications fragments which
 * it pushes in when the user changes tabs. Contains the ItemModelState objects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Remote extends SherlockFragmentActivity implements CommunicatorListener, ActionBar.TabListener,
		ItemModelStateProvider
{
	private BluetoothAdapter bluetoothAdapter;
	private AndroidCommunicator communicator;
	private SocketAndroidCommunicator remoteViewCommunicator;
	private SensorManager sensorManager;

	// Intent request codes
	private static final int REQUEST_DEVICES = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	private static final int OPTION_CONNECT = 0;
	private static final int OPTION_LOCK_LANDSCAPE = 1;
	private static final int OPTION_ENABLE_REMOTE_VIEW = 2;
	private static final int OPTION_EXIT = 3;
	private static final int OPTION_EXAGGERATION = 4;
	private static final int OPTION_FLY_HOME = 5;
	private static final int OPTION_HELP = 6;
	private static final int OPTION_SEND_LOCATION = 7;

	private VelocityTracker velocityTracker;
	private final ShakeEventListener sensorListener = new ShakeEventListener();

	private ControlFragment controlFragment;
	private ItemModelFragment datasetsFragment, layersFragment, flatLayersFragment, placesFragment;
	private Fragment[] tabFragments;
	private final SparseArray<ItemModelState> itemModelStates = new SparseArray<ItemModelState>();
	private final SparseArray<ItemModelFragmentMenuProvider> menuProviders = new SparseArray<ItemModelFragmentMenuProvider>();
	private boolean lockLandscape;
	private boolean sendLocation;
	private Fragment currentFragment;
	private float currentExaggeration;

	private boolean controlling = false;
	private boolean devicesShowing = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//Normally one shouldn't instantiate all these objects in the onCreate method,
		//as onCreate is called every time a configuration change occurs (orientation,
		//keyboard hidden, screen size, etc). But we are handling configuration changes
		//ourselves.

		//hide the status bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//get local Bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null)
		{
			Toast.makeText(this, R.string.bluetooth_unavailable, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		communicator = new AndroidCommunicator(this, bluetoothAdapter);
		communicator.addListener(this);

		remoteViewCommunicator = new SocketAndroidCommunicator(this);

		DatasetModelState datasetsState = new DatasetModelState(communicator, this);
		LayerModelState layersState = new LayerModelState(communicator, this);
		PlaceModelState placesState = new PlaceModelState(communicator, this);

		ItemModelState[] states = new ItemModelState[] { datasetsState, layersState, placesState };
		for (ItemModelState state : states)
		{
			itemModelStates.put(state.getModel().getId(), state);
			ItemModelFragmentMenuProvider menuProvider = new EmptyMenuProvider();
			if (state == placesState)
			{
				menuProvider = new PlacesMenuProvider(communicator);
			}
			menuProviders.put(state.getModel().getId(), menuProvider);
		}

		controlFragment = ControlFragment.newInstance(remoteViewCommunicator);
		datasetsFragment = ItemModelFragment.newInstance(datasetsState.getModel().getId(), false);
		layersFragment = ItemModelFragment.newInstance(layersState.getModel().getId(), false);
		flatLayersFragment = ItemModelFragment.newInstance(layersState.getModel().getId(), true);
		placesFragment = ItemModelFragment.newInstance(placesState.getModel().getId(), false);
		tabFragments =
				new Fragment[] { controlFragment, datasetsFragment, layersFragment, flatLayersFragment, placesFragment };

		//create the tabs
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		int[] tabIds =
				new int[] { R.string.controls_tab, R.string.datasets_tab, R.string.layers_tab,
						R.string.flat_layers_tab, R.string.places_tab };
		for (int i = 0; i < tabIds.length; i++)
		{
			ActionBar.Tab tab = getSupportActionBar().newTab();
			tab.setTag(tabIds[i]);
			tab.setText(tabIds[i]);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(true);

		//setup the shake sensor
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener()
		{
			@Override
			public void onShake()
			{
				communicator.sendMessage(new ShakeMessage());
			}
		});

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener()
		{
			public void onLocationChanged(Location location)
			{
				if (isSendLocation())
				{
					communicator.sendMessage(new LocationMessage(location.getLatitude(), location.getLongitude(), location
							.getAltitude(), location.getAccuracy(), location.getBearing()));
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras)
			{
			}

			public void onProviderEnabled(String provider)
			{
			}

			public void onProviderDisabled(String provider)
			{
			}
		};
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if (!bluetoothAdapter.isEnabled())
		{
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		else if (communicator.getState() != State.CONNECTED && !devicesShowing)
		{
			connect();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (communicator != null && communicator.getState() == AndroidCommunicator.State.NONE)
		{
			communicator.start();
			remoteViewCommunicator.start();
		}

		updateActionBarIcon(communicator.getState());
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	protected void onPause()
	{
		sensorManager.unregisterListener(sensorListener);
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		if (communicator != null)
		{
			communicator.sendMessage(new ExitMessage());
			communicator.stop();
			remoteViewCommunicator.stop();
		}
		velocityTracker.recycle();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, OPTION_CONNECT, 0, R.string.connect_menu).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, OPTION_EXAGGERATION, 0, R.string.vertical_exaggeration_menu);
		menu.add(0, OPTION_FLY_HOME, 0, R.string.fly_home);
		menu.add(0, OPTION_SEND_LOCATION, 0, R.string.send_location);
		menu.add(0, OPTION_LOCK_LANDSCAPE, 0, R.string.lock_landscape_menu).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, OPTION_ENABLE_REMOTE_VIEW, 0, R.string.remote_view_menu).setIcon(android.R.drawable.ic_dialog_map);
		menu.add(0, OPTION_HELP, 0, R.string.help).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, OPTION_EXIT, 0, R.string.exit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean connect = false;
		switch (item.getItemId())
		{
		case OPTION_CONNECT:
			connect = true;
			break;
		case OPTION_LOCK_LANDSCAPE:
			setLockLandscape(!item.isChecked());
			item.setChecked(isLockLandscape());
			break;
		case OPTION_ENABLE_REMOTE_VIEW:
			controlFragment.setRemoteEnabled(!item.isChecked());
			item.setChecked(controlFragment.isRemoteEnabled());
			break;
		case OPTION_EXIT:
			finish();
			break;
		case OPTION_HELP:
			showHelp();
			break;
		case OPTION_EXAGGERATION:
			showVerticalExaggerationDialog();
			break;
		case OPTION_FLY_HOME:
			communicator.sendMessage(new FlyHomeMessage());
			break;
		case OPTION_SEND_LOCATION:
			setSendLocation(!item.isChecked());
			item.setChecked(isSendLocation());
			break;
		case android.R.id.home:
			getSupportActionBar().selectTab(getSupportActionBar().getTabAt(0));
			connect = communicator.getState() != State.CONNECTED;
			break;
		}
		if (connect)
		{
			connect();
		}
		return super.onOptionsItemSelected(item);
	}

	protected void connect()
	{
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() == 1)
		{
			communicator.connect(pairedDevices.iterator().next());
		}
		else
		{
			Intent serverIntent = new Intent(this, Devices.class);
			startActivityForResult(serverIntent, REQUEST_DEVICES);
			devicesShowing = true;
		}
	}

	protected void showHelp()
	{
		startActivity(new Intent(this, Help.class));
	}

	protected void showVerticalExaggerationDialog()
	{
		Dialog dialog = new VerticalExaggerationDialog(this, currentExaggeration, communicator);
		dialog.setCanceledOnTouchOutside(true);

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		lp.width = Math.min(metrics.widthPixels - 20, (int) (400 * metrics.density));

		dialog.show();
		dialog.getWindow().setAttributes(lp);
	}

	@Override
	public void onBackPressed()
	{
		if (currentFragment != null && currentFragment instanceof ItemModelFragment)
		{
			ItemModelFragment imf = (ItemModelFragment) currentFragment;
			imf.onBackPressed();
		}
	}

	protected void ensureDiscoverable()
	{
		if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
		{
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			startActivity(discoverableIntent);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case REQUEST_DEVICES:
			// When DeviceListActivity returns with a device to connect
			devicesShowing = false;
			if (resultCode == Activity.RESULT_OK)
			{
				// Get the device MAC address
				String address = data.getExtras().getString(Devices.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
				// Attempt to connect to the device
				communicator.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK)
			{
				// Bluetooth is now enabled
			}
			else
			{
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	@Override
	public void stateChanged(final State newState)
	{
		String toast = null;
		switch (newState)
		{
		case CONNECTED:
			toast = getString(R.string.title_connected_to) + communicator.getDevice().getName();
			//toast = getString(R.string.title_connected_to) + communicator.getHostname();
			break;
		case CONNECTING:
			toast = getString(R.string.title_connecting);
			break;
		case LISTEN:
		case NONE:
			toast = getString(R.string.title_not_connected);
			break;
		}

		//state changing can occur on non-ui thread, so ensure toast is run on ui thread
		final String toastf = toast;
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (toastf != null)
				{
					communicator.toast(toastf);
				}

				updateActionBarIcon(newState);

				if (newState != State.CONNECTED)
				{
					for (int i = 0; i < itemModelStates.size(); i++)
					{
						itemModelStates.valueAt(i).clear();
					}
				}
			}
		});
	}

	protected void updateActionBarIcon(State state)
	{
		int iconId = state == State.CONNECTED ? R.drawable.ic_launcher : R.drawable.ic_launcher_gray;
		getSupportActionBar().setIcon(iconId);
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof ItemMessage<?>)
		{
			ItemMessage<?> itemMessage = (ItemMessage<?>) message;
			itemModelStates.get(itemMessage.getModelId()).handleMessage(itemMessage);
		}
		else if (message instanceof IpAddressesMessage)
		{
			remoteViewCommunicator.connect(((IpAddressesMessage) message).ipAddresses);
		}
		else if (message instanceof VerticalExaggerationMessage)
		{
			currentExaggeration = ((VerticalExaggerationMessage) message).exaggeration;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (controlling)
		{
			velocityTracker.addMovement(event);
			velocityTracker.computeCurrentVelocity(1);

			boolean down =
					event.getActionMasked() == MotionEvent.ACTION_DOWN
							|| event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN;
			boolean up =
					event.getActionMasked() == MotionEvent.ACTION_UP
							|| event.getActionMasked() == MotionEvent.ACTION_POINTER_UP;
			Finger[] fingers = new Finger[event.getPointerCount()];
			for (int i = 0; i < event.getPointerCount(); i++)
			{
				fingers[i] =
						new Finger(event.getPointerId(i), event.getX(i), event.getY(i),
								velocityTracker.getXVelocity(i), velocityTracker.getYVelocity(i),
								!(event.getActionIndex() == i && up));
			}

			FingerMessage<?> message =
					up ? new UpMessage(fingers) : down ? new DownMessage(fingers) : new MoveMessage(fingers);
			communicator.sendMessage(message);
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		int pos = tab.getPosition();
		controlling = pos == 0;
		ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
		currentFragment = tabFragments[pos];
		ft.replace(android.R.id.content, currentFragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
	}

	@Override
	public ItemModelState getItemModelState(int itemModelId)
	{
		return itemModelStates.get(itemModelId);
	}

	@Override
	public ItemModelFragmentMenuProvider getMenuProvider(int id)
	{
		return menuProviders.get(id);
	}

	public boolean isLockLandscape()
	{
		return lockLandscape;
	}

	public void setLockLandscape(boolean lockLandscape)
	{
		this.lockLandscape = lockLandscape;
		setRequestedOrientation(lockLandscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
				: ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	public boolean isSendLocation()
	{
		return sendLocation;
	}
	
	public void setSendLocation(boolean sendLocation)
	{
		this.sendLocation = sendLocation;
	}
}
