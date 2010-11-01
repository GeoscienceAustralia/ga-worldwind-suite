package au.gov.ga.worldwind.animator.ui.parametereditor;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterEditorWindowLabelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The parameter editor panel used to edit individual animation {@link Parameter}
 * curves on a 2D x-y time-value axis.
 */
public class ParameterEditor extends JFrame
{
	private static final long serialVersionUID = 20101101L;

	private Animator targetApplication;
	
	private JSplitPane containerPane;
	private JScrollPane leftScrollPane;
	private JScrollPane rightScrollPane;
	
	public ParameterEditor(Animator targetApplication)
	{
		Validate.notNull(targetApplication, "A Animator instance must be provided");
		this.targetApplication = targetApplication;
		
		this.setTitle(getMessage(getParameterEditorWindowLabelKey()));
		
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				ParameterEditor.this.targetApplication.setParameterEditorVisible(false);
			}
		});
		
		setupSplitPane();
	}

	private void setupSplitPane()
	{
		containerPane = new JSplitPane();
		containerPane.setDividerLocation(300);
		this.add(containerPane);
	}
	
	
	
}
