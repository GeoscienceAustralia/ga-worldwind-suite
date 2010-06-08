/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2002, Ithaqua Enterprises Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package gistoolkit.display.widgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 * Generic dialog to contain OK and Cancel Buttons.
 * <p>
 *  This dialog allows the user to easilly create a dialog that contains controlls.  The dialog
 *  contains both an OK, and Cancel button at the bottom right.  The application can
 *  determine which button was selected by calling isOK() on the dialog after it has returned.
 * </p>
 * <p>
 *  If you are in need of a dialog that checks the contents of it's controlls, for proper useage,
 *  you may wish to sub class this dialog.  The dialog will call the doOK() when the OK button 
 *  is selected by the user, but before the dialog is dismissed.  If the doOK() method returns
 *  a value of tue, the dialog will be dismissed, and isOK() will subsequently be true.
 *  If on the other hand, the doOK() buton returns false, then no action is taken, the dialog
 *  is not disposed.
 * </p>
 * <p>
 *  The same behavior is true of the doCancel() method with the exception that if it returns true,
 *  subsequent calls to the isOK() method return false.
 * </p>
 */
public class GISToolkitDialog extends JDialog implements ActionListener {
    
    /**
     * The OK button.  This button is used by the user to indicate that the dialog 
     *  should be taken seriously.
     */
    private JButton myOKButton;
    
    /**
     * Reference to the Cancel Button;
     */
    private JButton myCancelButton;
    
    /**
     * Determines if the OK button was selected.
     */
    private boolean myOK = false;
    
    /**
     * Reference to the center or contents panel.
     */
    private Container myContentsPanel = new JPanel();
    
