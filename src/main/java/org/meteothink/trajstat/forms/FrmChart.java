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
package org.meteothink.trajstat.forms;

import org.meteoinfo.chart.*;
import org.meteoinfo.chart.plot.*;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.event.IShapeSelectedListener;
import org.meteoinfo.global.event.ShapeSelectedEvent;
import org.meteoinfo.image.svg.SVGUtil;
import org.meteoinfo.layer.ImageLayer;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.*;
import org.meteoinfo.map.MouseTools;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.plugin.IApplication;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.shape.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class FrmChart extends JDialog {
    // <editor-fold desc="Variables">

    private IApplication app;
    private javax.swing.JToolBar toolBar;
    private ChartPanel chartPanel;
    private Chart chart;
    private javax.swing.JButton button_Sel;
    private javax.swing.JButton button_Remove;
    private javax.swing.JButton button_RemoveAll;
    private javax.swing.JToggleButton button_3D;
    private List<String> dateHeight = new ArrayList<>();
    private List<Object[]> trajShapes = new ArrayList<>();
    private List<MapLayer> mLayers = new ArrayList<>();
    private boolean isSingleLegend = true;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param parent
     * @param modal
     */
    public FrmChart(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        if (parent != null) {
            this.app = (IApplication) parent;
            app.getMapDocument().getActiveMapFrame().getMapView().addShapeSelectedListener(new IShapeSelectedListener() {
                @Override
                public void shapeSelectedEvent(ShapeSelectedEvent event) {
                    onShapeSelected();
                }
            });
        }
        this.setTitle("Pressure profile plot");

        if (parent != null) {
            this.updateMaplayers();
        }

        //Set icon image
        BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass().getResource("/images/TrajStat_Logo.png"));
        } catch (IOException e) {
        }
        this.setIconImage(image);

        if (parent != null)
            this.button_Sel.doClick();
    }

    private void initComponents() {
        toolBar = new javax.swing.JToolBar();
        chartPanel = new ChartPanel(null);
        button_Sel = new javax.swing.JButton();
        button_Remove = new javax.swing.JButton();
        button_RemoveAll = new javax.swing.JButton();
        button_3D = new javax.swing.JToggleButton();

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 400);

        ClassLoader classLoader = FrmChart.class.getClassLoader();

        //Tool bar
        SVGUtil.setSVGIcon(button_Sel, "org/meteothink/trajstat/icons/select.svg", classLoader);
        //button_Sel.setIcon(new FlatSVGIcon("org/meteothink/trajstat/icons/select.svg"));
        button_Sel.setToolTipText("Select Trajectory");
        button_Sel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSelTrajClick(e);
            }
        });
        toolBar.add(button_Sel);

        SVGUtil.setSVGIcon(button_Remove, "org/meteoinfo/icons/remove.svg");
        //button_Remove.setIcon(new FlatSVGIcon("org/meteoinfo/icons/remove.svg"));
        button_Remove.setToolTipText("Remove Last Trajectory");
        button_Remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveClick(e);
            }
        });
        toolBar.add(button_Remove);

        SVGUtil.setSVGIcon(button_RemoveAll, "org/meteoinfo/icons/delete.svg");
        //button_RemoveAll.setIcon(new FlatSVGIcon("org/meteoinfo/icons/delete.svg"));
        button_RemoveAll.setToolTipText("Remove All Trajectories");
        button_RemoveAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveAllClick(e);
            }
        });
        toolBar.add(button_RemoveAll);
        toolBar.addSeparator();

        SVGUtil.setSVGIcon(button_3D, "org/meteothink/trajstat/icons/figure-3d.svg", classLoader);
        button_3D.setToolTipText("3D view");
        button_3D.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                on3DChange(e);
            }
        });
        toolBar.add(button_3D);

        this.add(toolBar, BorderLayout.NORTH);

        //Chart panel                
        chartPanel.setBackground(Color.white);
        chart = new Chart();
        chartPanel.setChart(chart);
        this.add(chartPanel, BorderLayout.CENTER);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    private void updateMaplayers() {
        GroupNode gNode = app.getMapDocument().getActiveMapFrame().getGroupByName("Geo Data");
        if (gNode == null) {
            JOptionPane.showMessageDialog(this, "The layer group not found: Geo Data");
            return;
        }
        this.mLayers.clear();
        for (LayerNode layerNode : gNode.getLayers()) {
            if (layerNode.getMapLayer().isVisible()) {
                this.mLayers.add(layerNode.getMapLayer());
            }
        }
    }

    private void onSelTrajClick(ActionEvent e) {
        app.setCurrentTool((JButton) e.getSource());
        app.getMapDocument().getActiveMapFrame().getMapView().setMouseTool(MouseTools.SelectFeatures_Rectangle);
    }

    private void onShapeSelected() {
        VectorLayer trajLayer = (VectorLayer) app.getMapDocument().getActiveMapFrame().getMapView().getSelectedLayer();
        if (trajLayer != null) {
            if (trajLayer.getShapeType() == ShapeTypes.PolylineZ) {
                this.isSingleLegend = trajLayer.getLegendScheme().getLegendType() != LegendType.UniqueValue;
                int n = 0;
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHH");
                String dhstr;
                for (int i = 0; i < trajLayer.getShapeNum(); i++) {
                    if (trajLayer.getShapes().get(i).isSelected()) {
                        if (trajLayer.getFieldIdxByName("Date") >= 0) {
                            LocalDateTime aDate = (LocalDateTime) trajLayer.getCellValue("Date", i);
                            int hour = Integer.parseInt(trajLayer.getCellValue("Hour", i).toString());
                            aDate = aDate.withHour(hour);
                            dhstr = df.format(aDate);
                            float height = (float) trajLayer.getCellValue("Height", i);
                            dhstr = dhstr + "_" + String.valueOf(height);
                            if (this.dateHeight.contains(dhstr)) {
                                continue;
                            }
                            this.dateHeight.add(dhstr);
                        }

                        PolylineZShape aPLZ = (PolylineZShape) trajLayer.getShapes().get(i);
                        ColorBreak cb = trajLayer.getLegendScheme().getLegendBreaks().get(aPLZ.getLegendIndex());
                        trajShapes.add(new Object[]{aPLZ, cb});
                        n += 1;
                    }
                }
                if (n > 0) {
                    updateChart();
                }
            }
        }
    }

    private void updateChart() {
        if (this.trajShapes.isEmpty()) {
            this.chartPanel.getChart().clearPlots();
            this.chartPanel.paintGraphics();
            return;
        }

        String seriesKey;
        Plot2D plot = new Plot2D();
        int trajN = this.trajShapes.size();
        LegendScheme ls = null;
        if (this.isSingleLegend) {
            ls = LegendManage.createUniqValueLegendScheme(trajN, ShapeTypes.Polyline);
            for (ColorBreak cb : ls.getLegendBreaks()) {
                PolylineBreak plb = (PolylineBreak) cb;
                plb.setDrawSymbol(true);
                plb.setSymbolInterval(6);
                plb.setWidth(2);
            }
        }
        List<ColorBreak> cbs = new ArrayList<>();
        for (int i = 0; i < trajN; i++) {
            PolylineZShape shape = (PolylineZShape) this.trajShapes.get(i)[0];
            if (this.dateHeight.size() > 0 && this.dateHeight.size() > i) {
                seriesKey = this.dateHeight.get(i);
            } else {
                seriesKey = "Line " + String.valueOf(i);
            }
            int n = shape.getPointNum();
            Array xdata = Array.factory(DataType.DOUBLE, new int[]{n});
            Array ydata = Array.factory(DataType.DOUBLE, new int[]{n});
            for (int j = 0; j < n; j++) {
                xdata.setDouble(j, j);
                ydata.setDouble(j, ((PointZ) shape.getPoints().get(j)).M);
            }
            ColorBreak cb;
            if (ls == null) {
                cb = (ColorBreak) this.trajShapes.get(i)[1];
            } else {
                cb = ls.getLegendBreak(i);
                cb.setCaption(seriesKey);
            }
            cbs.add(cb);
            Graphic gg = GraphicFactory.createLineString(xdata, ydata, cb);
            plot.addGraphic(gg);
        }

        plot.setAutoExtent();
        plot.setDrawLegend(true);
        plot.getGridLine().setDrawXLine(true);
        plot.getGridLine().setDrawYLine(true);
        plot.getXAxis().setInverse(false);
        plot.getYAxis().setInverse(true);
        ChartText text = new ChartText("Age Hour");
        text.setXAlign(XAlign.CENTER);
        text.setYAlign(YAlign.TOP);
        plot.getXAxis().setLabel(text);
        text = new ChartText("hPa");
        text.setAngle(90);
        text.setXAlign(XAlign.LEFT);
        text.setYAlign(YAlign.BOTTOM);
        plot.getYAxis().setLabel(text);

        if (ls == null) {
            ls = new LegendScheme(ShapeTypes.Polyline);
            ls.setLegendBreaks(cbs);
        }
        ChartLegend legend = new ChartLegend(ls);
        plot.addLegend(legend);

        chart.clearPlots();
        chart.addPlot(plot);
        this.chartPanel.setMouseMode(MouseMode.SELECT);
        this.chartPanel.paintGraphics();
    }

    private void updateChart3D() {
        if (this.trajShapes.isEmpty()) {
            this.chartPanel.getChart().clearPlots();
            this.chartPanel.paintGraphics();
            return;
        }

        String seriesKey;
        Plot3D plot = new Plot3D();
        for (MapLayer layer : this.mLayers) {
            if (layer.getLayerType() == LayerTypes.VectorLayer) {
                GraphicCollection gcs = GraphicFactory.createGraphicsFromLayer((VectorLayer) layer, 0, 0);
                plot.addGraphic(gcs);
            } else {
                GraphicCollection gcs = GraphicFactory.createImage((ImageLayer) layer, 0, 0, null);
                plot.addGraphic(gcs);
            }
        }
        int trajN = this.trajShapes.size();
        LegendScheme ls = null;
        if (this.isSingleLegend) {
            ls = LegendManage.createUniqValueLegendScheme(trajN, ShapeTypes.Polyline);
            for (ColorBreak cb : ls.getLegendBreaks()) {
                PolylineBreak plb = (PolylineBreak) cb;
                plb.setDrawSymbol(true);
                plb.setSymbolInterval(6);
                plb.setWidth(2);
            }
        }
        PointZ pz;
        Extent3D extent = new Extent3D();
        List<ColorBreak> cbs = new ArrayList<>();
        int idx = 0;
        Array sx = Array.factory(DataType.DOUBLE, new int[]{trajN});
        Array sy = Array.factory(DataType.DOUBLE, new int[]{trajN});
        Array sz = Array.factory(DataType.DOUBLE, new int[]{trajN});
        for (int i = 0; i < trajN; i++) {
            PolylineZShape shape = (PolylineZShape) this.trajShapes.get(i)[0];
            if (this.dateHeight.size() > 0 && this.dateHeight.size() > i) {
                seriesKey = this.dateHeight.get(i);
            } else {
                seriesKey = "Line " + String.valueOf(i);
            }
            PointZ ppz = (PointZ)shape.getPoints().get(0);
            sx.setDouble(i, ppz.X);
            sy.setDouble(i, ppz.Y);
            sz.setDouble(i, ppz.Z);
            ColorBreak cb;
            if (ls == null) {
                cb = (ColorBreak) this.trajShapes.get(i)[1];
            } else {
                cb = ls.getLegendBreak(i);
                cb.setCaption(seriesKey);
            }
            cbs.add(cb);
            for (Polyline line : shape.getPolylines()) {
                int n = line.getPointList().size();
                Array xdata = Array.factory(DataType.DOUBLE, new int[]{n});
                Array ydata = Array.factory(DataType.DOUBLE, new int[]{n});
                Array zdata = Array.factory(DataType.DOUBLE, new int[]{n});
                for (int j = 0; j < n; j++) {
                    pz = (PointZ) line.getPointList().get(j);
                    xdata.setDouble(j, pz.X);
                    ydata.setDouble(j, pz.Y);
                    zdata.setDouble(j, pz.Z);
                }

                Graphic gg = GraphicFactory.createLineString3D(xdata, ydata, zdata, cb);
                plot.addGraphic(gg);
                if (idx == 0) {
                    extent = (Extent3D) gg.getExtent();
                } else {
                    extent = extent.union((Extent3D) gg.getExtent());
                }
                idx += 1;
            }
        }
        PointBreak pb = new PointBreak();
        pb.setStyle(PointStyle.Star);
        pb.setColor(Color.red);
        pb.setSize(14);
        Graphic gg = GraphicFactory.createPoints3D(sx, sy, sz, pb);
        plot.addGraphic(gg);
        extent = extent.extend(0.2);
        extent.minZ = 0;
        plot.setExtent(extent);

        ProjectionInfo proj = app.getMapDocument().getActiveMapFrame().getMapView().getProjection().getProjInfo();
        //plot.getZAxis().setInverse(true);
        ChartText text;
        if (proj.isLonLat()){
            text = new ChartText();
            text.setText("Longitude");
            text.setXAlign(XAlign.CENTER);
            text.setYAlign(YAlign.BOTTOM);
            plot.getXAxis().setLabel(text);
            text = new ChartText();
            text.setText("Latitude");
            text.setXAlign(XAlign.LEFT);
            text.setYAlign(YAlign.CENTER);
            plot.getYAxis().setLabel(text);
        }
        text = new ChartText();
        text.setText("Height (Meter)");
        text.setXAlign(XAlign.CENTER);
        text.setYAlign(YAlign.BOTTOM);
        plot.getZAxis().setLabel(text);

        if (ls == null) {
            ls = new LegendScheme(ShapeTypes.Polyline);
            ls.setLegendBreaks(cbs);
        }
        ChartLegend legend = new ChartLegend(ls);
        legend.setYShift(25);
        plot.addLegend(legend);
        plot.setPosition(0.13, 0.15, 0.71, 0.815);

        chart.clearPlots();
        chart.addPlot(plot);
        this.chartPanel.setMouseMode(MouseMode.ROTATE);
        this.chartPanel.paintGraphics();
    }

    private void onRemoveClick(ActionEvent e) {
        if (this.trajShapes.size() > 0) {
            this.trajShapes.remove(this.trajShapes.size() - 1);
            this.dateHeight.remove(this.dateHeight.size() - 1);
            this.updateChart();
        }
    }

    private void onRemoveAllClick(ActionEvent e) {
        if (this.trajShapes.size() > 0) {
            this.trajShapes.clear();
            this.dateHeight.clear();
            this.updateChart();
        }
    }

    private void on3DChange(ChangeEvent e) {
        if (this.button_3D.isSelected()) {
            this.updateChart3D();
        } else {
            this.updateChart();
        }
    }
    // </editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmClusterCal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FrmChart dialog = new FrmChart(null, true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
}
