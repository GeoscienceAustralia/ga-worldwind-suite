/*
 * DisplayListModel3D.java
 *
 * Created on February 27, 2008, 11:05 PM
 *
 * R. Wathelet added line 123
 *          gl.glDisable(GL.GL_TEXTURE_2D);
 */
package gov.nasa.worldwind.custom.render;

import gov.nasa.worldwind.formats.models.ResourceRetriever;
import gov.nasa.worldwind.formats.models.iModel3DRenderer;
import gov.nasa.worldwind.formats.models.geometry.Bounds;
import gov.nasa.worldwind.formats.models.geometry.Material;
import gov.nasa.worldwind.formats.models.geometry.Mesh;
import gov.nasa.worldwind.formats.models.geometry.Model;
import gov.nasa.worldwind.formats.models.geometry.Vec4;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;
import java.util.ArrayList;

/**
 * 
 * @author RodgersGB
 * 
 *         modifications made by Brian Wood and Z-Knight 
 *         modifications made by Eterna2 (added renderPickerList, etc - for display list without texture for picking in WWJ)
 *         modifications by Christian Doyle: fixed issue where picking would not work correctly if one had loaded different models
 */
public class DisplayListRenderer implements iModel3DRenderer
{

	private Logger log = Logger.getLogger(DisplayListRenderer.class.getName());
	
	private static DisplayListRenderer instance = new DisplayListRenderer();

	private DisplayListCache listCache = new DisplayListCache();

	private HashMap<Integer, Texture> texture;


	/**
	 * offset in the glCallList to access the model bounds 
	 */
	private static final int MODEL_BOUNDS_OFFSET = 1;
	
	/**
	 * offset in the glCallList to access the object bounds 
	 */
	private static final int OBJECT_BOUNDS_OFFSET = 2;
	
	/**
	 * offset in the glCallList to access a texture less object for picking
	 */
	private static final int PICKER_OFFSET = 3;
	
	private boolean isDebugging = true;


	/** Creates a new instance of DisplayListModel3D */
	public DisplayListRenderer()
	{
	}


	public static DisplayListRenderer getInstance()
	{
		return instance;
	}


	public void debug(boolean value)
	{
		this.isDebugging = value;
	}


	public void render(Object context, Model model)
	{
		GL gl = null;

		if (context instanceof GL)
		{
			gl = (GL) context;
		}
		else if (context instanceof GLAutoDrawable)
		{
			gl = ((GLAutoDrawable) context).getGL();
		}

		if (gl == null)
		{
			return;
		}

		if (model == null)
		{
			return;
		}

		int displayList = listCache.get(model);		
		
		if (displayList < 0)
		{
			displayList = initialize(gl, model);
			if (this.isDebugging)
			{
				log.info("Initialized the display list for model: " + model.getSource());
			}
		}

		// save some current state variables
		boolean isTextureEnabled = gl.glIsEnabled(GL.GL_TEXTURE_2D);
		boolean isLightingEnabled = gl.glIsEnabled(GL.GL_LIGHTING);
		boolean isMaterialEnabled = gl.glIsEnabled(GL.GL_COLOR_MATERIAL);

		// check lighting
		if (!model.isUsingLighting())
		{
			gl.glDisable(GL.GL_LIGHTING);
		}

		// check texture
		if (model.isUsingTexture())
		{
			gl.glEnable(GL.GL_TEXTURE_2D);
		}
		else
		{
			gl.glDisable(GL.GL_TEXTURE_2D);
		}

		// check wireframe
		if (model.isRenderingAsWireframe())
		{
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		}
		else
		{
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		}

		gl.glDisable(GL.GL_COLOR_MATERIAL);
		gl.glPushMatrix();

		// check for unit size model
		if (model.isUnitizeSize())
		{
			float scale = 1.0f / model.getBounds().getRadius();
			gl.glScalef(scale, scale, scale);
		}

		if (model.isCentered())
		{
			Vec4 center = model.getCenterPoint();
			gl.glTranslatef(-center.x, -center.y, -center.z);
		}

		if (model.isRenderPicker())
		{
			gl.glDisable(GL.GL_TEXTURE_2D); // added by R. Wathelet
			gl.glCallList(displayList + PICKER_OFFSET);
		}
		else
		{

			if (model.isRenderModel())
			{
				gl.glCallList(displayList);
			}

			// Disabled lighting for drawing the boundary lines so they are all white (or whatever I chose)
			gl.glDisable(GL.GL_LIGHTING);
			if (model.isRenderModelBounds())
			{
				gl.glCallList(displayList + MODEL_BOUNDS_OFFSET);
			}
			if (model.isRenderObjectBounds())
			{
				gl.glCallList(displayList + OBJECT_BOUNDS_OFFSET);
			}
		}

		gl.glPopMatrix();

		// Reset the flags back for lighting and texture
		if (isTextureEnabled)
		{
			gl.glEnable(GL.GL_TEXTURE_2D);
		}
		else
		{
			gl.glDisable(GL.GL_TEXTURE_2D);
		}

		if (isLightingEnabled)
		{
			gl.glEnable(GL.GL_LIGHTING);
		}
		else
		{
			gl.glDisable(GL.GL_LIGHTING);
		}

		if (isMaterialEnabled)
		{
			gl.glEnable(GL.GL_COLOR_MATERIAL);
		}
		else
		{
			gl.glDisable(GL.GL_COLOR_MATERIAL);
		}
	}


