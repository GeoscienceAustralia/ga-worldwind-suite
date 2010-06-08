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

package gistoolkit.display.shader;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import gistoolkit.display.widgets.*;

/**
 * Panel for editing the Point Image Renderer.
 */
public class FindImageDialog extends GISToolkitDialog{
    
    /** Path to the image. */
    private String myImagePath = null;
    private FindImageDialog getThis(){return this;}
    private class MyListener implements ActionListener{
        /**
         * Save the file chooser for future reference
         */
        private JFileChooser myChooser = new JFileChooser();
        
        public void actionPerformed(java.awt.event.ActionEvent inAE) {
            if (inAE.getSource() == myButtonLocation){
                if (myChooser == null){
                    myChooser = new JFileChooser();
                }
                String tempString = myTextFieldLocation.getText();
                if ((tempString != null) && (tempString.length() >0)){
                    File tempFile = new File(tempString);
                    if (tempFile.exists()){
                        if (tempFile.isDirectory()){
                            myChooser.setCurrentDirectory(tempFile);
                        }
                        if (tempFile.isFile()){
                            myChooser.setSelectedFile(tempFile);
                        }
                    }
                }
                
                int returnVal = myChooser.showOpenDialog(getThis());
                
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    String tempFile = myChooser.getSelectedFile().getAbsolutePath();
                    myTextFieldLocation.setText(tempFile);
                    try{
                        myImagePanel.setImage(Toolkit.getDefaultToolkit().createImage(tempFile));
                        myImagePath = tempFile;
                    }
                    catch(Exception e){
                    }
                }
            }
        }
    }
    private MyListener myListener = new MyListener();
    
    public String getImageName(){
        return myImagePath;
    }
    public void setImageName(String inImageFileName){
        myImagePath = inImageFileName;
        myTextFieldLocation.setText(inImageFileName);
    }
    public BufferedImage getImage() throws Exception{
        Image tempImage = Toolkit.getDefaultToolkit().createImage(myImagePath);

        // wait for the damn image to load.s 
        Panel tempWaitPanel = new Panel();
        MediaTracker mt = new MediaTracker(tempWaitPanel);
        mt.addImage(tempImage, 0);
        mt.waitForAll();
        
        int tempWidth = tempImage.getWidth(tempWaitPanel);
        int tempHeight = tempImage.getHeight(tempWaitPanel);
        
        // retrieve the pixels
        BufferedImage tempBImage = new BufferedImage(tempWidth, tempHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempGraphics = (Graphics2D) tempBImage.getGraphics();
        tempGraphics.drawImage(tempImage, 0, 0, tempWaitPanel);
        return tempBImage;
    }
    public void setImage(Image inImage){
        myImagePanel.setImage(inImage);
    }
    /**
     * Text field to allow the user to type the location directly.
     */
    private JTextField myTextFieldLocation;
    
    /**
     * Button for looking up a location.
     */
    private JButton myButtonLocation;
    
    /**
     * Image panel for displaying the selected images.
     */
    private ImagePanel myImagePanel = new ImagePanel();
    
    
    /** Creates new PointImageRendererPanel */
    public FindImageDialog() {
        setTitle("Find Image");
        initPanel();
    }
    
    /** laye out the gui widgets on the panel. */
    public void initPanel(){
        Container tempPanel = getContentPane();
        tempPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0;
        
        // create the type in box for the filename
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        JLabel tempLabelFile = new JLabel("File");
        tempPanel.add(tempLabelFile, c);
        
        c.gridx++;
        c.weightx = 1;
        myTextFieldLocation = new JTextField();
        tempPanel.add(myTextFieldLocation, c);
        
        c.gridx++;
        c.weightx = 0;
        myButtonLocation = new JButton("Browse");
        tempPanel.add(myButtonLocation, c);
        myButtonLocation.addActionListener(myListener);
        
        // add the image panel for displaying the selected image
        c.gridx = 0;
        c.gridy ++;
        c.weighty = 1;
        c.gridwidth = 3;
        JScrollPane tempScrollPane = new JScrollPane(myImagePanel);
        tempPanel.add(tempScrollPane, c);
        
        //setContentPane(tempPanel);
        setSize(300, 200);
        centerDialog();
    }
    
    public static void main(String[] inArgs){
        FindImageDialog tempDialog = new FindImageDialog();
        tempDialog.setModal(true);
        tempDialog.show();
        System.exit(0);
    }
}
