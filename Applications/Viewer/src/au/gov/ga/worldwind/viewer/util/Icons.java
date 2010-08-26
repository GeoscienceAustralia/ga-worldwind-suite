package au.gov.ga.worldwind.viewer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

public enum Icons
{
	about("about.gif"),
	add("add.gif"),
	bookmark("bookmark.gif"),
	category("category.gif"),
	check("check.gif"),
	checkall("checkall.gif"),
	checkboxes("checkboxes.gif"),
	collapse("collapse.gif"),
	collapseall("collapseall.gif"),
	compass("compass.gif"),
	crosshair45("crosshair45.gif"),
	crosshair("crosshair.gif"),
	crosshairwhite("crosshairwhite.gif"),
	datasets("datasets.gif"),
	delete("delete.gif"),
	deletevalue("deletevalue.gif"),
	down("down.gif"),
	earth32("earth32.png"),
	earth("earth.png"),
	edit("edit.gif"),
	error("error.gif"),
	escape("escape.gif"),
	exaggeration("exaggeration.png"),
	expand("expand.gif"),
	expandall("expandall.gif"),
	export("export.gif"),
	file("file.gif"),
	find("find.gif"),
	flag("flag.gif"),
	folder("folder.gif"),
	graticule("graticule.png"),
	help("help.gif"),
	hierarchy("hierarchy.gif"),
	home("home.gif"),
	image("image.gif"),
	imporrt("import.gif"),
	info("info.gif"),
	infowhite("infowhite.gif"),
	keyboard("keyboard.gif"),
	legend("legend.gif"),
	legendwhite("legendwhite.gif"),
	list("list.gif"),
	monitor("monitor.gif"),
	navigation("navigation.png"),
	newfile("newfile.gif"),
	newfolder("newfolder.gif"),
	offline("offline.gif"),
	overview("overview.gif"),
	pause("pause.gif"),
	properties("properties.gif"),
	refresh("refresh.gif"),
	reload("reload.gif"),
	remove("remove.gif"),
	run("run.gif"),
	save("save.gif"),
	scalebar("scalebar.gif"),
	screenshot("screenshot.gif"),
	search("search.gif"),
	settings("settings.gif"),
	skirts("skirts.png"),
	stop("stop.gif"),
	uncheck("uncheck.gif"),
	uncheckall("uncheckall.gif"),
	up("up.gif"),
	updown("updown.gif"),
	wireframe("wireframe.png"),
	world("world.gif"),
	zwireframe("zwireframe.png");

	private static final String ICON_DIRECTORY = "/au/gov/ga/worldwind/viewer/data/icons/";
	private ImageIcon icon;
	private URL url;

	Icons(String filename)
	{
		url = createURL(filename);
	}

	public ImageIcon getIcon()
	{
		if (icon == null)
		{
			icon = loadIcon(url);
		}
		return icon;
	}

	public URL getURL()
	{
		return url;
	}

	public static ImageIcon newLoadingIcon()
	{
		return loadIcon(createURL("progress.gif"));
	}

	private static URL createURL(String filename)
	{
		return Icons.class.getResource(ICON_DIRECTORY + filename);
	}

	private static ImageIcon loadIcon(URL url)
	{
		try
		{
			InputStream is = url.openStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) >= 0)
			{
				baos.write(buffer, 0, read);
			}
			return new ImageIcon(baos.toByteArray());
		}
		catch (IOException e)
		{
			return null;
		}
	}
}
