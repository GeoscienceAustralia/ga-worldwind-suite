/*
 * SimpleStringEditor.java
 *
 * Created on November 28, 2001, 5:33 PM
 */
package gistoolkit.display.widgets.attrieditors;
import javax.swing.JCheckBox;
import gistoolkit.display.widgets.AttributeEditor;
/**
 *
 * @author  ithaqua
 * @version
 */
public class SimpleBooleanEditor extends JCheckBox implements AttributeEditor{
    /** Creates new SimpleStringEditor */
    Object origValue = null;

    public SimpleBooleanEditor() {
        super("bool");
    }
    public void setAttribute(Object inAttribute) throws Exception {
        if (inAttribute instanceof Boolean){
            origValue = inAttribute;
            this.setSelected(((Boolean) inAttribute).booleanValue());
        }
        else throw new Exception("Error, not a Boolean type sent to SimpleBooleanEditor");
    }
    /** Return the edited string */
    public Object getAttribute(){
        try {
            if(this.getSelectedObjects()[0] != null) {
                return new Boolean(true);
            }else{
                return new Boolean(false);
            }
        }catch(Exception nfEx) {
            setSelected(false);
            return new Boolean(false);
        }
    }
}
