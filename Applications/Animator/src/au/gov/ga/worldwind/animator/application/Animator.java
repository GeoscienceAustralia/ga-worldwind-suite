package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.examples.sunlight.LensFlareLayer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GLCapabilities;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.layers.camerapath.CameraPathLayer;
import au.gov.ga.worldwind.animator.layers.depth.DepthLayer;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ExtendedBasicElevationModel;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ExtendedBasicElevationModelFactory;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ExtendedElevationModel;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.shaded.ShadedElevationLayer;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.shadows.ShadowsElevationLayer;
import au.gov.ga.worldwind.animator.layers.elevation.pervetex.ElevationTesselator;
import au.gov.ga.worldwind.animator.layers.file.FileLayer;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateRetrievalService;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateTaskService;
import au.gov.ga.worldwind.animator.layers.metacarta.MetacartaCoastlineLayer;
import au.gov.ga.worldwind.animator.layers.metacarta.MetacartaStateBoundariesLayer;
import au.gov.ga.worldwind.animator.layers.other.GravityLayer;
import au.gov.ga.worldwind.animator.layers.other.ImmediateBMNGWMSLayer;
import au.gov.ga.worldwind.animator.layers.other.ImmediateLandsatI3WMSLayer;
import au.gov.ga.worldwind.animator.layers.other.MagneticsLayer;
import nasa.worldwind.awt.WorldWindowGLCanvas;
import au.gov.ga.worldwind.animator.terrain.DetailedElevationModel;
import au.gov.ga.worldwind.animator.util.ChangeFrameListener;
import au.gov.ga.worldwind.animator.util.FileUtil;
import au.gov.ga.worldwind.animator.util.FrameSlider;
import au.gov.ga.worldwind.animator.view.roll.BasicRollOrbitView;
import au.gov.ga.worldwind.animator.animation.Animation;

public class Animator
{
	private final static String DATA_DRIVE = "Z";

	private static final GLCapabilities caps = new GLCapabilities();

	static
	{
		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setGreenBits(8);
		caps.setBlueBits(8);
		caps.setDepthBits(24);
		caps.setSampleBuffers(true);
		caps.setDoubleBuffered(true);
		caps.setNumSamples(4);
	}

