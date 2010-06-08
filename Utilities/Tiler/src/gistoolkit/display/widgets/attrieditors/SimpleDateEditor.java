/*
 * SimpleStringEditor.java
 *
 * Created on November 28, 2001, 5:33 PM
 */
package gistoolkit.display.widgets.attrieditors;
import javax.swing.*;
import gistoolkit.display.widgets.AttributeEditor;
/**
 *
 * @author  ithaqua
 * @version
 */
public class SimpleDateEditor extends JFormattedTextField implements AttributeEditor{
    public SimpleDateEditor() {
        super(new java.text.SimpleDateFormat("dd/MM/yyyy"));
    }
    
    /** Set the attribute (Presumably a string) to be edited */
    public void setAttribute(Object inAttribute) {
        setValue(inAttribute);
    }
    /** Return the edited string */
    public Object getAttribute(){
        return getValue();
    }
}
