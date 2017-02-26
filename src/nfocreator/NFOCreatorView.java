/*
 * NFOCreatorView.java
 */

package nfocreator;

import helpers.ComboCellEditor;
import helpers.ComboCellRenderer;
import helpers.Connection;
import helpers.IdleTask;
import helpers.Lookups;
import helpers.Moviename;
import helpers.NFOTableModel;
import helpers.ThreadTest;
import helpers.XMLCreator;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;

/**
 * The application's main frame.
 */
public class NFOCreatorView extends FrameView {

    public NFOCreatorView(SingleFrameApplication app) {
        super(app);

        initComponents();
        initButtons();
        initFirstMovieTable();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        buttonGroup1.add(adult_bt);
        buttonGroup1.add(imdb_bt);
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = NFOCreatorApp.getApplication().getMainFrame();
            aboutBox = new NFOCreatorAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        NFOCreatorApp.getApplication().show(aboutBox);
    }

    private void initFullMovieTable(){
        this.tableFirst.setVisible(false);
        String[] columnNames = {"Check", "Moviename", "Alternative"};
        this.tableComplete = new JTable(new DefaultTableModel(createDataArray(), columnNames)) {
            @Override
            public TableCellRenderer getCellRenderer(final int row, final int column) {
                TableColumn tableColumn = getColumnModel().getColumn(column);
                TableCellRenderer renderer = tableColumn.getCellRenderer();
                if (renderer == null) {
                    Class c = getColumnClass(column);
                    if (c.equals(Object.class)) {
                        Object o = getValueAt(row, column);
                        if (o != null) {
                            c = getValueAt(row, column).getClass();
                        }
                    }
                    renderer = getDefaultRenderer(c);
                }
                return renderer;
            }

            @Override
            public TableCellEditor getCellEditor(final int row, final int column) {
                TableColumn tableColumn = getColumnModel().getColumn(column);
                TableCellEditor editor = tableColumn.getCellEditor();
                if (editor == null) {
                    Class c = getColumnClass(column);
                    if (c.equals(Object.class)) {
                        Object o = getValueAt(row, column);
                        if (o != null) {
                            c = getValueAt(row, column).getClass();
                        }
                    }
                    editor = getDefaultEditor(c);
                }
                return editor;
            }

            @Override
            public boolean isCellEditable(int row, int col)
            {
                if (col == 0 || col == 2) {
                    return true;
                }
                return false;
            }
        };

        movieScrollPane.add(tableComplete);
        movieScrollPane.setViewportView(tableComplete);
        tableComplete.setName("completeMovieTable");
        tableComplete.getTableHeader().setReorderingAllowed(false);
        this.tableComplete.setColumnSelectionAllowed(false);
        this.tableComplete.setRowHeight(20);
        this.tableComplete.setVisible(true);
        tableComplete.setDefaultRenderer(JComponent.class, new ComboCellRenderer());
        tableComplete.setDefaultEditor(JComponent.class, new ComboCellEditor());
        this.get_btn.setEnabled(true);
        this.setFirstColumn();
    }

    private void initFirstMovieTable(){
        this.tableFirst = new JTable(this.nfoModel);
        movieScrollPane.add(tableFirst);
        movieScrollPane.setViewportView(tableFirst);
        tableFirst.setName("firstMovieTable");
        tableFirst.getTableHeader().setReorderingAllowed(false);
        this.tableFirst.setColumnSelectionAllowed(false);
        this.tableFirst.setRowHeight(20);
        this.nfoModel.addColumn("Check");
        this.nfoModel.addColumn("Moviename");
        setFirstColumn();
    }

    private void initButtons(){
        this.lookup_btn.setEnabled(false);
        this.get_btn.setEnabled(false);
    }

    private void setFirstColumn(){
        TableColumn firstcol = this.tableFirst.getColumnModel().getColumn(0);
        firstcol.setMinWidth(50);
        firstcol.setMaxWidth(50);
        if (this.tableComplete != null) {
            TableColumn completeCol = this.tableComplete.getColumnModel().getColumn(0);
            completeCol.setMinWidth(50);
            completeCol.setMaxWidth(50);
        }
    }

