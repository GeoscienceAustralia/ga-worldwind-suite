package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * Utility class to convert a HTML document to an image of a specified size.
 */
public class HtmlToImage
{

	/**
	 * Create and return an image of the provided HTML document. The image will have
	 * the provided dimensions.
	 * <p/>
	 * Note: This method will not change any styles to ensure that the html document will fit inside the
	 * specified dimensions.
	 */
	public static BufferedImage createImageFromHtml(URL htmlSource, int width, int height) throws IOException
	{
		Validate.notNull(htmlSource, "A html source is required");
		Validate.isTrue(width > 0 && height > 0, "Invalid dimensions. Dimensions must be greater than 0.");
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditorKit(new HTMLEditorKit(){
			@Override
			public Document createDefaultDocument()
			{
				Document doc = super.createDefaultDocument();
		        ((HTMLDocument)doc).setAsynchronousLoadPriority(-1);
		        return doc;
			}
		});
		editorPane.setSize(width, height);
		editorPane.setPreferredSize(new Dimension(width, height));
		
		editorPane.setPage(htmlSource);
		
		editorPane.paint(image.getGraphics());
		
		return image;
	}
	
}
