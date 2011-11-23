package au.gov.ga.worldwind.animator.ui;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddExaggeratorDialogTitleKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getExaggeratorDialogBoundaryLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getExaggeratorDialogExaggerationLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getTermOkKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getTermCancelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggerationImpl;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.JDoubleField;

/**
 * A dialog box that prompts the user for the two values needed to define an
 * elevation exaggerator: boundary elevation and exaggeration.
 */
public class ExaggeratorDialog extends JDialog
{
	private static final long serialVersionUID = 20101007L;

	/**
	 * Prompt the user for the information needed to create a new exaggerator.
	 * <p/>
	 * If the user cancels, will return <code>null</code>.
	 */
	public static ElevationExaggeration collectExaggeration(Frame parent)
	{
		ExaggeratorDialog dialog = new ExaggeratorDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.getResponse() == JOptionPane.CANCEL_OPTION)
		{
			return null;
		}
		
		return dialog.getExaggerator();
		
	}
	
	private static final double DEFAULT_BOUNDARY = 0.0;
	private static final double DEFAULT_EXAGGERATION = 1.0;
	
	private JDoubleField boundaryField;
	private JLabel boundaryLabel;
	
	private JDoubleField exaggerationField;
	private JLabel exaggerationLabel;
	
	private BasicAction okAction;
	private BasicAction cancelAction;
	
	private int response;
	
	
	private ExaggeratorDialog(Frame parent)
	{
		super(parent, getMessage(getAddExaggeratorDialogTitleKey()), true);
		
		initialiseComponents();
		addActionListeners();
		layoutDialog();
		
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(parent);
		
		validate();
		pack();
		
		boundaryField.requestFocusInWindow();
	}

	private void initialiseComponents()
	{
		boundaryLabel = new JLabel(getMessage(getExaggeratorDialogBoundaryLabelKey()));
		boundaryField = new JDoubleField(DEFAULT_BOUNDARY);
		boundaryField.setPositive(false);
		boundaryField.setColumns(12);
		
		exaggerationLabel = new JLabel(getMessage(getExaggeratorDialogExaggerationLabelKey()));
		exaggerationField = new JDoubleField(DEFAULT_EXAGGERATION);
		exaggerationField.setPositive(true);
		exaggerationField.setColumns(12);
	}

	private void layoutDialog()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		JPanel boundaryContainer = new JPanel();
		boundaryContainer.add(boundaryLabel);
		boundaryContainer.add(boundaryField);
		container.add(boundaryContainer);
		
		JPanel exaggerationContainer = new JPanel();
		exaggerationContainer.add(exaggerationLabel);
		exaggerationContainer.add(exaggerationField);
		container.add(exaggerationContainer);
		
		JPanel buttonContainer = new JPanel();
		buttonContainer.add(new JButton(okAction));
		buttonContainer.add(new JButton(cancelAction));
		container.add(buttonContainer);
		
		setContentPane(container);
	}
	
	private void addActionListeners()
	{
		okAction = new BasicAction(getMessage(getTermOkKey()), null);
		okAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		okAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.OK_OPTION;
				dispose();
			}
		});
		
		cancelAction = new BasicAction(getMessage(getTermCancelKey()), null);
		cancelAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		cancelAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});
		
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelDialogInput");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "okDialogInput");
		
		getRootPane().getActionMap().put("cancelDialogInput", cancelAction);
		getRootPane().getActionMap().put("okDialogInput", okAction);
	}

	
	public int getResponse()
	{
		return response;
	}
	
	public ElevationExaggeration getExaggerator()
	{
		if (exaggerationField.getValue() == null || boundaryField.getValue() == null)
		{
			return null;
		}
		return new ElevationExaggerationImpl(exaggerationField.getValue(), boundaryField.getValue());
	}
}