    /**
     * GISToolkitDialog constructor comment.
     */
    public GISToolkitDialog() {
        super();
        initPanel();
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Dialog
     */
    public GISToolkitDialog(Dialog owner) {
        super(owner);
        initPanel();
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Dialog
     * @param title java.lang.String
     */
    public GISToolkitDialog(Dialog owner, String title) {
        super(owner, title);
        initPanel();
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Dialog
     * @param title java.lang.String
     * @param modal boolean
     */
    public GISToolkitDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        initPanel();
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Dialog
     * @param modal boolean
     */
    public GISToolkitDialog(Dialog owner, boolean modal) {
        super(owner, modal);
        initPanel();
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Frame
     */
    public GISToolkitDialog(Frame owner) {
        super(owner);
        initPanel();
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Frame
     * @param title java.lang.String
     */
    public GISToolkitDialog(Frame owner, String title) {
        super(owner, title);
        initPanel();
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Frame
     * @param title java.lang.String
     * @param modal boolean
     */
    public GISToolkitDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }
    
    /**
     * GISToolkitDialog constructor comment.
     * @param owner java.awt.Frame
     * @param modal boolean
     */
    public GISToolkitDialog(Frame owner, boolean modal) {
        super(owner, modal);
        initPanel();
    }
    
    /**
     * Respond to the events from the OK and Cancel buttons.
     */
    public void actionPerformed(ActionEvent inAE){
        if (inAE.getSource() == myOKButton){
            // do the action to do on OK
            if (doOK()){
                
                // set the OK
                myOK = true;
                this.dispose();
            }
            
        }
        if (inAE.getSource() == myCancelButton){
            if (doCancel()){
                
                // set the OK
                myOK = false;
                this.dispose();
            }
        }
    }
    
    /**
     * Called when the Cancel button is selected.
     * If a false is returned, the dialog will not be disposed.
     * <p>
     *  The dialog will call the doCancel() when the Cancel button 
     *  is selected by the user, but before the dialog is dismissed.  If the doCancel() method returns
     *  a value of tue, the dialog will be dismissed, and isOK() will subsequently be false.
     *  If on the other hand, the doCancel() buton returns false, then no action is taken, the dialog
     *  is not disposed.
     * </p>
     */
    protected boolean doCancel(){
        return true;
    }
    
    /**
     * Called when the OK button is selected.
     * If a false is returned, the dialog will not be disposed, and the OK flag will not be set to true.
     * <p>
     *  If you are in need of a dialog that checks the contents of it's controlls, for proper useage,
     *  you may wish to sub class this dialog.  The dialog will call the doOK() when the OK button 
     *  is selected by the user, but before the dialog is dismissed.  If the doOK() method returns
     *  a value of tue, the dialog will be dismissed, and isOK() will subsequently be true.
     *  If on the other hand, the doOK() buton returns false, then no action is taken, the dialog
     *  is not disposed.
     * </p>
     */
    protected boolean doOK(){
        return true;
    }
    
    /**
     * Returns the Cancel Button.
     * <p>
     *  This allows the programmer to change the behavior, look, feel, and visibility of the cancel
     *  button if they wish.  Perhaps they want a dialog with only an ok, then
     *  tempDialog.getCancelButton().setVisible(false), would be appropriate.
     * </p>
     */
    public JButton getCancelButton(){
        return myCancelButton;
    }
    
    /**
     * Return a reference to the Contents Panel.
     * <p>
     * This method allows you to create your own container, and set it as the contents of the dialog.
     * The ContentPane will take up all the space above the buttons.
     * </p>
     */
    public Container getContentPane(){
        return myContentsPanel;
    }
    
    /**
     * Set the contentpane to the given component.
     * <p>
     * This method allows you to create your own container, and set it as the contents of the dialog.
     * The ContentPane will take up all the space above the buttons.
     * </p>
     */
    public void setContentPane(Container inComponent){
        Container p = super.getContentPane();
        p.remove(myContentsPanel);
        
        // add the contents panel
        p.add(inComponent, BorderLayout.CENTER);
        myContentsPanel = inComponent;
    }
    
    /**
     * Returns true if the OK button was selected.
     */
    public boolean getOK(){
        return myOK;
    }
    
    /**
     * Returns the OK Button.
     * <p>
     *  This allows the programmer to change the behavior, look, feel, and visibility of the OK
     *  button if they wish.  Perhaps they want a dialog with a DO IT button, then
     *  tempDialog.getOKButton().setText("DO IT"), would be appropriate.
     * </p>
     */
    public JButton getOKButton(){
        return myOKButton;
    }
    
    /**
     * Setup the display properties of the panel.
     */
    private void initPanel(){
        // create a border layout with the choice in the top, and the panel in the bottom.
        Container p = super.getContentPane();
        p.setLayout(new BorderLayout(2,2));
        
        // add the contents panel
        p.add(myContentsPanel, BorderLayout.CENTER);
        
        // add the OK and cancel button
        JPanel tempButtonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        
        myOKButton = new JButton("OK");
        myCancelButton = new JButton("Cancel");
        
        // set the size of the OK button to that of the cancel button.
        // keeps them the same size and looks beter.
        myOKButton.setPreferredSize(myCancelButton.getPreferredSize());
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        tempButtonPanel.add(myOKButton, c);
        c.weightx = 0;
        c.gridx = 1;
        tempButtonPanel.add(myCancelButton, c);
        p.add(tempButtonPanel, BorderLayout.SOUTH);
        
        // Listen to the buttons
        myOKButton.addActionListener(this);
        myCancelButton.addActionListener(this);
        
        // set the width
        setSize(300,400);
        
    }
    
    /**
     * Returns true if the OK button was selected.
     */
    public boolean isOK(){
        return myOK;
    }
    
    /**
     * Setup the display properties of the panel.
     */
    protected void resetSize(){
        // set the size based on the components.
        pack();
        centerDialog();
    }
    /**
     * center the dialog on the screen.
     */
    protected void centerDialog(){
        // set the size of the screen
        Dimension tempScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension tempSize = this.getSize();
        if (tempSize.width > tempScreenSize.width)
            tempSize.width = tempScreenSize.width;
        if (tempSize.height > tempScreenSize.height)
            tempSize.height = tempScreenSize.height;
        this.setLocation((tempScreenSize.width - tempSize.width) / 2, (tempScreenSize.height - tempSize.height) / 2);
    }
    
    /**
     * Sets the determiner if the OK button was selected.
     */
    public void setOK(boolean inOK){
        myOK = inOK;
    }

    /**
     * Testing
     * main entrypoint - starts the part when it is run as an application
     */
    public static void main(java.lang.String[] args) {
        try {
            GISToolkitDialog tempDialog = new GISToolkitDialog(); // change this line for your dialog.
            tempDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                };
            });
            tempDialog.setVisible(true);
        } catch (Throwable exception) {
            System.err.println("Exception occurred in main() of GISToolkitDialog");
            exception.printStackTrace(System.out);
        }
    }

}