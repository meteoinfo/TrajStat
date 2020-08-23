/* Copyright 2014 - Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteothink.trajstat;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.global.GenericFileFilter;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ui.CheckBoxListEntry;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.FrmLegendSet;
import org.meteoinfo.legend.GroupNode;
import org.meteoinfo.legend.LayerNode;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.plugin.IApplication;
import org.meteoinfo.plugin.PluginBase;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteothink.trajstat.forms.FrmAbout;
import org.meteothink.trajstat.forms.FrmAddData;
import org.meteothink.trajstat.forms.FrmCWT;
import org.meteothink.trajstat.forms.FrmChart;
import org.meteothink.trajstat.forms.FrmClusterCal;
import org.meteothink.trajstat.forms.FrmClusterStat;
import org.meteothink.trajstat.forms.FrmConvertToLine;
import org.meteothink.trajstat.forms.FrmCreateGridLayer;
import org.meteothink.trajstat.forms.FrmMultiSel;
import org.meteothink.trajstat.forms.FrmPSCF;
import org.meteothink.trajstat.forms.FrmToGrid;
import org.meteothink.trajstat.forms.FrmTrajMonth;
import org.meteothink.trajstat.trajectory.TrajUtil;

/**
 *
 * @author Yaqiang Wang
 */
public class Main extends PluginBase {
    // <editor-fold desc="Variables">

    private JMenu trajMenu = null;
    final private String path;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public Main() {
        this.setName("TrajStat");
        this.setAuthor("Yaqiang Wang");
        this.setVersion("1.5");
        this.setDescription("Trajctory statictis plugin");
        path = GlobalUtil.getAppPath(Main.class);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void load() {
        //Add menu
        if (trajMenu == null) {
            trajMenu = new JMenu(this.getName());

            //Load default project
            JMenuItem mi = new JMenuItem("Load Default Project");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onOpenDefaultProjectClick();
                }
            });
            trajMenu.add(mi);
            trajMenu.addSeparator();

            //Trajectory calculation
            mi = new JMenuItem("Calculate Trajectories");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCalculationClick();
                }
            });
            trajMenu.add(mi);
            trajMenu.addSeparator();

            //Convert
            mi = new JMenuItem("Convert To TGS Files");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onConvertToTGSClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("Join TGS File");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onJoinTGSFilesClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("Convert To Shape File");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onConvertToShapeFileClick();
                }
            });
            trajMenu.add(mi);
            trajMenu.addSeparator();

            //Pressure profile
            mi = new JMenuItem("Join Trajectory Layers");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        onJoinTrajLayersClick();
                    } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("Show Press Profile");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onShowPressProfileClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("Set Trajectory Legend");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onSetTrajLegendClick();
                }
            });
            trajMenu.add(mi);
            trajMenu.addSeparator();

            //Trajectory data table
            mi = new JMenuItem("Add Data to Trajectory");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onAddDataToTrajClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("Trajectory Average");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onTrajAveClick();
                }
            });
            trajMenu.add(mi);
            trajMenu.addSeparator();

            //Cluster analysis
//            mi = new JMenuItem("Convert To Line Data");
//            mi.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    onConvertToLineClick();
//                }                
//            });
//            trajMenu.add(mi);

            mi = new JMenuItem("Cluster Calculation");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onClusterAnalysisClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("Cluster Statistics");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onClusterStatClick();
                }
            });
            trajMenu.add(mi);
            trajMenu.addSeparator();

            //Source identify
            mi = new JMenuItem("Create Grid Layer");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCreateGridLayerClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("PSCF Analysis");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onPSCFAnalysisClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("CWT Analysis");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCWTAnalysisClick();
                }
            });
            trajMenu.add(mi);
            
            mi = new JMenuItem("Save to grid");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onSaveToGridClick();
                }
            });
            trajMenu.add(mi);
            trajMenu.addSeparator();

            //About and help
            mi = new JMenuItem("About");
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onAboutClick();
                }
            });
            trajMenu.add(mi);

            mi = new JMenuItem("Help");
