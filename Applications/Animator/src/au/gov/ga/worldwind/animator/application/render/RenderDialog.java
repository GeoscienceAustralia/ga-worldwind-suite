package au.gov.ga.worldwind.animator.application.render;

import static au.gov.ga.worldwind.animator.util.FileUtil.createSequenceFileName;
import static au.gov.ga.worldwind.animator.util.FileUtil.stripSequenceNumber;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.FileUtil.stripExtension;
import static au.gov.ga.worldwind.common.util.Util.isBlank;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.util.FileUtil;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.JDoubleField;
import au.gov.ga.worldwind.common.ui.JIntegerField;
import au.gov.ga.worldwind.common.ui.SwingUtil;

/**
 * A dialog box used for setting render properties and triggering a render.
 * <p/>
 * Most render properties are stored with the {@link Animation} object. 
 * Others may be application settings.
 */
public class RenderDialog extends JDialog implements ChangeOfAnimationListener
{
	private static final long serialVersionUID = 20110125L;

	private Animator targetApplication;
	
	private Animation currentAnimation;
	
	private JPanel contentPane;

	private BasicAction renderAction;
	private BasicAction cancelAction;
	private BasicAction applyAction;
	private BasicAction resetAction;
	
	// TODO: Add presets
	//private JPanel presetsPane;
	
	private JPanel dimensionsPane;
	private double aspectRatio;
	private JIntegerField widthField;
	private JIntegerField heightField;
	private JCheckBox lockedCheck;
	private JIntegerField scaleField;
	private JLabel renderDimensionsLabel;
	
	private JPanel detailPane;
	private JDoubleField detailField;
	private JFileChooser fileChooser = new JFileChooser();
	private JLabel outputExampleLabel;
	
	private JPanel frameRangePane;
	private JIntegerField frameStartField;
	private JIntegerField frameEndField;
	
	private JPanel destinationPane;
	private JTextField destinationField;

	private int response = JOptionPane.CANCEL_OPTION;
	
	private static final int DIALOG_WIDTH = 300;
	private static final int DIALOG_HEIGHT = 390;
	
	public RenderDialog(Animator targetApplication, Frame owner)
	{
		super(owner, true);
		
		this.targetApplication = targetApplication;
		
		initialiseDialog();
		initialiseActions();
		
		addPresetsPanel();
		addDimensionsPanel();
		addDetailPanel();
		addFrameRangePane();
		addDestinationPane();
		addButtonPanel();
		
		packComponents();
	}


	private void initialiseDialog()
	{
		setModal(true);
		setTitle(getMessage(getRenderDialogTitleKey()));
		setIconImage(Icons.render.getIcon().getImage());
		
		contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		setContentPane(contentPane);
		setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		setResizable(false);
	}
	
