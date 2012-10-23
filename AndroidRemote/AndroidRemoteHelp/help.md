World Wind Android Remote
=========================

Introduction
------------

The World Wind Android Remote suite consists of two applications:

1. The Server: a customized version of Geoscience Australia's World Wind Viewer which runs on a desktop computer.
2. The Remote: an Android app which remotely controls certain functionality of the Server, such as enabled layers and layer opacity, and camera movement.

The two applications communicate using the Bluetooth protocol. The Remote can also display a remote view of the globe; this requires both the Android and the computer to be connected to the same Local Area Network.

Getting Started
---------------

### Pairing ###

The Android device must first be paired to the computer using Bluetooth. To achieve this on Windows:

1. Open the "Bluetooth Devices" item from Control Panel.
2. Click "Add...".
3. Ensure that Bluetooth is turned on on the Android device. Also turn on Bluetooth discoverability (by default this will last for 120 seconds).
4. Check "My device is set up and ready to be found.", and click "Next >".
5. Select the Android device you wish to pair with and click "Next >".
6. Leave "Choose a passkey for me" selected, and click "Next >".
7. The Android device should prompt you for the passkey, which will be generated and displayed by the computer. Enter this passkey on the Android.
8. The two devices are now paired.

The Remote supports automatic connection. In order for this to work, the Android must only be paired to a single device/computer at a time. Otherwise the app will ask which device you wish to connect to each time.

### Running the application ###

1. Start the Server application by double clicking the "run.bat" file on the computer. The standard Viewer application should appear, with the virtual globe view.
2. Run the Remote app on the Android (the blue and white target icon).
3. If the devices have been successfully paired (see above), the app should automatically connect to the computer. If not, you can manually connect by clicking the Android's Menu button and clicking "Connect...". When connected, the icon in the top left of the app will appear blue instead of grey.
4. You should now be able to control the camera by dragging your fingers on the screen on the "Control" tab.

Usage
-----

The app displays a number of tabs across the top of the screen. Each tab controls/displays a different part of the application:

1. __CONTROL__ - provides control of the camera using your fingers.
2. __DATASETS__ - shows the hierarchy of datasets available in the Datasets panel of the Viewer, and allows adding new layers from the dataset list.
3. __LAYERS__ - displays the hierarchy of layers that have been added to the Viewer in the Layers panel. Provides ability for turning layers on/off, changing layer opacity, and deleting layers from the list.
4. __LAYERS (FLAT)__ - displays a flat list of the layers in the Layers panel. Provides the same functionality as the LAYERS tab, but with a simple list instead of a hierarchy.
5. __PLACES__ - shows the user's places that have been added to the Viewer's Places panel. Allows for flying to the given places, as well as starting/stopping automatic playback of the places list.

### Controlling the camera ###

_The Android device must have multitouch capabilities to fully support the camera control functionality of the app._

To control the camera, ensure the CONTROL tab is selected. You can quickly select this tab by touching the icon at the top left of the app.

 - To move the camera around the globe, drag a single finger across the screen in the desired direction. You can also set the globe spinning by performing a swipe gesture with one finger (touch down, move, touch up, in one motion).
 - To zoom in or out, drag two fingers toward each other or away from each other, in a pinching motion.
 - To rotate the camera, drag two fingers in a circular motion around a center point.
 - To pitch the camera, drag two fingers up or down.

If you ever get lost and wish to reset the camera back to the default view, press the Android menu button and select "Fly home". (The Android menu button is either a hardware button, a soft button, or represented as 3 dots in the top right corner of the app.) 

### Browsing datasets ###

To view the available datasets, select the DATASETS tab. You can then browse the dataset hierarchy.

Tapping on a folder will display the contents of that folder. You can go back to the previous folder by dragging the view left-to-right. You can also go back through the history of folders viewed by clicking the Android back button.

Tapping on a layer behaves like a toggle. Tapping an unadded layer will add the layer to the layer list, which is available on the LAYERS tab. Tapping any added layer will remove it from the layers list.

### Controlling layers ###

To browse the Viewer's layers, select the LAYERS tab, which allows access to the layers hierarchy. Alternatively you can browse a flat list view (non-hierarchical) of the layers using the LAYERS (FLAT) tab.

Like the DATASETS tab, tapping on a folder will display the contents of that folder. Dragging the view left-to-right will go back, and you can use the Android back button to go back through the folder history.

Tapping on a layer will display a dialog that allows toggling the layer on/off, changing the opacity, and deleting the layer from the layer list. If you delete a layer who's parent folders only contain that layer, the parent folders will also be removed.

### Flying to saved places ###

The Viewer's saved places are also available in the Android app, by switching to the PLACES tab. Places cannot be added/removed from the Android app; this must be performed in the Viewer's computer interface.

Tapping on a place will select the place, and the camera will fly to the coordinates defined by the place. Layers defined by the place will also be turned on/off.

You can also start and stop playback of all defined places, by clicking the Play icon in the top right corner of the app. This playback will automatically be stopped if any places are selected, or if the user starts controlling the camera in the CONTROL tab.

### Remote view ###

_This is an experimental feature._

The view of the globe can be streamed to the Android app if both the Android device and the computer exist on the same subnet of a LAN (ie the computer has a network interface with an IP address that is accessible by the Android). To enable this, press the Android menu button, and selecting the "Remote view" checkbox.

### Other functions ###

You can set the vertical exaggeration of the globe by pressing the Android menu button, and selecting the "Vertical exaggeration..." option.

If you want to lock the app orientation of the Android app so that it doesn't rotate when rotating the device, you can select the "Lock landscape" checkbox in the menu. This will lock the interface to landscape orientation.

If you ever get stuck controlling the camera and you want to reset the view, select the "Fly home" menu option.

The app can be quit by selecting the "Exit" menu option.

Troubleshooting
---------------

_DATASETS or LAYERS tabs are exhibiting strange behaviour._ The computer and Android models may have gotten out of sync. Try forcing a reconnection by selecting the "Connect..." menu option. This will refresh all dataset/layer/place information.

_The camera is stuck somewhere._ Reset the view by selecting the "Fly home" menu option.