	static
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name",
					"World Wind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes",
					"false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		}

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.nonProxyHosts", "localhost");
	}

	public static void main(String[] args)
	{
		new Animator();
	}

	private JFrame frame;

	private WorldWindowGLCanvas wwd;
	private FrameSlider slider;
	private Animation animation = null;
	private File file = null;
	private boolean autokey = false;
	private boolean applying = false;
	private Updater updater;
	private boolean changed = false;
	private boolean stop = false;
	private boolean settingSlider = false;
	private ChangeListener animationChangeListener;
	private Layer crosshair;
	private DetailedElevationModel dem;
	private LensFlareLayer lensFlare;
	private JCheckBoxMenuItem useScaledZoomCheck;

	private Vec4 sunPosition = new Vec4(0.0, 0.0,
			Earth.WGS84_EQUATORIAL_RADIUS * 2);
	private JSlider xRotate, yRotate, zRotate;

	private ShadedElevationLayer elevationEarth, elevationSW, elevationSONNE;
	private ShadowsElevationLayer shadowsEarth, shadowsSW;

	private Layer bmng, landsat;

	public Animator()
	{
		// ImmediateMode.setImmediate(true);

		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, BasicRollOrbitView.class
				.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME,
				AnimatorSceneController.class.getName());

		Configuration.setValue(AVKey.TASK_SERVICE_CLASS_NAME,
				ImmediateTaskService.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME,
				ImmediateRetrievalService.class.getName());

		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME,
				ElevationTesselator.class.getName());

		Configuration.setValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE,
				16777216L * 8); // 128 mb

		animationChangeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				changed = true;
				setTitleBar();
			}
		};

		animation = new Animation();
		resetChanged();
		updater = new Updater();

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				quit();
			}
		});

		frame.setLayout(new BorderLayout());
		wwd = new WorldWindowGLCanvas(caps);
		Model model = new BasicModel();
		wwd.setModel(model);
		setAnimationSize(1024, 576);
		frame.add(wwd, BorderLayout.CENTER);
		((AWTInputHandler) wwd.getInputHandler()).setSmoothViewChanges(false);
		((OrbitView) wwd.getView()).getOrbitViewLimits().setPitchLimits(
				Angle.ZERO, Angle.POS180);

		CompoundElevationModel cem = new CompoundElevationModel();
		dem = new DetailedElevationModel(cem);
		model.getGlobe().setElevationModel(dem);

		// tesselator.setMakeTileSkirts(false);
		// model.setShowWireframeInterior(true);
		wwd.getSceneController().setVerticalExaggeration(10);
		// ocem.setElevationOffset(200);

		LayerList layers = model.getLayers();

		StarsLayer stars = new StarsLayer();
		layers.add(stars);

		/*Skybox skybox = new Skybox();
		layers.add(skybox);
		Skysphere skysphere = new Skysphere();
		layers.add(skysphere);

		Vec4 direction = new Vec4(0.32, 0.9, -0.3, 1.0);
		lensFlare = LensFlareLayer
				.getPresetInstance(LensFlareLayer.PRESET_NOSUN);
		layers.add(lensFlare);
		lensFlare.setSunDistance(lensFlare.getSunDistance() * 100);
		lensFlare.setSunDirection(direction);*/

		SkyGradientLayer sky = new SkyGradientLayer();
		layers.add(sky);

		/*Color fogColor = new Color(138, 82, 57);
		FogLayer fog = new FogLayer();
		layers.add(fog);
		fog.setNearFactor(0.8f);
		fog.setFarFactor(0.4f);
		fog.setColor(fogColor);*/

		Layer depth = new DepthLayer();
		layers.add(depth);

		/*float[] fogColorf = fogColor.getComponents(new float[4]);
		sky.setHorizonColor(new Color(fogColorf[0], fogColorf[1], fogColorf[2], 1.0f));
		sky.setZenithColor(new Color(fogColorf[0], fogColorf[1], fogColorf[2], 1.0f));
		sky.setAtmosphereThickness(300);*/


		/*JFrame flareframe = new JFrame("lensflare");
		flareframe.setLayout(new GridLayout(0, 3));
		JButton button = new JButton("X");
		flareframe.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Vec4 sunDirection = lensFlare.getSunDirection();
				Matrix transform = Matrix.fromRotationX(Angle.fromDegrees(10));
				sunDirection = sunDirection.transformBy4(transform);
				lensFlare.setSunDirection(sunDirection);
				System.out.println(sunDirection);
				wwd.redraw();
			}
		});
		button = new JButton("Y");
		flareframe.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Vec4 sunDirection = lensFlare.getSunDirection();
				Matrix transform = Matrix.fromRotationY(Angle.fromDegrees(10));
				sunDirection = sunDirection.transformBy4(transform);
				lensFlare.setSunDirection(sunDirection);
				System.out.println(sunDirection);
				wwd.redraw();
			}
		});
		button = new JButton("Z");
		flareframe.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Vec4 sunDirection = lensFlare.getSunDirection();
				Matrix transform = Matrix.fromRotationZ(Angle.fromDegrees(10));
				sunDirection = sunDirection.transformBy4(transform);
				lensFlare.setSunDirection(sunDirection);
				System.out.println(sunDirection);
				wwd.redraw();
			}
		});
		flareframe.pack();
		flareframe.setVisible(true);*/

		bmng = new ImmediateBMNGWMSLayer();
		layers.add(bmng);

		landsat = new ImmediateLandsatI3WMSLayer();
		layers.add(landsat);

		ElevationModel earthem = (ElevationModel) new ExtendedBasicElevationModelFactory()
				.createFromConfigSource(
						"config/Earth/EarthElevationModelAsBil16.xml", null);
		ExtendedElevationModel eem = getEBEM(earthem);

		Sector bemSector = Sector.fromDegrees(-59.99875, -7.99875, 91.99875,
				171.99875);
		ExtendedBasicElevationModel bem = FileLayer.createElevationModel(
				"SW Margins DEM", "GA/SW Margins DEM", new File(DATA_DRIVE
						+ ":/SW Margins/bathy/tiled"), 7, 150, LatLon
						.fromDegrees(20d, 20d), bemSector, -8922, 3958, AVKey.INT16);
		bem.setMissingDataSignal(-32768);

		Sector semSector = Sector.fromDegrees(-27.0015, -22.5005, 106.5005,
				110.5025);
		ExtendedBasicElevationModel sem = FileLayer.createElevationModel(
				"SONNE DEM", "GA/SONNE DEM", new File(DATA_DRIVE
						+ ":/SW Margins/sonne/tiledB"), 7, 150, LatLon
						.fromDegrees(20d, 20d), semSector, -6400, -2310, AVKey.INT16);
		sem.setMissingDataSignal(-9999);

		cem.addElevationModel(eem);
		cem.addElevationModel(bem);
		// ocem.addElevationModel(sem);

		/*map1 = FileLayer.createLayer("WestMac Map Page 1",
				"GA/WestMac Map Page 1", ".dds", new File(DATA_DRIVE
						+ ":/West Macs Imagery/Rectified Map/5 Tiles/page1"),
				"png", 13, LatLon.fromDegrees(36d, 36d), Sector.fromDegrees(
						-24.0536281, -23.4102781, 132.0746805, 133.9779805));
		layers.add(map1);*/

		sunPosition = new Vec4(7649434.404798721, 4779897.118999491,
				-9020047.848073645, 0.0);

		elevationEarth = new ShadedElevationLayer(eem);
		elevationEarth.setSunPosition(sunPosition);
		elevationEarth.setExaggeration(50);
		elevationEarth.setMaxElevationClamp(0);
		elevationEarth.setMinElevationClamp(bem.getMinElevation());
		layers.add(elevationEarth);

		Sector belevSector = new Sector(bemSector.getMinLatitude()
				.addDegrees(1), bemSector.getMaxLatitude().addDegrees(-1),
				bemSector.getMinLongitude().addDegrees(1), bemSector
						.getMaxLongitude().addDegrees(-1));
		elevationSW = new ShadedElevationLayer(bem, belevSector);
		elevationSW.setSunPosition(sunPosition);
		elevationSW.setExaggeration(50);
		elevationSW.setSplitScale(1.65);
		elevationSW.setMaxElevationClamp(0);
		elevationSW.setMinElevationClamp(bem.getMinElevation());
		layers.add(elevationSW);

		Sector selevSector = new Sector(semSector.getMinLatitude(), semSector
				.getMaxLatitude(), semSector.getMinLongitude().addDegrees(0.1),
				semSector.getMaxLongitude());
		elevationSONNE = new ShadedElevationLayer(sem, selevSector);
		elevationSONNE.setSunPosition(sunPosition);
		elevationSONNE.setExaggeration(50);
		elevationSONNE.setSplitScale(1.65);
		elevationSONNE.setMaxElevationClamp(0);
		elevationSONNE.setMinElevationClamp(bem.getMinElevation());
		elevationSONNE.setShaderMaxElevation(bem.getMaxElevation());
		elevationSONNE.setShaderMinElevation(bem.getMinElevation());
		layers.add(elevationSONNE);

		shadowsEarth = new ShadowsElevationLayer(eem);
		shadowsEarth.setSunPosition(sunPosition);
		shadowsEarth.setOpacity(0.5);
		layers.add(shadowsEarth);

		shadowsSW = new ShadowsElevationLayer(bem, belevSector);
		shadowsSW.setSunPosition(sunPosition);
		shadowsSW.setOpacity(0.5);
		shadowsSW.setSplitScale(1.3);
		layers.add(shadowsSW);

		Layer gravity = new GravityLayer();
		layers.add(gravity);

		Layer magnetics = new MagneticsLayer();
		layers.add(magnetics);

		MetacartaStateBoundariesLayer state = new MetacartaStateBoundariesLayer();
		state.setSplitScale(1.2);
		layers.add(state);

		MetacartaCoastlineLayer coast = new MetacartaCoastlineLayer();
		coast.setSplitScale(1.2);
		layers.add(coast);

		
