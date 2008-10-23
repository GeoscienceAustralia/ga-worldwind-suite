package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

import javax.swing.JFrame;

import layers.radiometry.TernaryLayer;

import quadkey.VirtualEarthLayer;

public class Application extends JFrame
{
	public static void main(String[] args)
	{
		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		
		new Application().setVisible(true);
	}
	
	public Application()
	{
		WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
		wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
		this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BasicModel model = new BasicModel();
		//model.getLayers().add(new RadLayer());
		//model.getLayers().add(new GoogleEarthLayer());
		
		VirtualEarthLayer vel = new VirtualEarthLayer();
		//vel.setMapType("a");
		model.getLayers().add(vel);
		
		vel.setEnabled(false);
		
		model.getLayers().add(new TernaryLayer());
		
		
		
		wwd.setModel(model);
		
		wwd.getSceneController().setVerticalExaggeration(10);
	}
}
