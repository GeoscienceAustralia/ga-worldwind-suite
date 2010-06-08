/*
 * SimpleStringEditor.java
 *
 * Created on November 28, 2001, 5:33 PM
 */
package gistoolkit.display.widgets.attrieditors;
import javax.swing.JFormattedTextField;
import gistoolkit.display.widgets.AttributeEditor;
/**
 *
 * @author  ithaqua
 * @version
 */
public class SimpleDoubleEditor extends JFormattedTextField implements AttributeEditor{
    public SimpleDoubleEditor() {
        super(new java.text.DecimalFormat("#.0#######"));
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
