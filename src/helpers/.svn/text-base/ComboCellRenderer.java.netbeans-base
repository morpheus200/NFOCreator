/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author frank
 */
public class ComboCellRenderer implements TableCellRenderer {
    private JLabel label = new JLabel();
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
     /*       if(value instanceof JComboBox){
                label.setText(((JComboBox)value).getSelectedItem().toString());
                return label;
            }*/
        return (JComponent) value;
    }
}
