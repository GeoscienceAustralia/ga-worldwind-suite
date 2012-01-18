package au.gov.ga.worldwind.animator.panels;

import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.common.ui.collapsiblesplit.CollapsibleSplitPane;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanel;

/**
 * An interface for panels that can be collapsed using a {@link CollapsibleSplitPane}.
 * <p/>
 * Extends the {@link CollapsiblePanel} interface and adds Animator-specifics.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimatorCollapsiblePanel extends CollapsiblePanel, Nameable, ChangeOfAnimationListener
{
}
