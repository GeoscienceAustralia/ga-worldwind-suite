package au.gov.ga.worldwind.animator.application.render;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.JTextField;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.JIntegerField;

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
	private JIntegerField widthField;
	private JIntegerField heightField;
	private JCheckBox lockedCheck;
	private JTextField scaleField;
	
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
		
		JLabel heightLabel = new JLabel(getMessage(getRenderDialogHeightLabelKey()), JLabel.TRAILING);
		heightField = new JIntegerField(true, null);
		heightField.setToolTipText(getMessage(getRenderDialogHeightTooltipKey()));
		heightField.setColumns(6);
		heightField.setMaximumSize(new Dimension(100, 10));
		
		lockedCheck = new JCheckBox();
		lockedCheck.setIcon(Icons.unlock.getIcon());
		lockedCheck.setSelectedIcon(Icons.lock.getIcon());
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addComponent(widthLabel)
				.addComponent(widthField)
				.addComponent(heightLabel)
				.addComponent(heightField)
				.addComponent(lockedCheck)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup()
				.addComponent(widthLabel)
				.addComponent(widthField)
				.addComponent(heightLabel)
				.addComponent(heightField)
				.addComponent(lockedCheck)
		);
		
		dimensionsPane.add(widthField);
		dimensionsPane.add(heightField);
		
		contentPane.add(dimensionsPane);
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
		
		widthField.setValue(currentAnimation.getRenderParameters().getImageDimension().width);
		heightField.setValue(currentAnimation.getRenderParameters().getImageDimension().height);
		lockedCheck.setSelected(currentAnimation.getRenderParameters().isDimensionsLocked());
		
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
