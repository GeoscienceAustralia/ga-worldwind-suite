<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>GA 3D Data Viewer - Help - FAQ</title>
		
		<link href="style.css" rel="stylesheet" type="text/css" />
	</head>
	<body>
		<a name="top"/>
		<h2>Frequently Asked Questions (FAQs)</h2>
		<p>Below are a number of frequently asked questions, along with answers.</p>
		<hr/>
		<ul>
			<li><a href="#q1">Why can't I see <em>layerX</em> on the globe?</a></li>
			<li><a href="#q2">All of my layers are showing errors loading. What's happening?</a></li>
		</ul>
		<hr/>
		
		<a name="q1"/><h3>Why can't I see <em>layerX</em> on the globe?</h3>
		<p>
			There are a few reasons why a layer may not appear on the globe. Below are some of them,
			ordered roughly by likelihood.
			<ol>
				<li>
					<strong>It is not enabled</strong>
					<br/>
					Only enabled layers are shown on the globe. 
					Ensure that the check box in the Layers panel are next to the layer name is checked. 
				</li>
				<li>
					<strong>It's opacity is 0%</strong>
					<br/>
					If the layer's opacity is 0% it will be completely transparent and will not be visible on the globe.<br/>
					Ensure that the opacity of your layer is greater than 0%. This can be controlled using the opacity 
					slider in the <span class="UIElementReference">Layers</span> panel.
				</li>
				<li>
					<strong>It is being obstructed by another layer</strong>
					<br/>
					Layers in the 3D globe are drawn in order from top-to-bottom. 
					If your layer is ordered above another layer with the same spatial extents, it may
					be completely obstructed.<br/> 
					Try moving your layer to the bottom of the layer tree, or
					adjusting the opacity of any potentially obstructing layers.
				</li>
				<li>
					<strong>The layer imagery has not loaded yet</strong>
					<br/>
					If there is some network congestion it may take a few moments for the layer imagery to load.<br/>
					If you are sure they layer should be visible, try waiting to see if the imagery is being loaded.
					An indication that this may be the case is a loading icon displayed next to the layer in the layer tree.
				</li>
				<li>
					<strong>It has not activated yet</strong>
					<br/>
					Some layers will only activate when you zoom in close enough to it.<br/>
					Try zooming into the region where you expect to see data.
				</li>
			</ol> 
			<a href="#top">[Top]</a>
		</p>
		
		<a name="q2"/><h3>All of my layers are showing errors loading. What's happening?</h3>
		<p>
			This is most likely caused by a network problem. If you use a proxy to connect to the internet, ensure that the proxy settings
			have been updated correctly. These settings can be accessed via the <span class="UIElementReference">Network</span> panel of the 
			<span class="UIElementReference">Preferences</span> dialog (<span class="UIElementReference">Tools->Preferences...</span>). The 
			correct proxy settings can often be obtained from your network administrator, or by looking in the connection settings of your
			web browser.
			If this does not work, or you are not using a proxy server, confirm that your internet connection is working correctly. 
			The 3D data viewer requires a working connection to retrieve data for layers.
			<br/><a href="#top">[Top]</a>
		</p>
	</body>
</html>