//		CameraPathLayer cameraPathLayer = new CameraPathLayer(animation);
//		layers.add(cameraPathLayer);
		
		/*Landmarks landmarks = new Landmarks(model.getGlobe());
		layers.add(landmarks);*/

		for (Layer layer : layers)
		{
			layer.setEnabled(false);
		}

		// AnnotationAttributes townAttr = new AnnotationAttributes();
		// townAttr.setDefaults(defaultAttributes);
		// townAttr.setFont(Font.decode("Arial-BOLD-12"));

		// AnnotationLayer annos = new AnnotationLayer();
		// annos.addAnnotation(new GlobeAnnotation("Margaret River",
		// Position.fromDegrees(-33.7340, 115.0000, 0), townAttr));

		// layers.add(annos);


		//stars.setEnabled(true);
		sky.setEnabled(true);

		//depth.setEnabled(true);
		//bmng.setEnabled(true);

		landsat.setEnabled(true);

		//elevationEarth.setEnabled(true);
		//elevationSW.setEnabled(true);
		//elevationSONNE.setEnabled(true);

//		cameraPathLayer.setEnabled(true);

		// shadowsEarth.setEnabled(true);
		// shadowsSW.setEnabled(true);

		// gravity.setEnabled(true);
		// magnetics.setEnabled(true);

		// coast.setEnabled(true);
		// state.setEnabled(true);
		// landmarks.setEnabled(true);

		// lensFlare.setEnabled(true);
		// skybox.setEnabled(true);
		// fog.setEnabled(true);
		// skysphere.setEnabled(true);
		/*
				ShapefileLayer seismicShp = new ShapefileLayer();
				seismicShp.setColor(Color.yellow);
				seismicShp.setUsePolyline(true);
				seismicShp.setLineWidth(2.0);
				seismicShp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/seismic.shp"));
				layers.add(seismicShp);

				ShapefileLayer studyShp = new ShapefileLayer();
				studyShp.setColor(Color.white);
				studyShp.setLineWidth(2.0);
				//studyShp.setOpacity(0.5);
				studyShp.setUsePolyline(true);
				studyShp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/study_areas.shp"));
				layers.add(studyShp);*/

		/*		ShapefileLayer eezShp = new ShapefileLayer();
				eezShp.setColor(Color.green);
				eezShp.setFollowTerrain(true);
				eezShp.setLineWidth(1.0);
				//eezShp.setOpacity(0.4);
				eezShp.setUsePolyline(false);
				eezShp.setRemoveDetailLevels(1);
				eezShp.loadFile(new File(DATA_DRIVE
						+ ":/SW Margins/edited_data/eez_limit.shp"));
				layers.add(eezShp); */

		/*	ShapefileLayer eezShp2 = new ShapefileLayer();
			eezShp2.setColor(Color.green);
			eezShp2.setFollowTerrain(false);
			eezShp2.setLineWidth(2.5);		
			eezShp2.setUsePolyline(true);
			eezShp2.setRemoveDetailLevels(1);
			eezShp2.loadFile(new File(DATA_DRIVE
					+ ":/SW Margins/edited_data/eez_limit.shp"));
			layers.add(eezShp2);*/
		/*	
			ShapefileLayer extendedShp2 = new ShapefileLayer();
			extendedShp2.setColor(Color.yellow);
			extendedShp2.setFollowTerrain(true);
			extendedShp2.setLineWidth(1.0);
			//extendedShp2.setOpacity(0.4);
			extendedShp2.setUsePolyline(false);
			extendedShp2.setRemoveDetailLevels(1);		
			//extendedShp2.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/ecs.shp"));
			extendedShp2.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/ecs_line_minus_eez_limit.shp"));
			layers.add(extendedShp2);		*/


		/*		ShapefileLayer extendedShp = new ShapefileLayer();
				extendedShp.setColor(Color.magenta);
				extendedShp.setFollowTerrain(true);
				extendedShp.setLineWidth(2.5);
				//extendedShp.setOpacity(0.5);
				extendedShp.setRemoveDetailLevels(1);
				extendedShp.setUsePolyline(true);
				extendedShp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/ecs_line_minus_eez_limit.shp"));
				//extendedShp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/ecs.shp"));
				layers.add(extendedShp);  */


		/*ShapefileLayer swath1Shp = new ShapefileLayer();
		swath1Shp.setColor(Color.white);
		swath1Shp.setFollowTerrain(false);
		swath1Shp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/swath_leg1.shp"));
		layers.add(swath1Shp);
		
		ShapefileLayer swath2Shp = new ShapefileLayer();
		swath2Shp.setColor(Color.white);
		swath2Shp.setFollowTerrain(false);
		swath2Shp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/swath_leg2.shp"));
		layers.add(swath2Shp);
		
		ShapefileLayer swath3Shp = new ShapefileLayer();
		swath3Shp.setColor(Color.white);
		swath3Shp.setFollowTerrain(false);
		swath3Shp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/swath_leg3.shp"));
		layers.add(swath3Shp);*/

		/*ShapefileLayer coastlineShp = new ShapefileLayer();
		coastlineShp.setColor(Color.white);
		coastlineShp.setFollowTerrain(true);
		coastlineShp.setLineWidth(2.0);
		coastlineShp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/coastline.shp"));
		layers.add(coastlineShp);*/

		/*ShapefileLayer basinsShp = new ShapefileLayer();
		basinsShp.setColor(Color.white);
		basinsShp.setFollowTerrain(true);
		basinsShp.setLineWidth(2.0);
		basinsShp.setUsePolyline(true);
		basinsShp.loadFile(new File(DATA_DRIVE + ":/SW Margins/vector/OffhoreSedBasinsNov08.shp"));
		basinsShp.setRemoveDetailLevels(5);
		layers.add(basinsShp);*/


		crosshair = new CrosshairLayer();
		layers.add(crosshair);

		JPanel bottom = new JPanel(new BorderLayout());
		frame.add(bottom, BorderLayout.SOUTH);

		slider = new FrameSlider(0, 0, animation.getFrameCount());
		slider.setMinimumSize(new Dimension(0, 54));
		bottom.add(slider, BorderLayout.CENTER);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if (!settingSlider)
				{
					if (animation.size() > 0)
					{
						applyView(getView());
						wwd.redraw();
					}
					stop = true;
				}
			}
		});
		slider.addChangeFrameListener(new ChangeFrameListener()
		{
			public void frameChanged(int index, int oldFrame, int newFrame)
			{
				animation.setFrame(index, newFrame);
				updateSlider();
				applyView(getView());
				wwd.redraw();
			}
		});

		/*JScrollPane scrollPane = new JScrollPane(slider,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(null);
		bottom.add(scrollPane, BorderLayout.CENTER);
		
		Dimension sps = slider.getMinimumSize();
		sps.width *= 2;
		slider.setMinimumSize(sps);*/

		getView().addPropertyChangeListener(AVKey.VIEW,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent evt)
					{
						if (autokey && !applying)
						{
							addFrame();
						}
					}
				});

		StatusBar statusBar = new StatusBar();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		bottom.add(statusBar, BorderLayout.SOUTH);
		statusBar.setEventSource(wwd);

		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		createMenuBar();

		setTitleBar();
		updateSlider();

		frame.pack();
		frame.setVisible(true);


		/*JFrame sliders = new JFrame("sliders");
		sliders.setLayout(new GridLayout(0, 1));
		JSlider slider;

		slider = new JSlider(-180, 180, 0);
		sliders.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				((RollOrbitView) wwd.getView()).setHeading(Angle
						.fromDegrees(((JSlider) e.getSource()).getValue()));
				wwd.redraw();
			}
		});
		
		slider = new JSlider(0, 180, 0);
		sliders.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				((RollOrbitView) wwd.getView()).setPitch(Angle
						.fromDegrees(((JSlider) e.getSource()).getValue()));
				wwd.redraw();
			}
		});
		
		slider = new JSlider(-180, 180, 0);
		sliders.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				((RollOrbitView) wwd.getView()).setRoll(Angle
						.fromDegrees(((JSlider) e.getSource()).getValue()));
				wwd.redraw();
			}
		});
		
		sliders.setSize(640, 480);
		sliders.setVisible(true);*/

		/*JFrame exaggeration = new JFrame("exaggeration");
		exaggeration.setLayout(new GridLayout(0, 1));
		JSlider slider;

		slider = new JSlider(0, 100, (int) wwd.getSceneController()
				.getVerticalExaggeration());
		exaggeration.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				int value = ((JSlider) e.getSource()).getValue();
				Configuration.setValue(AVKey.VERTICAL_EXAGGERATION,
						(double) value);
				wwd.getSceneController().setVerticalExaggeration(value);
				wwd.redraw();
			}
		});

		slider = new JSlider(0, 100, (int) elevationSW.getExaggeration());
		exaggeration.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				int value = ((JSlider) e.getSource()).getValue();
				elevationEarth.setExaggeration(value);
				elevationSW.setExaggeration(value);
				elevationSONNE.setExaggeration(value);
				wwd.redraw();
			}
		});

		slider = new JSlider(1, 1000, (int) (shadowsSW.getScale() * 100.0));
		exaggeration.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				int value = ((JSlider) e.getSource()).getValue();
				shadowsSW.setScale(value / 100.0);
				shadowsEarth.setScale(value / 100.0);
				wwd.redraw();
			}
		});

		slider = new JSlider(-1000, 1000, (int) (shadowsSW.getBias() * 1000.0));
		exaggeration.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				int value = ((JSlider) e.getSource()).getValue();
				shadowsSW.setBias(value / 1000.0);
				shadowsEarth.setBias(value / 1000.0);
				wwd.redraw();
			}
		});

		xRotate = new JSlider(-180, 180, 0);
		exaggeration.add(xRotate);
		xRotate.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				rotateSunPosition(xRotate.getValue(), yRotate.getValue(),
						zRotate.getValue());
			}
		});

		yRotate = new JSlider(-180, 180, 0);
		exaggeration.add(yRotate);
		yRotate.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				rotateSunPosition(xRotate.getValue(), yRotate.getValue(),
						zRotate.getValue());
			}
		});

		zRotate = new JSlider(-180, 180, 0);
		exaggeration.add(zRotate);
		zRotate.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				rotateSunPosition(xRotate.getValue(), yRotate.getValue(),
						zRotate.getValue());
			}
		});

		exaggeration.setSize(640, 200);
		exaggeration.setVisible(true);*/
	}

	private void rotateSunPosition(double x, double y, double z)
	{
		Quaternion q = Quaternion.fromRotationXYZ(Angle.fromDegrees(x), Angle
				.fromDegrees(y), Angle.fromDegrees(z));
		Vec4 newPosition = sunPosition.transformBy3(q);
		elevationEarth.setSunPosition(newPosition);
		elevationSW.setSunPosition(newPosition);
		elevationSONNE.setSunPosition(newPosition);
		shadowsEarth.setSunPosition(newPosition);
		shadowsSW.setSunPosition(newPosition);
		wwd.redraw();

		System.out.println("New sun position = " + newPosition);
	}

	private ExtendedElevationModel getEBEM(ElevationModel elevationModel)
	{
		if (elevationModel instanceof ExtendedElevationModel)
		{
			return (ExtendedElevationModel) elevationModel;
		}
		else if (elevationModel instanceof CompoundElevationModel)
		{
			CompoundElevationModel cem = (CompoundElevationModel) elevationModel;
			for (ElevationModel model : cem.getElevationModels())
			{
				ExtendedElevationModel ebem = getEBEM(model);
				if (ebem != null)
				{
					return ebem;
				}
			}
		}
		return null;
	}

	private void setAnimationSize(int width, int height)
	{
		if (!frame.isVisible())
		{
			Dimension wwdSize = new Dimension(width, height);
			wwd.setMinimumSize(wwdSize);
			wwd.setMaximumSize(wwdSize);
			wwd.setPreferredSize(wwdSize);
			wwd.setSize(wwdSize);
			frame.pack();
		}
		else
		{
			Dimension wwdSize = wwd.getSize();
			Dimension frameSize = frame.getSize();
			int deltaWidth = frameSize.width - wwdSize.width;
			int deltaHeight = frameSize.height - wwdSize.height;

			frameSize = new Dimension(width + deltaWidth, height + deltaHeight);
			frame.setPreferredSize(frameSize);
			frame.setMinimumSize(frameSize);
			frame.setMaximumSize(frameSize);
			frame.setSize(frameSize);
			frame.pack();

			wwdSize = wwd.getSize();
			if (wwdSize.width != width || wwdSize.height != height)
			{
				JOptionPane.showMessageDialog(frame,
						"Could not set animation dimensions to " + width + "x"
								+ height + " (currently " + wwdSize.width + "x"
								+ wwdSize.height + ")",
						"Could not set dimensions", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("File");
		menu.setMnemonic('F');
		menuBar.add(menu);

		menuItem = new JMenuItem("New");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('N',
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('N');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newFile();
			}
		});

		menuItem = new JMenuItem("Open...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('O',
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('O');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				open();
			}
		});

		menuItem = new JMenuItem("Save");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('S',
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('S');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				save();
			}
		});

		menuItem = new JMenuItem("Save As...");
		menuItem.setMnemonic('A');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveAs();
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Exit");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				ActionEvent.ALT_MASK));
		menuItem.setMnemonic('x');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				quit();
			}
		});


		menu = new JMenu("Frame");
		menu.setMnemonic('r');
		menuBar.add(menu);

		menuItem = new JMenuItem("Add key");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		menuItem.setMnemonic('A');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addFrame();
			}
		});

		menuItem = new JMenuItem("Delete key");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menuItem.setMnemonic('D');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int index = animation.indexOf(slider.getValue());
				if (index >= 0)
				{
					animation.removeFrame(index);
				}
				updateSlider();
			}
		});

		menu.addSeparator();

		/*menuItem = new JMenuItem("Set key in percent");
		menuItem.setMnemonic('I');
		menu.add(menuItem);
		menu
		
		menu.addSeparator();*/

		final JCheckBoxMenuItem autoKeyItem = new JCheckBoxMenuItem("Auto key",
				autokey);
		menuItem.setMnemonic('k');
		menu.add(autoKeyItem);
		autoKeyItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				autokey = autoKeyItem.isSelected();
			}
		});

		menuItem = new JMenuItem("Set frame count...");
		menuItem.setMnemonic('c');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int frames = slider.getLength() - 1;
				Object value = JOptionPane.showInputDialog(frame,
						"Number of frames:", "Set frame count",
						JOptionPane.QUESTION_MESSAGE, null, null, frames);
				try
				{
					frames = Integer.parseInt((String) value);
				}
				catch (Exception ex)
				{
				}
				animation.setFrameCount(frames);
				updateSlider();
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Previous");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0));
		menuItem.setMnemonic('P');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() - 1);
			}
		});

		menuItem = new JMenuItem("Next");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));
		menuItem.setMnemonic('N');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() + 1);
			}
		});

		menuItem = new JMenuItem("Previous 10");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
				KeyEvent.SHIFT_DOWN_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() - 10);
			}
		});

		menuItem = new JMenuItem("Next 10");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
				KeyEvent.SHIFT_DOWN_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() + 10);
			}
		});

		menuItem = new JMenuItem("First");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('F');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getFirstFrame());
			}
		});

		menuItem = new JMenuItem("Last");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('L');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getLastFrame());
			}
		});


		menu = new JMenu("Animation");
		menu.setMnemonic('A');
		menuBar.add(menu);

		useScaledZoomCheck = new JCheckBoxMenuItem("Use scaled zoom");
		useScaledZoomCheck.setSelected(animation.isScaledZoom());
		menu.add(useScaledZoomCheck);
		useScaledZoomCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animation.setScaledZoom(useScaledZoomCheck.isSelected());
			}
		});

		menuItem = new JMenuItem("Scale animation...");
		menuItem.setMnemonic('S');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double scale = -1.0;
				Object value = JOptionPane.showInputDialog(frame,
						"Scale factor:", "Scale animation",
						JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
				try
				{
					scale = Double.parseDouble((String) value);
				}
				catch (Exception ex)
				{
				}
				if (scale != 1.0 && scale > 0)
				{
					animation.scale(scale);
				}
				updateSlider();
			}
		});

		menuItem = new JMenuItem("Scale height...");
		menuItem.setMnemonic('h');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double scale = -1.0;
				Object value = JOptionPane.showInputDialog(frame,
						"Scale factor:", "Scale height",
						JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
				try
				{
					scale = Double.parseDouble((String) value);
				}
				catch (Exception ex)
				{
				}
				if (scale != 1.0 && scale > 0)
				{
					animation.scaleHeight(scale);
				}
			}
		});

		menuItem = new JMenuItem("Smooth eye speed");
		menuItem.setMnemonic('m');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane
						.showConfirmDialog(
								frame,
								"This will redistribute keyframes to attempt to smooth the eye speed.\nDo you wish to continue?",
								"Smooth eye speed", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{
					animation.smoothEyeSpeed();
					updateSlider();
				}
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Preview");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		menuItem.setMnemonic('P');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(1);
			}
		});

		menuItem = new JMenuItem("Preview x2");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(2);
			}
		});

		menuItem = new JMenuItem("Preview x10");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(10);
			}
		});

		menuItem = new JMenuItem("Render (high-res)...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('R',
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('R');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(1);
			}
		});

		menuItem = new JMenuItem("Render (standard-res)...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(0);
			}
		});

		menuItem = new JMenuItem("Custom render (TO BE REMOVED!)");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Thread thread = new Thread(new Runnable()
				{
					public void run()
					{
						double detail = 1.0;

						int first = animation.getFirstFrame();
						int last = animation.getLastFrame();

						first = 590;
						last = 595;

						// last = 600;

						animate(detail, first, last, new File("frames"), false);

						/*joinThread(animate(
								detail,
								0,
								1,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));

						joinThread(animate(
								detail,
								first,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));*/

						/*
						int endFirst = 2000;
						int startSecond = 7000;
						
						alos.setEnabled(true);
						map1.setEnabled(false);
						map2.setEnabled(false);
						roads.setEnabled(false);

						joinThread(animate(
								detail,
								first,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/landsat")));

						alos.setEnabled(false);
						map1.setEnabled(false);
						map2.setEnabled(false);
						roads.setEnabled(true);

						joinThread(animate(
								detail,
								first,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/roads")));

						alos.setEnabled(false);
						map1.setEnabled(true);
						map2.setEnabled(false);
						roads.setEnabled(false);

						joinThread(animate(
								detail,
								first,
								endFirst,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));
						joinThread(animate(
								detail,
								startSecond,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));

						alos.setEnabled(false);
						map1.setEnabled(false);
						map2.setEnabled(true);
						roads.setEnabled(false);

						joinThread(animate(
								detail,
								first,
								endFirst,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map2")));
						joinThread(animate(
								detail,
								startSecond,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map2")));

						try
						{
							Runtime.getRuntime().exec(
									"c:/winnt/system32/shutdown.exe -s -t 60");
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}*/
					}
				});
				thread.setDaemon(true);
				thread.start();
			}

			private void joinThread(Thread thread)
			{
				while (thread.isAlive())
				{
					try
					{
						thread.join();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}

	private OrbitView getView()
	{
		return (OrbitView) wwd.getView();
	}

	private void addFrame()
	{
		OrbitView view = getView();
		if (view.getPitch().equals(Angle.ZERO))
		{
			view.setPitch(Angle.fromDegrees(0.1));
			wwd.redrawNow();
		}
		updater.addFrame(slider.getValue(), view);
		// animation.addFrame(slider.getValue(), view);
		// updateAnimation();
	}

	private void applyView(OrbitView view)
	{
		int frame = slider.getValue();
		if (animation.getFirstFrame() <= frame
				&& frame <= animation.getLastFrame())
		{
			applying = true;
			animation.applyFrame(view, frame);
			applying = false;
		}
	}

	public void quit()
	{
		if (querySave())
		{
			frame.dispose();
			System.exit(0);
		}
	}

	private void setAnimation(Animation animation)
	{
		this.animation = animation;
		if (useScaledZoomCheck != null)
			useScaledZoomCheck.setSelected(animation.isScaledZoom());
	}

	private void newFile()
	{
		if (querySave())
		{
			setAnimation(new Animation());
			resetChanged();
			setFile(null);
			updateSlider();
		}
	}

	private void open()
	{
		if (querySave())
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new XmlFilter());
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			{
				File newFile = chooser.getSelectedFile();
				Animation newAnimation = new Animation();
				try
				{
					newAnimation.load(newFile);
					setAnimation(newAnimation);
					resetChanged();
					setFile(newFile);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "Could not open '"
							+ newFile.getAbsolutePath() + "'.\n" + e, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			updateSlider();
		}
	}

	private void save()
	{
		if (file == null)
		{
			saveAs();
		}
		else
		{
			save(file);
		}
	}

	private void saveAs()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new XmlFilter());
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File newFile = chooser.getSelectedFile();
			if (!newFile.getName().toLowerCase().endsWith(".xml"))
			{
				newFile = new File(newFile.getParent(), newFile.getName()
						+ ".xml");
			}
			boolean override = true;
			if (newFile.exists())
			{
				override = JOptionPane.showConfirmDialog(frame, newFile
						.getAbsolutePath()
						+ " already exists.\nDo you want to replace it?",
						"Save As", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
			}
			if (override)
			{
				setFile(newFile);
				if (file != null)
					save(file);
			}
		}
	}

	private void save(File file)
	{
		if (file != null)
		{
			try
			{
				animation.save(file);
				resetChanged();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(frame, "Saving failed.\n" + e,
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			setTitleBar();
		}
	}

	private void setFile(File file)
	{
		this.file = file;
		setTitleBar();
	}

	private void resetChanged()
	{
		animation.removeChangeListener(animationChangeListener);
		animation.addChangeListener(animationChangeListener);
		changed = false;
	}

	private boolean querySave()
	{
		if (!changed)
			return true;
		String file = this.file == null ? "Animation" : "'"
				+ this.file.getName() + "'";
		String message = file + " has been modified. Save changes?";
		int value = JOptionPane.showConfirmDialog(frame, message, "Save",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (value == JOptionPane.CANCEL_OPTION)
			return false;
		if (value == JOptionPane.YES_OPTION)
			save();
		return true;
	}

	private void setTitleBar()
	{
		String file;
		String title = "World Wind Animator";
		if (this.file != null)
			file = this.file.getName();
		else
			file = "New animation";
		if (changed)
			file += " *";
		title = file + " - " + title;
		frame.setTitle(title);
	}

	private void updateSlider()
	{
		slider.clearKeys();
		for (int i = 0; i < animation.size(); i++)
		{
			slider.addKey(animation.getFrame(i));
		}
		slider.setMin(0);
		slider.setMax(animation.getFrameCount());
	}

	private void setSlider(int frame)
	{
		settingSlider = true;
		slider.setValue(frame);
		settingSlider = false;
	}

	private void preview(final int frameSkip)
	{
		if (animation != null && animation.size() > 0)
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					stop = false;

					int firstFrame = Math.max(slider.getValue(), animation
							.getFirstFrame());
					int lastFrame = animation.getLastFrame();

					for (int frame = firstFrame; frame <= lastFrame; frame += frameSkip)
					{
						setSlider(frame);
						applyView(getView());
						wwd.redrawNow();

						if (stop)
							break;
					}
				}
			});
			thread.start();
		}
	}

	private Thread animate(final double detailHint)
	{
		int firstFrame = Math.max(slider.getValue(), animation.getFirstFrame());
		int lastFrame = animation.getLastFrame();
		return animate(detailHint, firstFrame, lastFrame, new File(DATA_DRIVE
				+ ":/frames"), true);
	}

	private Thread animate(final double detailHint, final int firstFrame,
			final int lastFrame, final File outputDir, final boolean alpha)
	{
		if (animation != null && animation.size() > 0)
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					stop = false;

					boolean immediate = ImmediateMode.isImmediate();
					ImmediateMode.setImmediate(true);
					AnimatorSceneController asc = (AnimatorSceneController) wwd
							.getSceneController();

					setAnimationSize(animation.getWidth(), animation
							.getHeight());

					crosshair.setEnabled(false);
					double detailHintBackup = dem.getDetailHint();
					dem.setDetailHint(detailHint);
					frame.setAlwaysOnTop(true);

					View view = wwd.getView();
					OrbitView orbitView = (OrbitView) view;
					boolean detectCollisions = orbitView.isDetectCollisions();
					orbitView.setDetectCollisions(false);

					int filenameLength = String.valueOf(lastFrame).length();

					for (int frame = firstFrame; frame <= lastFrame; frame += 1)
					{
						setSlider(frame);
						applyView(getView());

						asc.takeScreenshot(new File(outputDir, "frame"
								+ FileUtil.paddedInt(frame, filenameLength)
								+ ".tga"), alpha);
						wwd.redraw();
						asc.waitForScreenshot();

						if (stop)
							break;
					}

					dem.setDetailHint(detailHintBackup);
					crosshair.setEnabled(true);
					orbitView.setDetectCollisions(detectCollisions);
					frame.setAlwaysOnTop(false);

					ImmediateMode.setImmediate(immediate);
				}
			});
			thread.start();
			return thread;
		}
		return null;
	}

	private class Updater
	{
		private Map<Integer, ViewParameters> toApply = new HashMap<Integer, ViewParameters>();
		private Object waiter = new Object();

		public Updater()
		{
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					while (true)
					{
						Integer key = getNextKey();
						if (key != null)
						{
							ViewParameters vp = removeValue(key);
							animation.addFrame(key, vp.eye, vp.center);
							updateSlider();
						}
						else
						{
							synchronized (waiter)
							{
								try
								{
									waiter.wait();
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}

		private synchronized Integer getNextKey()
		{
			Set<Integer> keyset = toApply.keySet();
			if (keyset.isEmpty())
				return null;
			return keyset.iterator().next();
		}

		private synchronized ViewParameters removeValue(Integer key)
		{
			return toApply.remove(key);
		}

		public synchronized void addFrame(int frame, OrbitView view)
		{
			toApply.put(frame, ViewParameters.fromView(view));
			synchronized (waiter)
			{
				waiter.notify();
			}
		}

	}

	private static class ViewParameters
	{
		public Position eye;
		public Position center;

		public static ViewParameters fromView(OrbitView view)
		{
			ViewParameters vp = new ViewParameters();
			vp.eye = view.getEyePosition();
			vp.center = view.getCenterPosition();
			return vp;
		}
	}

	private static class XmlFilter extends javax.swing.filechooser.FileFilter
	{
		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
				return true;
			return f.getName().toLowerCase().endsWith(".xml");
		}

		@Override
		public String getDescription()
		{
			return "XML files (*.xml)";
		}
	}
}
