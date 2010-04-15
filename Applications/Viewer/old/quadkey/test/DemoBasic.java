/* Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package quadkey.test;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.layers.Earth.BMNGSurfaceLayer;
import gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3;
import gov.nasa.worldwind.layers.Earth.USGSDigitalOrtho;
import gov.nasa.worldwind.layers.Earth.USGSUrbanAreaOrtho;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import quadkey.GoogleEarthLayer;
import quadkey.GoogleRoadsLayer;
import quadkey.VirtualEarthLayer;

/**
 * @author tag
 * @version $Id: WWPieceMaker.java 1764 2007-05-07 20:01:57Z tgaskins $
 */
public class DemoBasic
{

    private static class AppFrame extends JFrame
    {
		private static final long serialVersionUID = 112429964534231109L;

		private final WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
    	
        private DemoBasic.LayerAction[] layers = new DemoBasic.LayerAction[] {
            new DemoBasic.LayerAction(new BMNGSurfaceLayer(), true, wwd),
            new DemoBasic.LayerAction(new LandsatI3(), false, wwd),
            new DemoBasic.LayerAction(new USGSDigitalOrtho(), false, wwd),
            new DemoBasic.LayerAction(new USGSUrbanAreaOrtho(), false, wwd),
            new DemoBasic.LayerAction(new EarthNASAPlaceNameLayer(), true, wwd),
            new DemoBasic.LayerAction(new CompassLayer(), false, wwd),
            new DemoBasic.LayerAction(new VirtualEarthLayer(), false, wwd),
            new DemoBasic.LayerAction(new GoogleEarthLayer(), false, wwd),
            new DemoBasic.LayerAction(new GoogleRoadsLayer(), false, wwd)
        };
    	
        public AppFrame() //DemoBasic.LayerAction[] layers)
        {
            LayerList layerList = new LayerList();

            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            m.setShowWireframeExterior(false);
            m.setShowWireframeInterior(false);
            m.setShowTessellationBoundingVolumes(false);
            m.setLayers(layerList);
            wwd.setModel(m);
            
            try
            {
                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BorderLayout());
                wwd.setPreferredSize(new Dimension(800, 600));
                mainPanel.add(wwd, BorderLayout.CENTER);

                this.getContentPane().add(mainPanel, BorderLayout.CENTER);
            
           	
                JPanel westContainer = new JPanel(new BorderLayout());
                {
                    JPanel westPanel = new JPanel(new GridLayout(0, 1, 0, 10));
                    westPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
                    {
                        JPanel layersPanel = new JPanel(new GridLayout(0, 1, 0, 15));
                        layersPanel.setBorder(new TitledBorder("Layers"));
                        
                        for (DemoBasic.LayerAction action : layers)
                        {
                            JCheckBox jcb = new JCheckBox(action);
                            jcb.setSelected(action.selected);
                            layersPanel.add(jcb);
                            layerList.add(action.layer);

                            if (action.layer instanceof TiledImageLayer)
                                ((TiledImageLayer) action.layer).setShowImageTileOutlines(false);

                            if (action.layer instanceof LandsatI3)
                                ((TiledImageLayer) action.layer).setDrawBoundingVolumes(false);

                            if (action.layer instanceof USGSDigitalOrtho)
                                ((TiledImageLayer) action.layer).setDrawTileIDs(false);
                        }
                        
                        
                        westPanel.add(layersPanel);
                        westContainer.add(westPanel, BorderLayout.NORTH);
                    }
                }

                this.getContentPane().add(westContainer, BorderLayout.WEST);
                this.pack();

                Dimension prefSize = this.getPreferredSize();
                prefSize.setSize(prefSize.getWidth(), 1.1 * prefSize.getHeight());
                this.setSize(prefSize);

                // Center the app on the user's screen.
                Dimension parentSize;
                Point parentLocation = new Point(0, 0);
                parentSize = Toolkit.getDefaultToolkit().getScreenSize();
                int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
                int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
                this.setLocation(x, y);
                this.setResizable(true);

                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    
    private static class LayerAction extends AbstractAction
    {
		private static final long serialVersionUID = 7975749480372810776L;
		private Layer layer;
        private boolean selected;
        private WorldWindowGLCanvas canvas;
        
        public LayerAction(Layer layer, boolean selected, WorldWindowGLCanvas canvas)
        {
            super(layer.getName());
            this.layer = layer;
            this.selected = selected; 
            this.layer.setEnabled(this.selected);
            this.canvas = canvas;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	
            if (((JCheckBox) actionEvent.getSource()).isSelected()) 
            {
                this.layer.setEnabled(true);
            }
            else {
                this.layer.setEnabled(false);
            }

            appFrame.wwd.redraw(); //repaint();
        }
    }

    static
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Basic Demo");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    private static DemoBasic.AppFrame appFrame;

    public static void main(String[] args)
    {
        System.out.println("Java run-time version: " + System.getProperty("java.version"));
        System.out.println(gov.nasa.worldwind.Version.getVersion());

        try
        {
            //DemoBasic demo = new DemoBasic();
            appFrame = new DemoBasic.AppFrame(); //demo.layers);
            appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            appFrame.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