	/**
	 * Load the model and associated materials, etc
	 * 
	 * @param gl
	 * @param file
	 * @return
	 */
	private int initialize(GL gl, Model model)
	{
		if (this.isDebugging)
		{
			log.info("Initialize Model: " + model.getSource());
		}

		int numMaterials = model.getNumberOfMaterials();

		if (this.isDebugging && numMaterials > 0)
		{
			log.info("\n    Loading " + numMaterials + " Materials:");
		}

		String file = model.getSource();
		texture = new HashMap<Integer, Texture>();
		for (int i = 0; i < numMaterials; i++)
		{
			// TODO:DELETE THIS OLD LINE loadTexture(materials.get(i).strFile, i);
			// TODO:DELETE THIS OLD LINE materials.get(i).texureId = i;

			String subFileName = "";

			// If this is read from a jar file, then try to find the path relative to the model
			int index = file.lastIndexOf('/');
			if (index != -1)
			{
				subFileName = file.substring(0, index + 1);
			}
			else
			{
				// Else, the file path of the model was not from a jar file, so check maybe it
				// was from a local file and get that path.
				index = file.lastIndexOf('\\');

				if (index != -1)
				{
					subFileName = file.substring(0, index + 1);
				}
			}

			if (model.getMaterial(i).strFile != null)
			{
				if (this.isDebugging)
				{
					log.info("        Material:  " + subFileName + model.getMaterial(i).strFile);
				}

				URL result;
				try
				{
					result = ResourceRetriever.getResourceAsUrl(subFileName + model.getMaterial(i).strFile);
				}
				catch (IOException e)
				{
					if (this.isDebugging)
					{
						log.warning(" ... failed");
					}
					continue;
				}

				if (result != null && !result.getPath().endsWith("/") && !result.getPath().endsWith("\\"))
				{
					loadTexture(result, i);
					model.getMaterial(i).textureId = i;
					if (this.isDebugging)
					{
						log.info(" ... done. Texture ID: " + i);
					}
				}
				else if (this.isDebugging)
				{
					log.info(" ... failed (no result for material)");
				}
			}
		}

		if (this.isDebugging && numMaterials > 0)
		{
			log.info("    Load Materials: Done");
		}

		if (this.isDebugging)
		{
			log.info("\n    Generate Lists:");
		}
		int compiledList = listCache.generateList(model, gl, 4);

		if (this.isDebugging)
		{
			log.info("        Model List");
		}
		gl.glNewList(compiledList, GL.GL_COMPILE);
		genList(gl, model, true);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("        Boundary List");
		}
		gl.glNewList(compiledList + MODEL_BOUNDS_OFFSET, GL.GL_COMPILE);
		genModelBoundsList(gl, model);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("        Object Boundary List");
		}
		gl.glNewList(compiledList + OBJECT_BOUNDS_OFFSET, GL.GL_COMPILE);
		genObjectBoundsList(gl, model);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("        Picker Render List");
		}
		gl.glNewList(compiledList + PICKER_OFFSET, GL.GL_COMPILE);
		genList(gl, model, false);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("    Generate Lists: Done");
			log.info("Load Model: Done");
		}

		return compiledList;
	}


	/**
	 * Load a texture given by the specified URL and assign it to the texture id that is passed in.
	 * 
	 * @param url
	 * @param id
	 */
	private void loadTexture(URL url, int id)
	{
		if (url != null)
		{
			BufferedImage bufferedImage = null;

			try
			{
				bufferedImage = ImageIO.read(url);
			}
			catch (Exception e)
			{
				log.warning(" ... FAILED loading texture with exception: " + e.getMessage());
				return;
			}

			texture.put(id, TextureIO.newTexture(bufferedImage, true));
		}
	}


	/**
	 * Generate the model display list
	 * 
	 * @param gl
	 */
	private void genList(GL gl, Model model, boolean isFullRender)
	{
		TextureCoords coords;

		for (int i = 0; i < model.getNumberOfMeshes(); i++)
		{
			Mesh tempObj = model.getMesh(i);

			if (tempObj.numOfFaces == 0)
			{
				log.warning("Mesh: " + tempObj.name + " has no faces");
				continue;
			}

			if (tempObj.hasTexture && texture.get(tempObj.materialID) != null && isFullRender)
			{
				Texture t = texture.get(tempObj.materialID);

				// switch to texture mode and push a new matrix on the stack
				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glPushMatrix();

				// check to see if the texture needs flipping
				if (t.getMustFlipVertically())
				{
					gl.glScaled(1, -1, 1);
					gl.glTranslated(0, -1, 0);
				}

				// switch to modelview matrix and push a new matrix on the stack
				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glPushMatrix();

				// This is required to repeat textures...because some are not and so only
				// part of the model gets filled in....Might be a way to check if this is
				// required per object but I'm not sure...would need to research this.
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

				// enable, bind and get texture coordinates
				t.enable();
				t.bind();
				coords = t.getImageTexCoords();
			}

                        //sort triangles and polygons of model into separate lists to minimize glBegin/glEnd calls
                        ArrayList<Integer> triangleIndeces = new ArrayList<Integer>();
                        ArrayList<Integer> polygonIndeces = new ArrayList<Integer>();
                        for (int j=0; j<tempObj.numOfFaces; j++)
                        {
                            if (tempObj.faces[j].vertIndex.length == 3)
                                triangleIndeces.add(j);
                            else if (tempObj.faces[j].vertIndex.length > 3)
                                polygonIndeces.add(j);
                        }

                        //apply first material and save its ID, so it is applied only when changed
                        int currentMaterialID = tempObj.faces[0].materialID;
                        float[] rgba = new float[4];
                        Material firstMaterial = model.getMaterial(currentMaterialID);
                        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, firstMaterial.diffuseColor.getRGBComponents(rgba), 0);
                        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, firstMaterial.ambientColor.getRGBComponents(rgba), 0);
                        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, firstMaterial.specularColor.getRGBComponents(rgba), 0);
                        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, firstMaterial.shininess);
                        gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, firstMaterial.emissive.getRGBComponents(rgba), 0);

                        //draw triangles
                        gl.glBegin(GL.GL_TRIANGLES);
                        for (int j : triangleIndeces)
                        {
				// If the object has a texture, then do nothing till later...else
				// apply the material property to it.
				if (tempObj.hasTexture && isFullRender)
				{
					// nothing
					// Has no texture but has a material instead and this material is
					// the FACES material, and not the OBJECTS material ID as being used
					// incorrectly below...by specification, the materialID is associated
					// with a FACE and not an OBJECT
				}
				else
				{
                                        //apply material only when different from current
					if (tempObj.faces[j].materialID != currentMaterialID && tempObj.faces[j].materialID < model.getNumberOfMaterials())
					{
                                                currentMaterialID = tempObj.faces[j].materialID;

						Material material = model.getMaterial(tempObj.faces[j].materialID);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, material.diffuseColor.getRGBComponents(rgba), 0);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, material.ambientColor.getRGBComponents(rgba), 0);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, material.specularColor.getRGBComponents(rgba), 0);
						gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, material.shininess);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, material.emissive.getRGBComponents(rgba), 0);
					}
				}

				int indexType = 0;
				int vertexIndex = 0;
				int normalIndex = 0;
				int textureIndex = 0;
				// TODO: the number of vertices for a face is not always 3
				for (int whichVertex = 0; whichVertex < tempObj.faces[j].vertIndex.length; whichVertex++)
				{
					vertexIndex = tempObj.faces[j].vertIndex[whichVertex];

					try
					{
						normalIndex = tempObj.faces[j].normalIndex[whichVertex];

						indexType = 0;
						gl.glNormal3f(tempObj.normals[normalIndex].x, tempObj.normals[normalIndex].y, tempObj.normals[normalIndex].z);

						if (tempObj.hasTexture)
						{
							if (tempObj.texCoords != null)
							{
								textureIndex = tempObj.faces[j].coordIndex[whichVertex];
								indexType = 1;
								gl.glTexCoord2f(tempObj.texCoords[textureIndex].u, tempObj.texCoords[textureIndex].v);
							}
						}
						indexType = 2;
						gl.glVertex3f(tempObj.vertices[vertexIndex].x, tempObj.vertices[vertexIndex].y, tempObj.vertices[vertexIndex].z);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						switch (indexType)
						{
							case 0:
								log.warning("Normal index " + normalIndex + " is out of bounds");
								break;

							case 1:
								log.warning("Texture index " + textureIndex + " is out of bounds");
								break;

							case 2:
								log.warning("Vertex index " + vertexIndex + " is out of bounds");
								break;
						}
					}
				}
				
			}
                        gl.glEnd();

                        //draw polygons
                        for (int j : polygonIndeces)
                        {
				// If the object has a texture, then do nothing till later...else
				// apply the material property to it.
				if (tempObj.hasTexture && isFullRender)
				{
					// nothing
					// Has no texture but has a material instead and this material is
					// the FACES material, and not the OBJECTS material ID as being used
					// incorrectly below...by specification, the materialID is associated
					// with a FACE and not an OBJECT
				}
				else
				{
                                        //apply material only when different from current
					if (tempObj.faces[j].materialID != currentMaterialID && tempObj.faces[j].materialID < model.getNumberOfMaterials())
					{
                                                currentMaterialID = tempObj.faces[j].materialID;

						Material material = model.getMaterial(tempObj.faces[j].materialID);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, material.diffuseColor.getRGBComponents(rgba), 0);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, material.ambientColor.getRGBComponents(rgba), 0);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, material.specularColor.getRGBComponents(rgba), 0);
						gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, material.shininess);
						gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, material.emissive.getRGBComponents(rgba), 0);
					}
				}

				int indexType = 0;
				int vertexIndex = 0;
				int normalIndex = 0;
				int textureIndex = 0;
				gl.glBegin(GL.GL_POLYGON);
				// TODO: the number of vertices for a face is not always 3
				for (int whichVertex = 0; whichVertex < tempObj.faces[j].vertIndex.length; whichVertex++)
				{
					vertexIndex = tempObj.faces[j].vertIndex[whichVertex];

					try
					{
						normalIndex = tempObj.faces[j].normalIndex[whichVertex];

						indexType = 0;
						gl.glNormal3f(tempObj.normals[normalIndex].x, tempObj.normals[normalIndex].y, tempObj.normals[normalIndex].z);

						if (tempObj.hasTexture)
						{
							if (tempObj.texCoords != null)
							{
								textureIndex = tempObj.faces[j].coordIndex[whichVertex];
								indexType = 1;
								gl.glTexCoord2f(tempObj.texCoords[textureIndex].u, tempObj.texCoords[textureIndex].v);
							}
						}
						indexType = 2;
						gl.glVertex3f(tempObj.vertices[vertexIndex].x, tempObj.vertices[vertexIndex].y, tempObj.vertices[vertexIndex].z);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						switch (indexType)
						{
							case 0:
								log.warning("Normal index " + normalIndex + " is out of bounds");
								break;

							case 1:
								log.warning("Texture index " + textureIndex + " is out of bounds");
								break;

							case 2:
								log.warning("Vertex index " + vertexIndex + " is out of bounds");
								break;
						}
					}
				}
                                gl.glEnd();
			}

			if (tempObj.hasTexture && isFullRender)
			{
				Texture t = texture.get(tempObj.materialID);
				if (t != null)
				{
					t.disable();
				}

				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glPopMatrix();

				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glPopMatrix();
			}
		}

		// Try this clearing of color so it won't use the previous color
		gl.glColor3f(1.0f, 1.0f, 1.0f);
	}


	/**
	 * Render the desired object of the model (specified by an id number of the object)
	 * 
	 * @param gl
	 * @param id
	 */
	public void renderBoundsOfObject(GL gl, int id, Model model)
	{
		if (id >= 0 && id <= model.getNumberOfMeshes())
		{
			if (model.getMesh(id).bounds != null)
			{
				drawBounds(gl, model.getMesh(id).bounds);
			}
		}
	}


	/**
	 * Draw the boundary of the model (the large box representing the entire model and not the object in it)
	 * 
	 * @param gLDrawable
	 */
	private void genModelBoundsList(GLAutoDrawable gLDrawable, Model model)
	{
		GL gl = gLDrawable.getGL();
		drawBounds(gl, model.getBounds());
	}


	/**
	 * Draw the boundary of the model (the large box representing the entire model and not the object in it)
	 * 
	 * @param gl
	 */
	private void genModelBoundsList(GL gl, Model model)
	{
		drawBounds(gl, model.getBounds());
	}


	/**
	 * Draw the boundaries over all of the objects of the model
	 * 
	 * @param gLDrawable
	 */
	private void genObjectBoundsList(GLAutoDrawable gLDrawable, Model model)
	{
		GL gl = gLDrawable.getGL();
		genObjectBoundsList(gl, model);
	}


	/**
	 * Draw the boundaries over all of the objects of the model
	 * 
	 * @param gl
	 */
	private void genObjectBoundsList(GL gl, Model model)
	{
		for (int i = 0; i < model.getNumberOfMeshes(); i++)
		{
			if (model.getMesh(i).bounds != null)
			{
				drawBounds(gl, model.getMesh(i).bounds);
			}
		}
	}


	/**
	 * Draws the bounding box of the object using the max and min extrema points.
	 * 
	 * @param gl
	 * @param bounds
	 */
	private void drawBounds(GL gl, Bounds bounds)
	{
		// Front Face
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.min.z);
		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.min.z);
		gl.glEnd();

		// Back Face
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.max.z);
		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.max.z);
		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.max.z);
		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.max.z);
		gl.glEnd();

		// Connect the corners between the front and back face.
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.max.z);

		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.max.z);

		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.max.z);

		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.min.z);
		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.max.z);
		gl.glEnd();
	}


	/**
	 * Convert an Unsigned byte to integer
	 * 
	 * @param b
	 * @return
	 */
	public int unsignedByteToInt(byte b)
	{
		return b & 0xFF;
	}


	/**
	 * Convert integer to float
	 * 
	 * @param i
	 * @return
	 */
	public float intToFloat(int i)
	{
		return i / 255.0f;
	}

	public class DisplayListCache
	{

		private HashMap<Object, Integer> listCache;


		/** Creates a new instance of WWDisplayListCache */
		private DisplayListCache()
		{
			listCache = new HashMap<Object, Integer>();
		}


		public void clear()
		{
			listCache.clear();
		}


		public int get(Object objID)
		{
			if (listCache.containsKey(objID))
			{
				return listCache.get(objID);
			}
			else
			{
				return -1;
			}
		}


		public void remove(Object objID, GL gl, int howMany)
		{
			Integer list = listCache.get(objID);

			if (list != null)
			{
				gl.glDeleteLists(list, howMany);
			}

			listCache.remove(objID);
		}


		/**
		 * Returns an integer identifier for an OpenGL display list based on the object being passed in. If the object
		 * already has a display list allocated, the existing ID is returned.
		 */
		public int generateList(Object objID, GL gl, int howMany)
		{
			Integer list = null;

			list = listCache.get(objID);
			if (list == null)
			{
				list = new Integer(gl.glGenLists(howMany));
				listCache.put(objID, list);
			}

			return list;
		}
	}
}