    private Object[][] createDataArray() {
        Object[][] data = new Object[this.linkLookups.size()][];
        int a = 0;
        for (Lookups lookups : this.linkLookups) {
            Object[] rowData = new Object[3];
            JComboBox btn2 = lookups.comboBox;
            rowData[0] = false;
            rowData[1] = lookups.movies[2];
            rowData[2] = btn2;
            if (a <= this.linkLookups.size()) {
                data[a] = rowData;
                a++;
            }
        }

        return data;
    }

    private void fillTableMovie(){
        if (this.linkLookups.size() != 0){
            for (Lookups lookups : this.linkLookups) {
                    this.nfoModel.addRow(new Object[]{new Boolean(false), lookups.movies[2]});
                    this.nfoModel.fireTableDataChanged();
            }
        }
    }

    ///////////////////////////
    public Task startTask() {
        IdleTask task = new IdleTask(org.jdesktop.application.Application.getInstance());

        ApplicationContext C = getApplication().getContext();
        TaskMonitor M = C.getTaskMonitor();
        TaskService S = C.getTaskService();
        S.execute(task);
       // this.taskMonitor.setForegroundTask(task);
        M.setForegroundTask(task);

        return task;
    }

   private Object adultLookWorker() {
        Connection connect = new Connection();
        if (adult_bt.isSelected() && !imdb_bt.isSelected()) {
            for (Lookups lookups : this.linkLookups) {
                    lookups.movieInfo = connect.ScanAdult(lookups.movies[2]).movieInfo;
                    lookups.comboBox = new JComboBox(lookups.movieInfo[0]);
            }
        }
        return "All Done";
    }

 
/////////////////////////////////////////

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        imdb_bt = new javax.swing.JRadioButton();
        adult_bt = new javax.swing.JRadioButton();
        movieScrollPane = new javax.swing.JScrollPane();
        get_btn = new javax.swing.JButton();
        lookup_btn = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        loadMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonGroup1 = new javax.swing.ButtonGroup();
        folderChooser = new javax.swing.JFileChooser();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(nfocreator.NFOCreatorApp.class).getContext().getResourceMap(NFOCreatorView.class);
        imdb_bt.setText(resourceMap.getString("imdb_bt.text")); // NOI18N
        imdb_bt.setName("imdb_bt"); // NOI18N

        adult_bt.setText(resourceMap.getString("adult_bt.text")); // NOI18N
        adult_bt.setName("adult_bt"); // NOI18N

        movieScrollPane.setName("movieScrollPane"); // NOI18N