	private void initialiseActions()
	{
		renderAction = new BasicAction(getMessage(getRenderDialogRenderLabelKey()), getMessage(getRenderDialogRenderTooltipKey()), Icons.render.getIcon());
		renderAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				bindToAnimation();
				hideDialog(JOptionPane.OK_OPTION);
			}
		});
		
		cancelAction = new BasicAction(getMessage(getTermCancelKey()), null);
		cancelAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				hideDialog(JOptionPane.CANCEL_OPTION);
			}
		});
		
		applyAction = new BasicAction(getMessage(getRenderDialogApplyLabelKey()), getMessage(getRenderDialogApplyTooltipKey()), null);
		applyAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				bindToAnimation();
				targetApplication.resizeWindowToRenderDimensions();
			}
		});
		
		resetAction = new BasicAction(getMessage(getRenderDialogResetLabelKey()), getMessage(getRenderDialogResetTooltipKey()), null);
		resetAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateFieldsFromAnimation();
			}
		});
	}
	
	private void addPresetsPanel()
	{
		// TODO Auto-generated method stub
		
	}

	private void addDimensionsPanel()
	{
		dimensionsPane = new DialogPane(getMessage(getRenderDialogDimensionsLabelKey()));

		GroupLayout layout = new GroupLayout(dimensionsPane);
		dimensionsPane.setLayout(layout);

		JLabel widthLabel = new JLabel(getMessage(getRenderDialogWidthLabelKey()), JLabel.TRAILING);
		widthField = new JIntegerField(true, null);
		widthField.setToolTipText(getMessage(getRenderDialogWidthTooltipKey()));
		widthField.setColumns(6);
		widthField.setMaximumSize(new Dimension(100, 10));
		widthField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (lockedCheck.isSelected())
				{
					lockHeightToWidth();
				}
				else
				{
					updateAspectRatio();
				}
				updateRenderDimensions();
			}
		});
		
		JLabel heightLabel = new JLabel(getMessage(getRenderDialogHeightLabelKey()), JLabel.TRAILING);
		heightField = new JIntegerField(true, null);
		heightField.setToolTipText(getMessage(getRenderDialogHeightTooltipKey()));
		heightField.setColumns(6);
		heightField.setMaximumSize(new Dimension(100, 10));
		heightField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (lockedCheck.isSelected())
				{
					lockWidthToHeight();
				}
				else
				{
					updateAspectRatio();
				}
				updateRenderDimensions();
			}
		});
		
		lockedCheck = new JCheckBox();
		lockedCheck.setIcon(Icons.unlock.getIcon());
		lockedCheck.setSelectedIcon(Icons.lock.getIcon());
		
		JLabel scaleLabel = new JLabel(getMessage(getRenderDialogScaleLabelKey()), JLabel.TRAILING);
		scaleField = new JIntegerField(true, null);
		scaleField.setToolTipText(getMessage(getRenderDialogScaleTooltipKey()));
		scaleField.setColumns(5);
		scaleField.setMaximumSize(new Dimension(50, 10));
		scaleField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				updateRenderDimensions();
			}
		});
		JLabel scaleUnitsLabel = new JLabel("%");
		
		JLabel outputLabel = new JLabel(getMessage(getRenderDialogRenderSizeLabelKey()) + ":", JLabel.TRAILING);
		renderDimensionsLabel = new JLabel();
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		Component hGlue = Box.createHorizontalGlue();
		
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(
					layout.createParallelGroup()
					.addGroup(
						layout.createSequentialGroup()
						.addComponent(widthLabel)
						.addComponent(widthField)
					).addGroup(
						layout.createSequentialGroup()
						.addComponent(scaleLabel)
						.addComponent(scaleField)
						.addComponent(scaleUnitsLabel)
					)
				)
				.addGroup(
					layout.createParallelGroup()
					.addGroup(
						layout.createSequentialGroup()
						.addComponent(heightLabel)
						.addComponent(heightField)
						.addComponent(lockedCheck)
					).addGroup(
						layout.createSequentialGroup()
						.addComponent(outputLabel)
						.addComponent(renderDimensionsLabel)
					)
				)
				.addComponent(hGlue)
				
		);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(
					layout.createParallelGroup()
					.addComponent(widthLabel)
					.addComponent(widthField)
					.addComponent(heightLabel)
					.addComponent(heightField)
					.addComponent(lockedCheck)
					.addComponent(hGlue)
				).addGroup(
					layout.createParallelGroup()
					.addComponent(scaleLabel)
					.addComponent(scaleField)
					.addComponent(scaleUnitsLabel)
					.addComponent(outputLabel)
					.addComponent(renderDimensionsLabel)
				)
		);
		
		contentPane.add(dimensionsPane);
	}
	
	private void lockWidthToHeight()
	{
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				widthField.setValue((int)(heightField.getValue()*aspectRatio));
			}
		});
		
	}
	
	private void lockHeightToWidth()
	{
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				heightField.setValue((int)(widthField.getValue()/aspectRatio));
			}
		});
	}

	private void updateRenderDimensions()
	{
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				double scaler = scaleField.getValue() / 100d;
				int newWidth = (int)(widthField.getValue() * scaler);
				int newHeight = (int)(heightField.getValue() * scaler);
				renderDimensionsLabel.setText(newWidth + "x" + newHeight);
			}
		});
	}

	private void updateAspectRatio()
	{
		aspectRatio = (double)(widthField.getValue()) / (double)(heightField.getValue());
	}
	
	private void addDetailPanel()
	{
		detailPane = new DialogPane(getMessage(getRenderDialogDetailLabelKey()));
		
		GroupLayout layout = new GroupLayout(detailPane);
		detailPane.setLayout(layout);
		
		JLabel detailLabel = new JLabel(getMessage(getRenderDialogDetailLevelLabelKey()));
		detailField = new JDoubleField(null);
		detailField.setPositive(true);
		detailField.setToolTipText(getMessage(getRenderDialogDetailLevelTooltipKey()));
		detailField.setMaximumSize(new Dimension(50, 10));
		
		Component hGlue = Box.createHorizontalGlue();
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addComponent(detailLabel)
				.addComponent(detailField)
				.addComponent(hGlue)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup()
				.addComponent(detailLabel)
				.addComponent(detailField)
				.addComponent(hGlue)
		);
		
		contentPane.add(detailPane);
	}

	private void addFrameRangePane()
	{
		frameRangePane = new DialogPane(getMessage(getRenderDialogFrameRangeLabelKey()));
		
		GroupLayout layout = new GroupLayout(frameRangePane);
		frameRangePane.setLayout(layout);
		
		
		JLabel frameStartLabel = new JLabel(getMessage(getRenderDialogFrameStartLabelKey()));
		frameStartField = new JIntegerField(true, null);
		frameStartField.setToolTipText(getMessage(getRenderDialogFrameStartTooltipKey()));
		frameStartField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				frameStartField.setValue(Math.min(Math.max(0, frameStartField.getValue()), getCurrentAnimation().getLastFrame()));
				forceFrameEndAfterFrameStart();
				updateOutputExampleLabel();
			}
		});
		
		JLabel frameEndLabel = new JLabel(getMessage(getRenderDialogFrameEndLabelKey()));
		frameEndField = new JIntegerField(true, null);
		frameEndField.setToolTipText(getMessage(getRenderDialogFrameEndTooltipKey()));
		frameEndField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				frameEndField.setValue(Math.max(Math.min(getCurrentAnimation().getLastFrame(), frameEndField.getValue()), 0));
				forceFrameStartBeforeFrameEnd();
				updateOutputExampleLabel();
			}

		});
		
		Component hGlue = Box.createHorizontalGlue();
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addComponent(frameStartLabel)
				.addComponent(frameStartField)
				.addComponent(frameEndLabel)
				.addComponent(frameEndField)
				.addComponent(hGlue)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup()
				.addComponent(frameStartLabel)
				.addComponent(frameStartField)
				.addComponent(frameEndLabel)
				.addComponent(frameEndField)
				.addComponent(hGlue)
		);

		contentPane.add(frameRangePane);
	}
	
	private void forceFrameEndAfterFrameStart()
	{
		if (frameEndField.getValue() < frameStartField.getValue())
		{
			frameEndField.setValue(frameStartField.getValue());
		}
	}
	
	private void forceFrameStartBeforeFrameEnd()
	{
		if (frameStartField.getValue() > frameEndField.getValue())
		{
			frameStartField.setValue(frameEndField.getValue());
		}
	}
	
	private void addDestinationPane()
	{
		destinationPane = new DialogPane(getMessage(getRenderDialogDestinationLabelKey()));
		
		GroupLayout layout = new GroupLayout(destinationPane);
		destinationPane.setLayout(layout);
		
		JLabel destinationLabel = new JLabel(getMessage(getRenderDialogOutputFieldLabelKey()));
		JButton browseButton = new JButton(Icons.folder.getIcon());
		browseButton.setMargin(new Insets(0,0,0,0));
		browseButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!isBlank(destinationField.getText()))
				{
					File currentDir = new File(destinationField.getText());
					while (!currentDir.exists() && !currentDir.isDirectory())
					{
						if (currentDir.getParentFile() == null)
						{
							break;
						}
						currentDir = currentDir.getParentFile();
					}
					fileChooser.setCurrentDirectory(currentDir);
				}
				else
				{
					fileChooser.setCurrentDirectory(Settings.get().getLastUsedLocation());
				}
				fileChooser.showOpenDialog(RenderDialog.this);
				if (fileChooser.getSelectedFile() == null)
				{
					return;
				}
				destinationField.setText(FileUtil.stripSequenceNumber(FileUtil.stripExtension(fileChooser.getSelectedFile().getAbsolutePath())));
				updateOutputExampleLabel();
			}
		});
		
		destinationField = new JTextField();
		destinationField.setToolTipText(getMessage(getRenderDialogOutputFieldTooltipKey()));
		destinationField.setMinimumSize(new Dimension(200, 10));
		destinationField.addPropertyChangeListener("text", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				updateOutputExampleLabel();
			}
		});
		destinationField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				updateOutputExampleLabel();
			}
		});
		
		outputExampleLabel = new JLabel();
		
		Component hGlue = Box.createHorizontalGlue();
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
				layout.createParallelGroup()
				.addGroup(
					layout.createSequentialGroup()
					.addComponent(destinationLabel)
					.addComponent(destinationField)
					.addComponent(browseButton)
					.addComponent(hGlue)
				)
				.addComponent(outputExampleLabel)
		);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(
					layout.createParallelGroup()
					.addComponent(destinationLabel)
					.addComponent(destinationField)
					.addComponent(browseButton)
					.addComponent(hGlue)
				)
				.addComponent(outputExampleLabel)
		);
		
		contentPane.add(destinationPane);
	}
	
	private void updateOutputExampleLabel()
	{
		if (isBlank(destinationField.getText()))
		{
			outputExampleLabel.setText(getMessage(getRenderDialogOutputExampleLabelKey(), ""));
			return;
		}
		File destinationFile = new File(destinationField.getText());
		String name = stripSequenceNumber(stripExtension(destinationFile.getName()));
		
		int firstFrame = Math.max(0, frameStartField.getValue());
		int lastFrame = Math.min(frameEndField.getValue(), getCurrentAnimation().getLastFrame());
		int filenameLength = String.valueOf(getCurrentAnimation().getFrameCount()).length();
		
		final String example = createSequenceFileName(name, firstFrame, filenameLength, "") + ",...," + createSequenceFileName(name, lastFrame, filenameLength, "");
		
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				outputExampleLabel.setText(getMessage(getRenderDialogOutputExampleLabelKey(), example));
			}
		});
	}
	
	private void addButtonPanel()
	{
		contentPane.add(Box.createVerticalGlue());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(renderAction));
		buttonPanel.add(new JButton(cancelAction));
		buttonPanel.add(new JButton(applyAction));
		buttonPanel.add(new JButton(resetAction));
		buttonPanel.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);
		buttonPanel.setMaximumSize(new Dimension(400, 30));
		contentPane.add(buttonPanel);
	}

	public void setCurrentAnimation(Animation currentAnimation)
	{
		this.currentAnimation = currentAnimation;
	}

	public Animation getCurrentAnimation()
	{
		return currentAnimation;
	}

	@Override
	public void updateAnimation(Animation newAnimation)
	{
		setCurrentAnimation(newAnimation);
	}
	
	private void packComponents()
	{
		this.validate();
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		if (visible)
		{
			updateFieldsFromAnimation();
			updateOutputExampleLabel();
			response = JOptionPane.CANCEL_OPTION;
		}
		super.setVisible(visible);
	}
	
	private void updateFieldsFromAnimation()
	{
		if (currentAnimation == null)
		{
			return;
		}
		
		SwingUtil.invokeTaskOnEDT(new Runnable(){
			@Override
			public void run()
			{
				RenderParameters renderParameters = currentAnimation.getRenderParameters();
				widthField.setValue(renderParameters.getImageDimension().width);
				heightField.setValue(renderParameters.getImageDimension().height);
				lockedCheck.setSelected(renderParameters.isDimensionsLocked());
				scaleField.setValue(renderParameters.getImageScalePercent());
				aspectRatio = renderParameters.getImageAspectRatio();
				detailField.setValue(renderParameters.getDetailLevel());
				if (renderParameters.getRenderDestination() != null)
				{
					destinationField.setText(renderParameters.getRenderDestination().getAbsolutePath());
				}
				frameStartField.setValue(renderParameters.getStartFrame() == null ? 0 : renderParameters.getStartFrame());
				frameEndField.setValue(renderParameters.getEndFrame() == null ? currentAnimation.getLastFrame() : renderParameters.getEndFrame());
				updateRenderDimensions();
			}
		});
	}
	
	private void bindToAnimation()
	{
		if (currentAnimation == null)
		{
			return;
		}
		
		RenderParameters renderParameters = currentAnimation.getRenderParameters();
		renderParameters.setImageDimension(new Dimension(widthField.getValue(), heightField.getValue()));
		renderParameters.setDimensionsLocked(lockedCheck.isSelected());
		renderParameters.setImageScalePercent(scaleField.getValue());
		renderParameters.setDetailLevel(detailField.getValue());
		
		if (isBlank(destinationField.getText()))
		{
			renderParameters.setRenderDestination(null);
		}
		else
		{
			String filename = FileUtil.stripSequenceNumber(FileUtil.stripExtension(destinationField.getText()));
			renderParameters.setRenderDestination(new File(filename));
		}
		
		renderParameters.setStartFrame(Math.max(frameStartField.getValue(), 0));
		renderParameters.setEndFrame(Math.min(frameEndField.getValue(), currentAnimation.getLastFrame()));
	}

	private void hideDialog(int response)
	{
		this.response = response;
		setVisible(false);
	}
	
	public int getResponse()
	{
		return response;
	}
	
	@SuppressWarnings("serial")
	private class DialogPane extends JPanel
	{
		public DialogPane(String title)
		{
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
		}
		
		@Override
		public Dimension getPreferredSize()
		{
			Dimension result = super.getPreferredSize();
			result.width = getParent().getWidth();
			return result;
		}
		
		@Override
		public Dimension getMinimumSize()
		{
			Dimension result = super.getPreferredSize();
			result.width = getParent().getWidth();
			return result;
		}
	}
}
