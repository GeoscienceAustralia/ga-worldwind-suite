package au.gov.ga.worldwind.animator.application.render;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermCancelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.ui.BasicAction;
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

	private Animation currentAnimation;
	
	private JPanel contentPane;

	private BasicAction renderAction;
	private BasicAction cancelAction;
	
	private JPanel presetsPane;
	
	private JPanel dimensionsPane;
	private double aspectRatio;
	private JIntegerField widthField;
	private JIntegerField heightField;
	private JCheckBox lockedCheck;
	private JIntegerField scaleField;
	private JLabel renderDimensionsLabel;
	
	private JPanel detailPane;
	
	private JPanel frameRangePane;
	
	private JPanel destinationPane;

	private int response = JOptionPane.CANCEL_OPTION;
	
	public RenderDialog(Frame owner)
	{
		super(owner, true);
		
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
		setMinimumSize(new Dimension(300, 300));
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
				).addGroup(
					layout.createParallelGroup()
					.addComponent(scaleLabel)
					.addComponent(scaleField)
					.addComponent(scaleUnitsLabel)
					.addComponent(outputLabel)
					.addComponent(renderDimensionsLabel)
				)
		);
		
		dimensionsPane.add(widthField);
		dimensionsPane.add(heightField);
		
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
		// TODO Auto-generated method stub
		
	}

	private void addFrameRangePane()
	{
		// TODO Auto-generated method stub
		
	}
	
	private void addDestinationPane()
	{
		// TODO
	}
	
	private void addButtonPanel()
	{
		contentPane.add(Box.createVerticalGlue());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(renderAction));
		buttonPanel.add(new JButton(cancelAction));
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
		
		// TODO: REMOVE!!!
		renderParameters.setStartFrame(0);
		renderParameters.setEndFrame(currentAnimation.getLastKeyFrame() == null ? 0 : currentAnimation.getLastKeyFrame().getFrame());
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
	
	private class DialogPane extends JPanel
	{
		public DialogPane(String title)
		{
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
		}
	}
}
