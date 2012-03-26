## Geoscience Australia World Wind Suite ##
# TileServer README #

The `TileServer` is a simple JSP application that can be used to serve tile requests from a 
NASA World Wind formatted tile cache, such as those created by the `Tiler` application.

The `TileServer` is fully World Wind compliant - it can be used to serve tiles to any application
built on the NASA World Wind SDK. It also contains extensions specific to the Geoscience Australia
World Wind Suite that provides efficient filesystem and network usage.

## Project contents ##
The `TileServer` project contains a number of files. Many of these are example files used to become familiar
with how the system works. The actual tile server components are found in the `tile_server` folder. 

The project contents are described below.

<pre>
+-- blank_tiles			A folder containing 'blank' tiles for use as placeholders
| +-- blank.dds				A blank DDS tile
| +-- blank.gif				A blank GIF tile
| +-- blank.jpg				A blank JPG tile
| `-- blank.png				A blank PNG tile
+-- example_dataset		A folder containing an example dataset structure. For use as a reference only.
| `--...
+-- jnlp_jsp			A folder containing example JSP fragments that generate Java webstart JNLP responses.
| `-- webstart.jnlp.jsp		A fragment for generating a JNLP response for the GA worldwind application
`-- tile_server			Contains the actual tile server components
 +-- common.inc				The common include file. Contains the logic for the tile server
 +-- elev.jsp				The JSP file for elevation data requests
 `-- tiles.jsp				The JSP file for raster tile data requests
</pre>

## Installation ##
The `TileServer` is very simple to install. All it requires is a JSP container instance (such as [Apache Tomcat](http://tomcat.apache.org/)).
The `TileServer` can be installed into any compliant JSP container. Each container has it's own application deployment process. The instructions
given here are for [Apache Tomcat](http://tomcat.apache.org/).

To install:

1.	Create a new folder under the `%CATALINA_BASE%/webapps` directory. Give this folder a meaningful but short name (as it will form part of the URL
	used to access the tileserver instance). 
	An example might be `wwtileserver`.

2.	Copy the files `tiles.jsp`, `elev.jsp` and `common.inc` into the newly created folder.

3.	Edit the marked lines in `common.inc` to point to the root folder of your tile caches.

4.	Restart Tomcat.

You should now be able to access the `TileServer` using a URL like `http://www.yourdomain.com/wwtileserver/tiles.jsp' 
(_Note that the actual URL will depend on how Tomcat has been set up_).

## License ##

The `TileServer` project is released under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) open source license.
For more information, see `LICENSE-2.0.html`, or [http://www.apache.org/licenses/LICENSE-2.0.htm](http://www.apache.org/licenses/LICENSE-2.0.html).

## See also ##

1. [Geoscience Australia](http://www.ga.gov.au)
2. [NASA World Wind SDK](http://worldwind.arc.nasa.gov/java/)