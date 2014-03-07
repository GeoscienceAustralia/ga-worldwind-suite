# GA World Wind Suite #

The `ga-worldwind-suite` is a collection of tools created by the Geoscience Australia (GA) Movies and 3D Visualisation (M3DV) team. The tools are built around the [NASA World Wind Java SDK](http://worldwind.arc.nasa.gov/java/"), an open-source virtual globe toolkit.

The suite consists of a number of tools, found in the project subfolders. These tools are:

* `Viewer` - A virtual globe tool supporting display of raster, vector, surface and volume data as well as digital elevation models (DEMs).
* `Animator` - A keyframe animation tool designed for the production of flythrough style animations of geoscience and geospatial data.
* `Common` - A general-purpose library that underpins the `Viewer` and `Animator` tools.
* `Tiler` - Used to prepare tilesets for use in the `Viewer` and `Animator` tools.
* `TileServer` - A simple JSP application that can be used to serve World Wind tilesets.

For more information on the project, see the [project wiki](https://github.com/ga-m3dv/ga-worldwind-suite/wiki).

----

**Update: March 2013**

The GA-M3DV team is no longer actively maintaining this project. We have moved our development effort to the [EarthSci platform](https://github.com/ga-m3dv/ga-earthsci-rcp). 

This platform is an open source project that will fill the same niche as the `ga-worldwind-suite`, but has been redesigned from the ground-up to be a flexible earth science visualisation platform. It is built on Eclipse E4AP and has a powerful plugin architecture that makes it easier to add new functionality without having to change the core codebase.


Feel free to continue using the `ga-worldwind-suite` if it meets your needs, but if you want to leverage the ongoing development of new features we recommend you consider the `EarthSci` platform.

----

## Webstart previews ##
We have created a separate [repository](https://github.com/ga-m3dv/ga-worldwind-webstart) containing webstart versions of some of the tools in this repository. To run these applications, use the following links:
* [Viewer](https://raw.github.com/ga-m3dv/ga-worldwind-webstart/master/Webstart/jnlp/viewer.jnlp)
* [Animator](https://raw.github.com/ga-m3dv/ga-worldwind-webstart/master/Webstart/jnlp/animator.jnlp)

We will update these builds periodically to reflect the latest changes in the codebase.

As of Java 7 update 51, Oracle changed the security policy to disable self-signed webstart apps by default. Both of the webstart links above contain self-signed JARs, so you will need to either change the Java security level to Medium, or add the URL to the list of exception sites (see the Security tab in the Java Control Panel).

## Reporting bugs and requesting features ##
We are striving to ensure the `ga-worldwind-suite` is a high-quality and usable suite of tools. If you find any bugs/issues with the tools, or would like to see new features added, please report them via the [issue tracker](https://github.com/ga-m3dv/ga-worldwind-suite/issues). Better yet, implement the changes yourself and then open a [pull request](http://help.github.com/send-pull-requests/).

Please note that your bug/issue/feature may already be registered in the issue tracker. Please take a quick look before you raise a ticket to make sure you aren't duplicating an existing issue. For a good guide on writing effective issue reports, see Allan McRae's blog post ["How to file a bug report"](http://allanmcrae.com/2011/05/how-to-file-a-bug-report/).

## How to contribute ##
The `ga-worldwind-suite` is being managed using a "fork+pull" method described [here](http://help.github.com/fork-a-repo/) and we encourage developers to contribute in any way they can. To contribute patches to the codebase:

1. Fork the repository
2. Make your changes
3. Submit a pull request

Some ideas for how to contribute include:

* Find a bug/issue in the [issue tracker](https://github.com/ga-m3dv/ga-worldwind-suite/issues) and fix it!
* Implement a feature you would like to see
* Add some unit tests to the test suite
* Help create technical documentation and developer guides to make it easier for future developers to get involved

## License ##
The `ga-worldwind-suite` is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html) and is distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and limitations under the License.

## Contact ##
For more information on the `ga-worldwind-suite` project, please email *m3dv:at:ga.gov.au*.