        get_btn.setText(resourceMap.getString("get_btn.text")); // NOI18N
        get_btn.setName("get_btn"); // NOI18N
        get_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                get_btnActionPerformed(evt);
            }
        });

        lookup_btn.setText(resourceMap.getString("lookup_btn.text")); // NOI18N
        lookup_btn.setName("lookup_btn"); // NOI18N
        lookup_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lookup_btnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imdb_bt)
                    .addComponent(adult_bt))
                .addGap(18, 18, 18)
                .addComponent(lookup_btn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(get_btn)
                .addGap(386, 386, 386))
            .addComponent(movieScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(imdb_bt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(adult_bt))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lookup_btn)
                            .addComponent(get_btn))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(movieScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        loadMenuItem.setText(resourceMap.getString("loadMenuItem.text")); // NOI18N
        loadMenuItem.setName("loadMenuItem"); // NOI18N
        loadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadMenuItem);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(nfocreator.NFOCreatorApp.class).getContext().getActionMap(NFOCreatorView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 417, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        folderChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setName("folderChooser"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void loadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadMenuItemActionPerformed
       if (adult_bt.isSelected()) {
           this.folderChooser.setCurrentDirectory(new File ("D:\\serine\\porn"));
       }
       
       this.folderChooser.setVisible(true);
       if (this.folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            this.openFolder = this.folderChooser.getSelectedFile().getAbsolutePath();
       }
        try {
            linkLookups = Moviename.getInstance().GetMovielist(this.openFolder);
            this.lookup_btn.setEnabled(true);
            this.fillTableMovie();
        } catch (IOException ex) {
            Logger.getLogger(NFOCreatorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_loadMenuItemActionPerformed

    @Action
    private void lookup_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lookup_btnActionPerformed
        final Connection connect = new Connection();


        if (adult_bt.isSelected() && !imdb_bt.isSelected()) {
            if (!busyIconTimer.isRunning()) {
                statusAnimationLabel.setIcon(busyIcons[0]);
                busyIconIndex = 0;
                busyIconTimer.start();
            }

            SwingWorker adultLookupWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    for (Lookups lookups : linkLookups) {
                        //connect.setNAme(lookups.movies[2]);
                        lookups.movieInfo = connect.ScanAdult(lookups.movies[2]).movieInfo;
                        lookups.comboBox = new JComboBox(lookups.movieInfo[0]);
                    }

                    return null;
                }
                @Override
                protected void done(){
                    //beendet die idleIcon animation und füllt die tabelle mit infos
                    initFullMovieTable();
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                }
            };

            adultLookupWorker.execute();
        }


        if (imdb_bt.isSelected() && !adult_bt.isSelected()) {
           // for (String[] buffer : lookups.movies) {
               // try {
               //     connect.ScanIMDB(buffer[2]);
               /* } catch (IOException ex) {
                    Logger.getLogger(NFOCreatorView.class.getName()).log(Level.SEVERE, null, ex);
                }*/
            }
        //}

        //this.initFullMovieTable();
    }//GEN-LAST:event_lookup_btnActionPerformed

    private void get_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_get_btnActionPerformed
        if (adult_bt.isSelected() && !imdb_bt.isSelected()) {
            int progressbarmax = 0;
            for (int i = 0; i < tableComplete.getRowCount(); i++) {
                if (tableComplete.getValueAt(i, 0).equals(true)) {
                    progressbarmax++;
                }
            }

            if ( progressbarmax != 0) {
                progressBar.setMinimum(0);
                progressBar.setMaximum(progressbarmax);
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
            }

            SwingWorker adultGetWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    int activeRowCounter = 0;
                    Lookups lookups;
                    String movieID = "";
                    String movieName = "";
                    String moviePictureUrl = "";
                    String movieCD = "";
                    int value = progressBar.getMinimum();
                    for (int row = 0; row < tableComplete.getRowCount(); row++){
                        if (tableComplete.getValueAt(row, 0).equals(true)) {
                            value++;
                            lookups = linkLookups.get(row);
                            JComboBox buffer = (JComboBox) tableComplete.getValueAt(row, 2);
                            movieID = lookups.movieInfo[1][buffer.getSelectedIndex()];
                            movieName = lookups.movies[2];
                            movieCD = lookups.movies[1];
                            moviePictureUrl = lookups.movieInfo[2][buffer.getSelectedIndex()];
                            String destFolder = openFolder + File.separator + lookups.movies[2];

                            boolean status = false;
                            try {
                                status = Moviename.getInstance().CreateFolder(openFolder + File.separator + movieName);
                                if (status) {
                                status = Moviename.getInstance().MoveFile(movieName, openFolder, destFolder);
                                }

                                if (status) {
                                    XMLCreator.getInstance().CreateNFOAdult(destFolder, movieName, movieCD,movieID);
                                    Connection connect = new Connection();
                                     status = connect.loadPicture(moviePictureUrl, destFolder);
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(NFOCreatorView.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            activeRowCounter++;
                        }

                        progressBar.setValue(value);
                    }

                    return null;
                }

                @Override
                protected void done(){
                    //beendet die progressbar animation
                    progressBar.setVisible(false);
                    progressBar.setValue(0);

                }
            };

            adultGetWorker.execute();
        }
    }//GEN-LAST:event_get_btnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton adult_bt;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JFileChooser folderChooser;
    private javax.swing.JButton get_btn;
    private javax.swing.JRadioButton imdb_bt;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JButton lookup_btn;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JScrollPane movieScrollPane;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    //private final TaskMonitor taskMonitor ;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private String openFolder = "";
    private NFOTableModel nfoModel = new NFOTableModel();
    private LinkedList<Lookups> linkLookups = new LinkedList<Lookups>();

    private JTable tableComplete = null;
    private JTable tableFirst = null;
    private JDialog aboutBox;
}


