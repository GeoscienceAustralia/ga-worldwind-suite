/**
 * Copyright (c) 2008-2010 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package gov.nasa.worldwind.formats.models.loader;

import gov.nasa.worldwind.formats.models.ModelLoadException;
import gov.nasa.worldwind.formats.models.geometry.Model;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.io.File;
import java.net.URL;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.jogl.JoglContextCapabilities;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

/**
 * Simplest example of loading a Collada model.
 */

public class ArdorColladaLoader implements iLoader{
    
    
    public static Node loadColladaModel(String modelFileStr) throws Exception {
    	
        final Node root = new Node( "rootNode" );

        String modelDirStr = new File(modelFileStr).getParent();
        String modelNameStr = new File(modelFileStr).getName();

        File modelDir = new File(modelDirStr);
        modelDirStr = modelDir.getAbsolutePath();
        
        ColladaImporter importer = new ColladaImporter();

        SimpleResourceLocator modelLocator = new SimpleResourceLocator(new URL("file:" + modelDirStr));
        SimpleResourceLocator textureLocator = new SimpleResourceLocator(new URL("file:" + modelDirStr));
        importer.setModelLocator(modelLocator);
        importer.setTextureLocator(textureLocator);

        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, modelLocator);
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, textureLocator);
        
        ColladaStorage storage =  importer.load(modelNameStr);
        root.attachChild(storage.getScene());

        root.updateGeometricState(0);
        return root;
    }
    
    public static void initializeArdorSystem(final DrawContext dc) {

        if (ContextManager.getContextForKey("HACKED CONTEXT") != null) {
        	RenderContext rc = ContextManager.switchContext("HACKED CONTEXT");
                return;
        } 

            Logging.logger().info("ARDOR INITIALIZER -->>  initializeArdorSystem");

            final JoglContextCapabilities caps = new JoglContextCapabilities(dc.getGL());
            final RenderContext rc = new RenderContext(dc.getGLContext(), caps);

            ContextManager.addContext("HACKED CONTEXT", rc);
            ContextManager.switchContext("HACKED CONTEXT");
            Camera cam = new Camera() {
                @Override
                public FrustumIntersect contains(BoundingVolume bound) {
                    return FrustumIntersect.Inside;
                }
            };
            ContextManager.getCurrentContext().setCurrentCamera(cam);
            AWTImageLoader.registerLoader();
    }

	@Override
	public Model load(String path) throws ModelLoadException {
		// TODO Auto-generated method stub
		Model model = new Model(path);
		return model;
	}
}