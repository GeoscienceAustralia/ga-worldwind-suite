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
package au.gov.ga.worldwind.animator.application.effects;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getAddEffectDialogTitleKey;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermCancelKey;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getTermOkKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A {@link JDialog} that displays a list of {@link AnimatableEffect}s, allowing the user
 * to select an effect to add to the current animation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EffectDialog extends JDialog
{
	/**
	 * Show a new {@link EffectDialog}, and block until the user has selected an
	 * effect.
	 * 
	 * @param parent
	 *            Dialog's parent
	 * @return The effect class selected, or null if the user cancelled.
	 */
	public static Class<? extends AnimatableEffect> collectEffect(Frame parent)
	{
		EffectDialog dialog = new EffectDialog(parent);
		dialog.setVisible(true);

		if (dialog.getResponse() != JOptionPane.OK_OPTION)
		{
			return null;
		}

		return dialog.getEffect();
	}

	private JList effectList;
	private JButton okButton;
	private JButton cancelButton;
	private int response;

	public EffectDialog(Frame parent)
	{
		super(parent, getMessage(getAddEffectDialogTitleKey()), true);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				response = JOptionPane.CLOSED_OPTION;
				dispose();
			}
		});

		GridBagConstraints c;
		JPanel rootPanel = new JPanel(new GridBagLayout());
		setContentPane(rootPanel);
		int spacing = 5;

		DefaultListModel model = new DefaultListModel();
		SortedSet<Class<? extends AnimatableEffect>> effects = EffectRegistry.instance.getEffects();
		for (Class<? extends AnimatableEffect> effect : effects)
		{
			model.addElement(new EffectListElement(effect));
		}

		effectList = new JList(model);
		effectList.setBorder(BorderFactory.createEtchedBorder());
		c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(spacing, spacing, spacing, spacing);
		c.fill = GridBagConstraints.BOTH;
		rootPanel.add(effectList, c);

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		rootPanel.add(buttonPanel, c);

		okButton = new JButton(getMessage(getTermOkKey()));
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.OK_OPTION;
				dispose();
			}
		});
		getRootPane().setDefaultButton(okButton);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0, spacing, spacing, spacing);
		buttonPanel.add(okButton, c);

		cancelButton = new JButton(getMessage(getTermCancelKey()));
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				response = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.insets = new Insets(0, 0, spacing, spacing);
		buttonPanel.add(cancelButton, c);

		pack();
		setLocationRelativeTo(parent);
	}

	public int getResponse()
	{
		return response;
	}

	public Class<? extends AnimatableEffect> getEffect()
	{
		Object selected = effectList.getSelectedValue();
		if (selected != null && selected instanceof EffectListElement)
		{
			return ((EffectListElement) selected).effect;
		}
		return null;
	}

	private class EffectListElement
	{
		public final Class<? extends AnimatableEffect> effect;

		public EffectListElement(Class<? extends AnimatableEffect> effect)
		{
			this.effect = effect;
		}

		@Override
		public String toString()
		{
			return EffectRegistry.instance.getEffectName(effect);
		}
	}
}
