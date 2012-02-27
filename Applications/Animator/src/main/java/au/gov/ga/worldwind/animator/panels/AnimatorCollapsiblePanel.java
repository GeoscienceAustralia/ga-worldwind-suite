/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
