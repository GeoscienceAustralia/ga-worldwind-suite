## Geoscience Australia World Wind Suite ##
# Tiler README #

The `Tiler` project is an application for preparing tilesets for use in the `Viewer` and `Animator` tools, or any tool built on the NASA World Wind SDK.

The `Tiler` currently supports tiling of raster datasets (both image and elevation, using [GDAL](http://www.gdal.org) to provide data loading) and vector datasets (using [Mapnik](https://github.com/mapnik/mapnik/wiki/) styled shapefiles etc.). It gives fine-grained control over tile size, level count and output format, and allows a number of image-processing operations to be performed on the data during tile generation. For a brief overview on tiling, see the *Tiling 101* section below.

For more information on the `ga-worldwind-suite` and the `Tiler` project, see the [project wiki](https://github.com/ga-m3dv/ga-worldwind-suite/wiki).

## Tiling 101 ##
'Tiling' is the process of creating a pyramid tileset of a source dataset. This tileset contains a number of 'levels' (which are versions of the original dataset at difference resolutions), and each 'level' is divided into 'tiles' of the same size. This format allows for very efficient display of the original dataset - tiles of the correct resolution can be downloaded individually rather than waiting for the entire dataset to be downloaded. For more information on the tileset format used in World Wind applications, see [here](http://worldwindcentral.com/wiki/Tiling_System).

### Raster tiling ###
Some points to keep in mind when tiling image/elevation data:

* The default tilesize for raster data is 512x512 pixels and for elevation data is 150x150 pixels. This is the standard for World Wind applications. Only change this if you have a good reason to. If you do change it, try to pick a power-of-2 tilesize to make best use of GPU texture capabilities.
* The default level zero tile size (LZTS) is 36.0 degrees for images and 20.0 degrees for elevation data. This can be tweaked to optimise the number of tiles generated in the tileset. Try the "Calculate Optimal" button to pick a good value for this.
* For image data, the tiler can output JPEG and PNG formatted tiles
  * JPEGs will have better compression for 'complex' imagery such as satellite etc. Use JPEG for this type of data, where compression artefacts won't change the data appearance. JPEG is not suitable (a) if you want to embed transparency in the tiles, or (b) if the pixel data in the image represents distinct values (such as landcover classification or similar).
  * PNGs will have better performance for 'simple' imagery (such as topographic maps etc.). They also allow transparency to be embedded in the tilesets, removing the need for mask generation etc.
* World Wind applications expect tilesets to be in WGS84 lat/lon projection. If your source data isn't in this format, either reproject it yourself or ensure that "`Reproject if required`" is enabled.
* If your data is 'complex' it is worth using bilinear minification/magnification. This will take a bit longer but the results will be more pleasing to the eye than the alternative (nearest-neighbour) approach. Note that if the pixel data in your dataset is 'meaningful' (e.g. a classification etc) you may want to use nearest-neighbour as the bilinear filtering will 'smooth' the data.
* You can override the level count if desired. This will 'chop' off the bottom *n* levels from your tileset, meaning that the highest level of detail in your generated tileset will be less than the source data. Note that while this will also reduce the number of tiles in a tileset, it is different  from changing the LZTS described above, and has different side-effects.
* The `Tiler` gives you the option of replacing pixel values at tiling time. This can be useful in the following scenarios (among others):
  * You want to turn a particular colour (or range of colours) to transparent
  * You want to 'clean up' noisy data (e.g. clamp all values close to black to solid black)
  * You want to 'clean up' elevation data by clamping values close to NODATA to NODATA

### Vector tiling ###
Tiling of vector data is performed by Mapnik. Most of the styling options are controlled by the Mapnik document. However, there are some points to note when using the `Tiler` to prepare vector datasets.

* The World Wind SDK (and certainly the `Viewer` and `Animator` tools) have built-in support for shapefile vector data without the need for tiling. Tiling of vector datasets is intended for complex or large vectors.
* The option to "`Use Mapnik for all levels`" will take longer, but the vectors will be re-generated at the correct resolution for each level prior to rasterisation. This will make lines and boundaries sharper etc. If this option is unchecked, nearest-neighbor scaling will be used.
* The level count should be chosen so that the bottom level tiles at 512x512 pixel resolution match closely with the 'native resolution' of the dataset.

## System Requirements ##
1. Java 1.6+
2. Microsoft .NET framework (Optional - required for vector tiling)

## Installation / Use ##
1. Execute the `Run` target of the Ant build script (build.xml)
2. Once complete, navigate to the `%PROJECT_HOME%\target\executable` directory and copy its contents to a desired install location
3. Launch the `gui.bat` script (or alternatively use the CLI `console.bat`)

## License ##

The `Tiler` project is released under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) open source license.
For more information, see `LICENSE-2.0.html`, or [http://www.apache.org/licenses/LICENSE-2.0.htm](http://www.apache.org/licenses/LICENSE-2.0.html).

