/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteothink.trajstat.forms;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.data.GridData;
import org.meteoinfo.global.GenericFileFilter;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.GroupNode;
import org.meteoinfo.legend.LayerNode;
import org.meteoinfo.plugin.IApplication;
import org.meteoinfo.shape.Shape;

/**
 *
 * @author wyq
 */
public class FrmToGrid extends javax.swing.JDialog {

    private final IApplication app;

    /**
     * Creates new form FrmToGrid
     *
     * @param parent
     * @param modal
     */
    public FrmToGrid(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        app = (IApplication) parent;

        //Set PSCF and CWT layers
        GroupNode pNode = app.getMapDocument().getActiveMapFrame().getGroupByName("PSCF");
        this.jComboBox_Layer.removeAllItems();
        for (LayerNode lNode : pNode.getLayers()) {
            this.jComboBox_Layer.addItem(lNode.getMapLayer());
        }
        pNode = app.getMapDocument().getActiveMapFrame().getGroupByName("CWT");
        for (LayerNode lNode : pNode.getLayers()) {
            this.jComboBox_Layer.addItem(lNode.getMapLayer());
        }
        if (this.jComboBox_Layer.getItemCount() > 0) {
            this.jComboBox_Layer.setSelectedIndex(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox_Layer = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jComboBox_Field = new javax.swing.JComboBox();
        jButton_Save = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("PSCF/CWT Layer:");

        jComboBox_Layer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_Layer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_LayerActionPerformed(evt);
            }
        });

        jLabel2.setText("Field:");

        jComboBox_Field.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton_Save.setText("Save");
        jButton_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_Save)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jComboBox_Layer, 0, 164, Short.MAX_VALUE)
                        .addComponent(jComboBox_Field, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox_Layer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton_Save)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SaveActionPerformed
        // TODO add your handling code here:
        Object obj = this.jComboBox_Layer.getSelectedItem();
        if (obj != null) {
            VectorLayer layer = (VectorLayer) obj;
            //Get grid x y data
            int sn = layer.getShapeNum();
            int xn = 0;
            double oldx = Double.NEGATIVE_INFINITY;
            double x;
            for (Shape shape : layer.getShapes()) {
                x = shape.getPoints().get(0).X;
                if (x < oldx) {
                    break;
                } else {
                    oldx = x;
                }
                xn += 1;
            }

            int yn = sn / xn;
            int idx = sn - xn;
            Shape shp = layer.getShapes().get(idx);
            PointD point = shp.getExtent().getCenterPoint();
            double minx = point.X;
            double miny = point.Y;
            PointD rpoint = layer.getShapes().get(idx + 1).getExtent().getCenterPoint();
            double xdelta = rpoint.X - minx;
            PointD tpoint = layer.getShapes().get(idx - xn).getExtent().getCenterPoint();
            double ydelta = tpoint.Y - miny;

            // Create GridData and set value
            GridData gData = new GridData(minx, xdelta, xn, miny, ydelta, yn);
            String fieldName = this.jComboBox_Field.getSelectedItem().toString();
            double v;
            for (int i = 0; i < yn; i++) {
                for (int j = 0; j < xn; j++) {
                    idx = (yn - i - 1) * xn + j;
                    v = (double) layer.getCellValue(fieldName, idx);
                    gData.data[i][j] = v;
                }
            }

            // Output GridData file
            String userPath = System.getProperty("user.dir");
            File pathDir = new File(userPath);
            JFileChooser aDlg = new JFileChooser();
            String[] fileExts = {"dat"};
            GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "Surfer ASCII file (*.dat)");
            aDlg.setFileFilter(pFileFilter);
            aDlg.setAcceptAllFileFilterUsed(false);
            aDlg.setCurrentDirectory(pathDir);
            if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog((JFrame) app)) {
                String fileName = aDlg.getSelectedFile().getAbsolutePath();
                String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
                if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                    fileName = fileName + "." + extent;
                }
                gData.saveAsSurferASCIIFile(fileName);
            }
        }
    }//GEN-LAST:event_jButton_SaveActionPerformed

    private void jComboBox_LayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_LayerActionPerformed
        // TODO add your handling code here:
        Object obj = this.jComboBox_Layer.getSelectedItem();
        if (obj != null) {
            VectorLayer layer = (VectorLayer) obj;
            this.jComboBox_Field.removeAllItems();
            for (Field field : layer.getFields()) {
                if (field.isNumeric()) {
                    this.jComboBox_Field.addItem(field.getColumnName());
                }
            }
            if (this.jComboBox_Field.getItemCount() > 0) {
                this.jComboBox_Field.setSelectedIndex(this.jComboBox_Field.getItemCount() - 1);
            }
        }
    }//GEN-LAST:event_jComboBox_LayerActionPerformed

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmToGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmToGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmToGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmToGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmToGrid dialog = new FrmToGrid(new javax.swing.JFrame(), true);
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Save;
    private javax.swing.JComboBox jComboBox_Field;
    private javax.swing.JComboBox jComboBox_Layer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}
