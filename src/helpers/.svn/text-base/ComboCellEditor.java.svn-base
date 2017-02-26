/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author frank
 */
public class ComboCellEditor implements TableCellEditor, Serializable {
    protected EventListenerList listenerList = new EventListenerList();
    protected transient ChangeEvent changeEvent = null;
    protected JComponent editorComponent = null;

    public Component getComponent() {
        return editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return editorComponent;
    }

    @Override
    public boolean isCellEditable(final EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        if (editorComponent != null && anEvent instanceof MouseEvent && ((MouseEvent) anEvent).getID() == MouseEvent.MOUSE_PRESSED) {
            Component dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, 3, 3);
            MouseEvent e = (MouseEvent) anEvent;
            MouseEvent e2 = new MouseEvent(dispatchComponent, MouseEvent.MOUSE_RELEASED, e.getWhen() + 100000, e.getModifiers(), 3, 3, e.getClickCount(), e.isPopupTrigger());
            dispatchComponent.dispatchEvent(e2);
            e2 = new MouseEvent(dispatchComponent, MouseEvent.MOUSE_CLICKED, e.getWhen() + 100001, e.getModifiers(), 3, 3, 1, e.isPopupTrigger());
            dispatchComponent.dispatchEvent(e2);
        }
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    @Override
    public void addCellEditorListener(final CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }

    @Override
    public void removeCellEditorListener(final CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }

    protected void fireEditingStopped() {
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i+1]).editingStopped(changeEvent);
            }
        }
    }

    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i+1]).editingCanceled(changeEvent);
            }
        }
    }

    // implements TableCellEditor
    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        editorComponent = (JComponent) value;
        return editorComponent;
    }
}