//            HelpSet hs = getHelpSet("/trajstat/help/mi.hs");
//            HelpBroker hb = hs.createHelpBroker();
//            //Assign help to components
//            CSH.setHelpIDString(mi, "top");
//            //Handle events
//            mi.addActionListener(new CSH.DisplayHelpFromSource(hb));
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e){
                    onHelpClick();
                }
            });
            trajMenu.add(mi);
        }

        IApplication app = this.getApplication();
        JMenuBar mainMenuBar = app.getMainMenuBar();
        mainMenuBar.add(trajMenu);
        mainMenuBar.validate();
    }

    @Override
    public void unload() {
        if (trajMenu != null) {
            this.getApplication().getMainMenuBar().remove(trajMenu);
            this.getApplication().getMainMenuBar().repaint();
        }
    }

    private void onOpenDefaultProjectClick() {
        IApplication app = this.getApplication();
        //String path = GlobalUtil.getAppPath(Main.class);
        String projectFn = path + File.separator + "default.mip";
        app.openProjectFile(projectFn);
    }

    private void onCalculationClick() {
        IApplication app = this.getApplication();
        FrmTrajMonth frm = new FrmTrajMonth((JFrame) app, false, path);
        frm.setLocationRelativeTo(frm.getParent());
        frm.setVisible(true);
    }

    private void onConvertToTGSClick() {
        IApplication app = this.getApplication();
        JFrame appFrame = (JFrame) app;
        String userPath = System.getProperty("user.dir");
        File pathDir = new File(userPath);
        JFileChooser aDlg = new JFileChooser();
        aDlg.setAcceptAllFileFilterUsed(false);
        aDlg.setCurrentDirectory(pathDir);
        aDlg.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(appFrame)) {
            File[] files = aDlg.getSelectedFiles();
            System.setProperty("user.dir", files[0].getParent());

            appFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (File file : files) {
                String trajfn = file.getAbsolutePath();
                String tgsfn = trajfn + ".tgs";
                try {
                    TrajUtil.trajToTGS(trajfn, tgsfn);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            appFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void onJoinTGSFilesClick() {
        IApplication app = this.getApplication();
        JFrame appFrame = (JFrame) app;
        String userPath = System.getProperty("user.dir");
        File pathDir = new File(userPath);
        JFileChooser aDlg = new JFileChooser();
        String[] fileExts = {"tgs"};
        GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "TGS File (*.tgs)");
        aDlg.setFileFilter(pFileFilter);
        aDlg.setAcceptAllFileFilterUsed(false);
        aDlg.setCurrentDirectory(pathDir);
        aDlg.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(appFrame)) {
            File[] files = aDlg.getSelectedFiles();
            System.setProperty("user.dir", files[0].getParent());

            appFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            List<String> tgsfns = new ArrayList<>();
            for (File file : files) {
                tgsfns.add(file.getAbsolutePath());
            }

            aDlg.setMultiSelectionEnabled(false);
            if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(appFrame)) {
                File sFile = aDlg.getSelectedFile();
                try {
                    TrajUtil.joinTGSFiles(tgsfns, sFile.getAbsolutePath());
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            appFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void onConvertToShapeFileClick() {
        final IApplication app = this.getApplication();
        final JFrame appFrame = (JFrame) app;
        String userPath = System.getProperty("user.dir");
        File pathDir = new File(userPath);
        JFileChooser aDlg = new JFileChooser();
        String[] fileExts = {"tgs"};
        GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "TGS File (*.tgs)");
        aDlg.setFileFilter(pFileFilter);
        aDlg.setAcceptAllFileFilterUsed(false);
        aDlg.setCurrentDirectory(pathDir);
        aDlg.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(appFrame)) {
            final File[] files = aDlg.getSelectedFiles();
            System.setProperty("user.dir", files[0].getParent());

            SwingWorker worker = new SwingWorker<String, String>() {
                @Override
                protected String doInBackground() throws Exception {
                    //Show progress bar
                    app.getProgressBar().setVisible(true);
                    app.getProgressBar().setValue(0);
                    appFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    //Convert
                    int i = 0;
                    for (File file : files) {
                        try {
                            VectorLayer layer = TrajUtil.convertToShapeFile(file.getAbsolutePath(), file.getAbsolutePath().replace(".tgs", ".shp"));
                            if (layer != null) {
                                addTrajLayer(layer, app.getMapDocument().getActiveMapFrame());
                            }
                            app.getProgressBar().setValue((int) ((double) (i + 1) / files.length * 100));
                            i += 1;
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return "";
                }

                @Override
                protected void done() {
                    //Hide progress bar
                    app.getProgressBar().setVisible(false);
                    appFrame.setCursor(Cursor.getDefaultCursor());
                }
            };
            worker.execute();
        }
    }

    /**
     * Add a traject layer into a map frame
     *
     * @param layer The trajectory layer
     * @param mapFrame the map frame
     */
    public static void addTrajLayer(VectorLayer layer, MapFrame mapFrame) {
        int gHandle;
        GroupNode gNode = mapFrame.getGroupByName("Trajectory");
        if (gNode == null) {
            gHandle = mapFrame.addNewGroup("Trajectory");
        } else {
            gHandle = gNode.getGroupHandle();
        }
        mapFrame.addLayer(layer, gHandle);
    }

    /**
     * Add a traject layer into a map frame
     *
     * @param layer The trajectory layer
     * @param mapFrame The map frame
     * @param groupName The group name
     */
    public static void addLayer(VectorLayer layer, MapFrame mapFrame, String groupName) {
        int gHandle;
        GroupNode gNode = mapFrame.getGroupByName(groupName);
        if (gNode == null) {
            gHandle = mapFrame.addNewGroup(groupName);
        } else {
            gHandle = gNode.getGroupHandle();
        }
        mapFrame.addLayer(layer, gHandle);
    }

    private void onJoinTrajLayersClick() throws Exception {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        //Select layers
        int i, j;
        FrmMultiSel frmMultiSel = new FrmMultiSel((JFrame) app, true);
        frmMultiSel.setTitle("Select Layers");
        frmMultiSel.setLabelText("Layers:");
        DefaultListModel listModel = new DefaultListModel();
        for (LayerNode lNode : gNode.getLayers()) {
            listModel.addElement(new CheckBoxListEntry(lNode.getMapLayer(), true));
        }
        frmMultiSel.setListModel(listModel);
        frmMultiSel.setLocationRelativeTo((JFrame) app);
        frmMultiSel.setVisible(true);
        List<VectorLayer> layers = new ArrayList<>();
        if (frmMultiSel.isOK()) {
            listModel = frmMultiSel.getListModel();
            for (i = 0; i < listModel.getSize(); i++) {
                if (((CheckBoxListEntry) listModel.get(i)).isSelected()) {
                    layers.add((VectorLayer) ((CheckBoxListEntry) listModel.get(i)).getValue());
                }
            }

            if (layers.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No layer was selected!");
                return;
            }

            JFrame appFrame = (JFrame) app;
            String userPath = System.getProperty("user.dir");
            File pathDir = new File(userPath);
            JFileChooser aDlg = new JFileChooser();
            String[] fileExts = {"shp"};
            GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "Shape File (*.shp)");
            aDlg.setFileFilter(pFileFilter);
            aDlg.setAcceptAllFileFilterUsed(false);
            aDlg.setCurrentDirectory(pathDir);
            if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(appFrame)) {
                File file = aDlg.getSelectedFile();
                String shpfn = file.getCanonicalPath();
                System.setProperty("user.dir", file.getParent());
                String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
                if (!shpfn.substring(shpfn.length() - extent.length()).equals(extent)) {
                    shpfn = shpfn + "." + extent;
                    file = new File(shpfn);
                }
                VectorLayer outLayer = new VectorLayer(ShapeTypes.PolylineZ);
                for (Field field : layers.get(0).getFields()) {
                    outLayer.editAddField(new Field(field.getColumnName(), field.getDataType()));
                }

                for (VectorLayer layer : layers) {
                    for (i = 0; i < layer.getShapeNum(); i++) {
                        int shapeNum = outLayer.getShapeNum();
                        if (outLayer.editInsertShape((PolylineZShape) layer.getShapes().get(i), shapeNum)) {
                            for (j = 0; j < outLayer.getFieldNumber(); j++) {
                                outLayer.editCellValue(j, shapeNum, layer.getCellValue(j, i));
                            }
                        }
                    }
                }

                if (outLayer.getShapeNum() > 0) {
                    outLayer.setLayerName(file.getName());
                    LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.black, 1.0f);
                    aLS.setFieldName("Year");
                    outLayer.setLegendScheme(aLS);
                    outLayer.setLayerDrawType(LayerDrawType.TrajLine);
                    outLayer.setFileName(shpfn);
                    outLayer.saveFile(shpfn);
                } else {
                    JOptionPane.showMessageDialog(null, "No valid shapes created!");
                    return;
                }

                //Add layer
                addTrajLayer(outLayer, app.getMapDocument().getActiveMapFrame());
            }
        }
    }

    private void onShowPressProfileClick() {
        IApplication app = this.getApplication();
        FrmChart frmChart = new FrmChart((JFrame) app, false);
        frmChart.setLocationRelativeTo((JFrame) app);
        frmChart.setVisible(true);
    }

    private void onSetTrajLegendClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        //Select layers
        int i;
        FrmMultiSel frmMultiSel = new FrmMultiSel((JFrame) app, true);
        frmMultiSel.setTitle("Select Layers");
        frmMultiSel.setLabelText("Layers:");
        DefaultListModel listModel = new DefaultListModel();
        for (LayerNode lNode : gNode.getLayers()) {
            listModel.addElement(new CheckBoxListEntry(lNode.getMapLayer(), true));
        }
        frmMultiSel.setListModel(listModel);
        frmMultiSel.setVisible(true);
        List<VectorLayer> layers = new ArrayList<>();
        if (frmMultiSel.isOK()) {
            listModel = frmMultiSel.getListModel();
            for (i = 0; i < listModel.getSize(); i++) {
                if (((CheckBoxListEntry) listModel.get(i)).isSelected()) {
                    layers.add((VectorLayer) ((CheckBoxListEntry) listModel.get(i)).getValue());
                }
            }

            if (layers.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No layer was selected!");
                return;
            }

            //Set legend
            FrmLegendSet frmLegend = new FrmLegendSet((JFrame) app, true);
            frmLegend.setLegendScheme(layers.get(0).getLegendScheme());
            if (frmLegend.isOK()) {
                LegendScheme aLS = frmLegend.getLegendScheme();
                for (VectorLayer layer : layers) {
                    layer.setLegendScheme(aLS);
                }
            }
            app.getMapDocument().getActiveMapFrame().getMapView().paintLayers();
        }
    }

    private void onAddDataToTrajClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        FrmAddData frmAddData = new FrmAddData((JFrame) app, false);
        frmAddData.setLocationRelativeTo((JFrame) app);
        frmAddData.setVisible(true);
    }

    private void onTrajAveClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        //Select layers
        int i;
        FrmMultiSel frmMultiSel = new FrmMultiSel((JFrame) app, true);
        frmMultiSel.setTitle("Select Layers");
        frmMultiSel.setLabelText("Layers:");
        DefaultListModel listModel = new DefaultListModel();
        for (LayerNode lNode : gNode.getLayers()) {
            listModel.addElement(new CheckBoxListEntry(lNode.getMapLayer(), true));
        }
        frmMultiSel.setListModel(listModel);
        frmMultiSel.setVisible(true);
        List<VectorLayer> layers = new ArrayList<>();
        if (frmMultiSel.isOK()) {
            listModel = frmMultiSel.getListModel();
            for (i = 0; i < listModel.getSize(); i++) {
                if (((CheckBoxListEntry) listModel.get(i)).isSelected()) {
                    layers.add((VectorLayer) ((CheckBoxListEntry) listModel.get(i)).getValue());
                }
            }

            if (layers.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No layer was selected!");
                return;
            }

            //Select a field
            List<String> fldNames = layers.get(0).getFieldNames();
            Object[] selValues = new Object[fldNames.size()];
            for (i = 0; i < selValues.length; i++) {
                selValues[i] = fldNames.get(i);
            }
            Object value = JOptionPane.showInputDialog(null, "Choose a field", "Input", JOptionPane.INFORMATION_MESSAGE,
                    null, selValues, selValues[0]).toString();
            if (value == null) {
                return;
            }
            String fieldName = value.toString();

            //Average trajectory
            List<String> valueList = new ArrayList<>();
            List<List<PointZ>> pointList = new ArrayList<>();
            List<Integer> trajNums = new ArrayList<>();
            String vStr;
            for (VectorLayer layer : layers) {
                for (i = 0; i < layer.getAttributeTable().getNumRecords(); i++) {
                    vStr = layer.getCellValue(fieldName, i).toString();
                    if (vStr.equals("Null")) {
                        continue;
                    }

                    List<PointZ> points = (List<PointZ>) layer.getShapes().get(i).getPoints();
                    if (!valueList.contains(vStr)) {
                        valueList.add(vStr);
                        pointList.add(points);
                        trajNums.add(1);
                    } else {
                        int n = valueList.indexOf(vStr);
                        List<PointZ> ps = pointList.get(n);
                        for (int j = 0; j < points.size(); j++) {
                            if (j >= ps.size()) {
                                break;
                            }
                            PointZ point = ps.get(j);
                            PointZ pp = points.get(j);
                            point.X += pp.X;
                            point.Y += pp.Y;
                            point.Z += pp.Z;
                            point.M += pp.M;
                        }
                        trajNums.set(n, trajNums.get(n) + 1);
                    }
                }
            }
            for (i = 0; i < pointList.size(); i++) {
                List<PointZ> plist = pointList.get(i);
                int n = trajNums.get(i);
                for (int j = 0; j < plist.size(); j++) {
                    PointZ point = plist.get(j);
                    point.X /= n;
                    point.Y /= n;
                    point.Z /= n;
                    point.M /= n;
                }
            }

            //Create average trajectory layer
            String userPath = System.getProperty("user.dir");
            File pathDir = new File(userPath);
            JFileChooser aDlg = new JFileChooser();
            String[] fileExts = {"shp"};
            GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "Shape File (*.shp)");
            aDlg.setFileFilter(pFileFilter);
            aDlg.setAcceptAllFileFilterUsed(false);
            aDlg.setCurrentDirectory(pathDir);
            if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog((JFrame) app)) {
                File file = aDlg.getSelectedFile();
                String shpfn = "";
                try {
                    shpfn = file.getCanonicalPath();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.setProperty("user.dir", file.getParent());
                String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
                if (!shpfn.substring(shpfn.length() - extent.length()).equals(extent)) {
                    shpfn = shpfn + "." + extent;
                }
                VectorLayer trajLayer = new VectorLayer(ShapeTypes.PolylineZ);
                trajLayer.editAddField(fieldName, DataType.STRING);
                for (i = 0; i < pointList.size(); i++) {
                    try {
                        PolylineZShape lineShape = new PolylineZShape();
                        List<PointZ> points = pointList.get(i);
                        lineShape.setPoints(points);
                        lineShape.setExtent(MIMath.getPointsExtent(points));
                        int shapeNum = trajLayer.getShapeNum();
                        if (trajLayer.editInsertShape(lineShape, shapeNum)) {
                            trajLayer.editCellValue(fieldName, shapeNum, valueList.get(i));
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (trajLayer.getShapeNum() > 0) {
                    trajLayer.setLayerName(file.getName());
                    LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.black, 1.0f);
                    aLS.setFieldName("Year");
                    trajLayer.setLegendScheme(aLS);
                    trajLayer.setLayerDrawType(LayerDrawType.TrajLine);
                    trajLayer.setFileName(shpfn);
                    trajLayer.saveFile(shpfn);
                } else {
                    JOptionPane.showMessageDialog(null, "No valid shapes created!");
                    return;
                }

                //Add layer
                addTrajLayer(trajLayer, app.getMapDocument().getActiveMapFrame());
            }
        }
    }

    private void onConvertToLineClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        FrmConvertToLine frm = new FrmConvertToLine((JFrame) app, false);
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }

    private void onClusterAnalysisClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        FrmClusterCal frm = new FrmClusterCal((JFrame) app, false);
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }

    private void onClusterStatClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        FrmClusterStat frm = new FrmClusterStat((JFrame) app, false);
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }

    private void onCreateGridLayerClick() {
        IApplication app = this.getApplication();
        FrmCreateGridLayer frm = new FrmCreateGridLayer((JFrame) app, false);
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }

    private void onPSCFAnalysisClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        FrmPSCF frm = new FrmPSCF((JFrame) app, false);
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }

    private void onCWTAnalysisClick() {
        IApplication app = this.getApplication();
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Trajectory");
        if (gNode == null) {
            JOptionPane.showMessageDialog(null, "No Trajectory group exist!");
            return;
        }
        if (gNode.getLayers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Trajectory layer exist!");
            return;
        }

        FrmCWT frm = new FrmCWT((JFrame) app, false);
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }
    
    private void onSaveToGridClick() {
        IApplication app = this.getApplication();        
        FrmToGrid frm = new FrmToGrid((JFrame) app, false);
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }

    private void onAboutClick() {
        IApplication app = this.getApplication();
        FrmAbout frm = new FrmAbout((JFrame) app, false);
        frm.setVersion(this.getVersion());
        frm.setLocationRelativeTo((JFrame) app);
        frm.setVisible(true);
    }
    
    private void onHelpClick() {
        try {
            URI uri = new URI("http://www.meteothink.org/docs/trajstat/index.html");
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            if (desktop != null) {
                desktop.browse(uri);
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(FrmAbout.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
        }
    }
    
    // </editor-fold>
}
