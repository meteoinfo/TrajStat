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
package org.meteothink.trajstat.trajectory;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.time.format.DateTimeFormatter;

import org.meteoinfo.geoprocess.GeometryUtil;
import org.meteoinfo.geoprocess.analysis.DistanceType;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointD;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.common.util.GlobalUtil;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author Yaqiang Wang
 */
public class TrajUtil {

    /**
     * Trajectory calculation
     *
     * @param trajConfig Trajectory configure
     * @throws IOException
     * @throws InterruptedException
     */
    public static void trajCal(TrajConfig trajConfig) throws IOException, InterruptedException {
        String pluginDir = GlobalUtil.getAppPath(TrajUtil.class);
        String workDir = pluginDir + File.separator + "working";
        String cfn = workDir + File.separator + "CONTROL";

        //Loop
        int dayNum = trajConfig.getDayNum();
        int hourNum = trajConfig.getStartHoursNum();
        //int tnum = dayNum * hourNum;
        for (int i = 0; i < dayNum; i++) {
            for (int j = 0; j < hourNum; j++) {
                //Write control file
                trajConfig.upateStartTime(i, j);
                trajConfig.saveControlFile(cfn);

                //Run trajectory module
                ProcessBuilder pb = new ProcessBuilder(trajConfig.getTrajExcuteFileName());
                pb.directory(new File(workDir));
                Process process = pb.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    System.out.println(s);
                }
                process.waitFor();
            }
        }
    }

    /**
     * Convert trajectory end point file to TGS file
     *
     * @param trajfn Trajectory end point file
     * @param tgsfn TGS file
     * @throws IOException
     */
    public static void trajToTGS(String trajfn, String tgsfn) throws IOException {
        if (!new File(trajfn).exists()) {
            return;
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(tgsfn)));
        bw.write("start_year,start_month,start_day,start_hour,year,month,day,hour,age_hour,latitude,longitude,height,press");
        bw.newLine();

        BufferedReader sr = new BufferedReader(new FileReader(new File(trajfn)));

        //Record #1
        String aLine = sr.readLine().trim();
        String[] dataArray = aLine.split("\\s+");
        int meteoFileNum = Integer.parseInt(dataArray[0]);

        //Record #2
        int m, n;
        for (m = 0; m < meteoFileNum; m++) {
            sr.readLine();
        }

        //Record #3
        aLine = sr.readLine().trim();
        dataArray = aLine.split("\\s+");
        int trajNum = Integer.parseInt(dataArray[0]);

        //Record #4             
        String[] sDates = new String[trajNum];
        for (m = 0; m < trajNum; m++) {
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            aLine = "";
            for (n = 0; n <= 3; n++) {
                if (dataArray[n].length() < 2) {
                    dataArray[n] = "0" + dataArray[n];
                }
                aLine = aLine + dataArray[n] + ",";
            }
            sDates[m] = aLine;
        }

        //Record #5
        sr.readLine();

        //Record #6
        int id;
        String wYear, wMonth, wDay, wHour;
        String ageHour, lat, lon, Height, press;
        List<String>[] trajLines = new ArrayList[trajNum];
        for (m = 0; m < trajLines.length; m++) {
            trajLines[m] = new ArrayList();
        }
        //int pNum = 0;
        while (true) {
            aLine = sr.readLine();
            if (aLine == null) {
                break;
            }
            if (aLine.isEmpty()) {
                continue;
            }
            dataArray = aLine.trim().split("\\s+");

            if (dataArray.length < 13) {
                JOptionPane.showMessageDialog(null, "Wrong file format! Please Check!"
                        + System.getProperty("line.separator") + "Line: " + aLine);
                sr.close();
                if (new File(tgsfn).exists()) {
                    new File(tgsfn).delete();
                }
                return;
            }
            //pNum += 1;

            id = Integer.parseInt(dataArray[0]);
            wYear = dataArray[2];
            wMonth = dataArray[3];
            wDay = dataArray[4];
            wHour = dataArray[5];
            ageHour = dataArray[8];
            lat = dataArray[9];
            lon = dataArray[10];
            Height = dataArray[11];
            press = dataArray[12];
            aLine = sDates[id - 1] + wYear + "," + wMonth + "," + wDay + "," + wHour + "," + ageHour + "," + lat + "," + lon + "," + Height + "," + press;
            trajLines[id - 1].add(aLine);
        }
        sr.close();

        for (m = 0; m < trajNum; m++) {
            for (n = 0; n < trajLines[m].size(); n++) {
                bw.write(trajLines[m].get(n));
                bw.newLine();
            }
        }
        bw.close();
    }

    /**
     * Convert trajectory end point file to TGS file
     *
     * @param trajfns Trajectory end point files
     * @param tgsfn TGS file
     * @throws IOException
     */
    public static void trajToTGS(List<String> trajfns, String tgsfn) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(tgsfn)));
        bw.write("start_year,start_month,start_day,start_hour,year,month,day,hour,age_hour,latitude,longitude,height,press");
        bw.newLine();

        for (String trajfn : trajfns) {
            if (!new File(trajfn).exists()) {
                continue;
            }

            BufferedReader sr = new BufferedReader(new FileReader(new File(trajfn)));

            //Record #1
            String aLine = sr.readLine().trim();
            String[] dataArray = aLine.split("\\s+");
            int meteoFileNum = Integer.parseInt(dataArray[0]);

            //Record #2
            int m, n;
            for (m = 0; m < meteoFileNum; m++) {
                sr.readLine();
            }

            //Record #3
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            int trajNum = Integer.parseInt(dataArray[0]);

            //Record #4             
            String[] sDates = new String[trajNum];
            for (m = 0; m < trajNum; m++) {
                aLine = sr.readLine().trim();
                dataArray = aLine.split("\\s+");
                aLine = "";
                for (n = 0; n <= 3; n++) {
                    if (dataArray[n].length() < 2) {
                        dataArray[n] = "0" + dataArray[n];
                    }
                    aLine = aLine + dataArray[n] + ",";
                }
                sDates[m] = aLine;
            }

            //Record #5
            sr.readLine();

            //Record #6
            int id;
            String wYear, wMonth, wDay, wHour;
            String ageHour, lat, lon, Height, press;
            List<String>[] trajLines = new ArrayList[trajNum];
            for (m = 0; m < trajLines.length; m++) {
                trajLines[m] = new ArrayList();
            }
            //int pNum = 0;
            while (true) {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                if (aLine.isEmpty()) {
                    continue;
                }
                dataArray = aLine.trim().split("\\s+");

                if (dataArray.length < 13) {
                    JOptionPane.showMessageDialog(null, "Wrong file format! Please Check!"
                            + System.getProperty("line.separator") + "Line: " + aLine);
                    sr.close();
                    if (new File(tgsfn).exists()) {
                        new File(tgsfn).delete();
                    }
                    return;
                }
                //pNum += 1;

                id = Integer.parseInt(dataArray[0]);
                wYear = dataArray[2];
                wMonth = dataArray[3];
                wDay = dataArray[4];
                wHour = dataArray[5];
                ageHour = dataArray[8];
                lat = dataArray[9];
                lon = dataArray[10];
                Height = dataArray[11];
                press = dataArray[12];
                aLine = sDates[id - 1] + wYear + "," + wMonth + "," + wDay + "," + wHour + "," + ageHour + "," + lat + "," + lon + "," + Height + "," + press;
                trajLines[id - 1].add(aLine);
            }

            sr.close();

            for (m = 0; m < trajNum; m++) {
                for (n = 0; n < trajLines[m].size(); n++) {
                    bw.write(trajLines[m].get(n));
                    bw.newLine();
                }
            }
        }

        bw.close();
    }

    /**
     * Convert trajectory endpoint files to TGS files
     *
     * @param trajConfig Trajectory configure
     * @throws IOException
     */
    public static void trajToTGS(TrajConfig trajConfig) throws IOException {
        int dayNum = trajConfig.getDayNum();
        int hourNum = trajConfig.getStartHoursNum();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        for (int i = 0; i < dayNum; i++) {
            trajConfig.upateStartTime(i, 0);
            String tgsfn = trajConfig.getOutPath() + format.format(trajConfig.getStartTime())
                    + ".tgs";
            List<String> trajfns = new ArrayList<String>();
            for (int j = 0; j < hourNum; j++) {
                trajConfig.upateStartTime(i, j);
                String trajfn = trajConfig.getOutPath() + trajConfig.getTrajFileName();
                trajfns.add(trajfn);
            }
            trajToTGS(trajfns, tgsfn);
        }
    }

    /**
     * Join TGS files
     *
     * @param trajConfig Trajectory configure
     * @return 
     * @throws IOException
     */
    public static String joinTGSFiles(TrajConfig trajConfig) throws IOException {
        int dayNum = trajConfig.getDayNum();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMM");
        String monthfn = trajConfig.getOutPath() + format.format(trajConfig.getStartTime()) + ".tgs";
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(monthfn)));
        bw.write("start_year,start_month,start_day,start_hour,year,month,day,hour,age_hour,latitude,longitude,height,press");
        bw.newLine();
        format = DateTimeFormatter.ofPattern("yyyyMMdd");
        for (int i = 0; i < dayNum; i++) {
            trajConfig.upateStartTime(i, 0);
            String tgsfn = trajConfig.getOutPath() + format.format(trajConfig.getStartTime()) + ".tgs";
            if (!new File(tgsfn).exists()) {
                continue;
            }
            BufferedReader sr = new BufferedReader(new FileReader(new File(tgsfn)));
            sr.readLine();
            String aLine = sr.readLine();
            while (aLine != null) {
                if (aLine.isEmpty()) {
                    aLine = sr.readLine();
                }
                bw.write(aLine);
                bw.newLine();
                aLine = sr.readLine();
            }
            sr.close();
        }
        bw.close();

        return monthfn;
    }

    /**
     * Join TGS files
     *
     * @param tgsfns TGS files
     * @param joinedfn Joined file name
     * @throws IOException
     */
    public static void joinTGSFiles(List<String> tgsfns, String joinedfn) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(joinedfn)));
        bw.write("start_year,start_month,start_day,start_hour,year,month,day,hour,age_hour,latitude,longitude,height,press");
        bw.newLine();
        for (int i = 0; i < tgsfns.size(); i++) {
            String tgsfn = tgsfns.get(i);
            if (!new File(tgsfn).exists()) {
                continue;
            }
            BufferedReader sr = new BufferedReader(new FileReader(new File(tgsfn)));
            sr.readLine();
            String aLine = sr.readLine();
            while (aLine != null) {
                if (aLine.isEmpty()) {
                    aLine = sr.readLine();
                }
                bw.write(aLine);
                bw.newLine();
                aLine = sr.readLine();
            }
            sr.close();
        }
        bw.close();
    }

    /**
     * Convert TGS file to shape file
     *
     * @param tgsFile The TGS file
     * @param shpFile The shape file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     * @return The vector layer
     */
    public static VectorLayer convertToShapeFile(String tgsFile, String shpFile) throws FileNotFoundException, IOException, Exception {
        LocalDateTime sDate = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        String aLine;
        String sMonth;
        String sDay;
        String sHour;
        float height = 0.0f;

        BufferedReader sr = new BufferedReader(new FileReader(new File(tgsFile)));

        sr.readLine();

        VectorLayer aLayer = new VectorLayer(ShapeTypes.PolylineZ);
        aLayer.editAddField("ID", DataType.INT);
        aLayer.editAddField("Date", DataType.DATE);
        aLayer.editAddField("Year", DataType.INT);
        aLayer.editAddField("Month", DataType.INT);
        aLayer.editAddField("Day", DataType.INT);
        aLayer.editAddField("Hour", DataType.INT);
        aLayer.editAddField("Height", DataType.FLOAT);

        int i = 0;
        List<PointZ> pList = new ArrayList<>();
        while (true) {
            aLine = sr.readLine();
            if (aLine == null) {
                break;
            }
            String[] lineArray = aLine.split(",");
            if (lineArray.length < 13) {
                continue;
            }
            String sYear = lineArray[0];
            if (Integer.parseInt(sYear) < 40) {
                sYear = "20" + sYear;
            } else {
                sYear = "19" + sYear;
            }

            String ageHour = lineArray[8];
            float lat = Float.parseFloat(lineArray[9]);
            float lon = Float.parseFloat(lineArray[10]);
            float alt = Float.parseFloat(lineArray[11]);
            float press = Float.parseFloat(lineArray[12]);

            if (ageHour.equals("0.0")) {
                if (i > 0 && pList.size() > 1) {
                    PolylineZShape aPolylineZ = new PolylineZShape();
                    aPolylineZ.setPoints(pList);
                    aPolylineZ.setValue(0);
                    aPolylineZ.setExtent(GeometryUtil.getPointsExtent(pList));
                    int shapeNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPolylineZ, shapeNum)) {
                        aLayer.editCellValue("ID", shapeNum, shapeNum + 1);
                        aLayer.editCellValue("Date", shapeNum, sDate);
                        aLayer.editCellValue("Year", shapeNum, sDate.getYear());
                        aLayer.editCellValue("Month", shapeNum, sDate.getMonthValue());
                        aLayer.editCellValue("Day", shapeNum, sDate.getDayOfMonth());
                        aLayer.editCellValue("Hour", shapeNum, sDate.getHour());
                        aLayer.editCellValue("Height", shapeNum, height);
                    }
                }
                sMonth = lineArray[1];
                sDay = lineArray[2];
                sHour = lineArray[3];
                height = Float.parseFloat(lineArray[11]);
                sDate = LocalDateTime.parse(sYear + "-" + sMonth + "-" + sDay + " " + sHour, format);
                pList = new ArrayList<>();
            }
            PointZ aPoint = new PointZ();
            aPoint.X = lon;
            aPoint.Y = lat;
            aPoint.Z = alt;
            aPoint.M = press;
            if (pList.size() > 1) {
                PointZ oldPoint = pList.get(pList.size() - 1);
                if (Math.abs(aPoint.X - oldPoint.X) > 100) {
                    if (aPoint.X > oldPoint.X) {
                        aPoint.X -= 360;
                    } else {
                        aPoint.X += 360;
                    }
                }
            }
            pList.add(aPoint);

            i += 1;
        }
        sr.close();

        if (i > 1 && pList.size() > 0) {
            PolylineZShape aPolylineZ = new PolylineZShape();
            aPolylineZ.setPoints(pList);
            aPolylineZ.setValue(0);
            aPolylineZ.setExtent(GeometryUtil.getPointsExtent(pList));
            int shapeNum = aLayer.getShapeNum();
            if (aLayer.editInsertShape(aPolylineZ, shapeNum)) {
                aLayer.editCellValue("ID", shapeNum, shapeNum + 1);
                aLayer.editCellValue("Date", shapeNum, sDate);
                aLayer.editCellValue("Year", shapeNum, sDate.getYear());
                aLayer.editCellValue("Month", shapeNum, sDate.getMonthValue());
                aLayer.editCellValue("Day", shapeNum, sDate.getDayOfMonth());
                aLayer.editCellValue("Hour", shapeNum, sDate.getHour());
                aLayer.editCellValue("Height", shapeNum, height);
            }
        }

        if (aLayer.getShapeNum() > 0) {
            aLayer.setLayerName(new File(shpFile).getName());
            LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.black, 1.0f);
            aLS.setFieldName("Year");
            aLayer.setLegendScheme(aLS);
            aLayer.setLayerDrawType(LayerDrawType.TrajLine);
            aLayer.setFileName(shpFile);
            aLayer.saveFile(shpFile);
            return aLayer;
        } else {
            //JOptionPane.showMessageDialog(null, "No valid shapes created.");
            return null;
        }
    }

    /**
     * Remove trajectory intermediate files
     *
     * @param trajConfig Trajectory configure
     */
    public static void removeTrajFiles(TrajConfig trajConfig) {
        int dayNum = trajConfig.getDayNum();
        int hourNum = trajConfig.getStartHoursNum();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        for (int i = 0; i < dayNum; i++) {
            trajConfig.upateStartTime(i, 0);
            String tgsfn = trajConfig.getOutPath() + format.format(trajConfig.getStartTime()) + ".tgs";
            File tgsf = new File(tgsfn);
            if (tgsf.exists()) {
                tgsf.delete();
            }
            for (int j = 0; j < hourNum; j++) {
                trajConfig.upateStartTime(i, j);
                String trajfn = trajConfig.getOutPath() + trajConfig.getTrajFileName();
                File trajf = new File(trajfn);
                if (trajf.exists()) {
                    trajf.delete();
                }
            }
        }
    }

    /**
     * Calculate cluster mean trajectories
     *
     * @param clusters The cluster list
     * @param CLev Cluster level
     * @param pointNum Endpoint number of one trajectory
     * @param layers Trajectory layers
     * @return Mean trajectory data of all clusters
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static double[][] calMeanTrajs(List<Integer> clusters, int CLev, int pointNum, List<VectorLayer> layers) throws FileNotFoundException, IOException {
        int M;
        int cluster;
        int i, j;
        M = pointNum * 3;
        double[][] trajDataArray = new double[CLev][M];
        //---- Mean Trajectories
        int[] trajNumArray = new int[CLev];
        //Initialize Trajectory number of each cluster
        for (i = 0; i < CLev; i++) {
            trajNumArray[i] = 0;
            for (j = 0; j < M; j++) {
                trajDataArray[i][j] = 0.0;
            }
        }
        i = 0;
        for (VectorLayer layer : layers) {
            for (int s = 0; s < layer.getShapeNum(); s++) {
                PolylineZShape shape = (PolylineZShape) layer.getShapes().get(s);
                if (shape.getPointNum() != pointNum) {
                    continue;
                }

                cluster = clusters.get(i);
                int m = 0;
                for (j = 0; j < pointNum; j++) {
                    PointZ point = (PointZ) shape.getPoints().get(j);
                    trajDataArray[cluster - 1][m] += point.Y;
                    m += 1;
                    trajDataArray[cluster - 1][m] += point.X;
                    m += 1;
                    trajDataArray[cluster - 1][m] += point.Z;
                    m += 1;
                }
                trajNumArray[cluster - 1] += 1;
                i += 1;
            }
        }

        for (i = 0; i < CLev; i++) {
            for (j = 0; j < M; j++) {
                trajDataArray[i][j] = trajDataArray[i][j] / trajNumArray[i];
            }
        }

        return trajDataArray;
    }

    /**
     * Calculate total spatial variance - TSV Calculate cluster spatial variance
     * = SPVAR
     *
     * @param clusters The cluster list
     * @param CLev Cluster level
     * @param pointNum Endpoint number of one trajectory
     * @param layers Trajectory layers
     * @param disType Distance type
     * @return Mean trajectory data of all clusters
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static double calTSV(List<Integer> clusters, int CLev, int pointNum, List<VectorLayer> layers,
            DistanceType disType) throws FileNotFoundException, IOException {
        //Calculate mean trajectories
        double[][] trajDataArray = calMeanTrajs(clusters, CLev, pointNum, layers);

        //Calculate TSV
        double tsv = 0.0;
        int cluster;
        int i, j;
        double x, y;
        i = 0;
        for (VectorLayer layer : layers) {
            for (int s = 0; s < layer.getShapeNum(); s++) {
                PolylineZShape shape = (PolylineZShape) layer.getShapes().get(s);
                if (shape.getPointNum() != pointNum) {
                    continue;
                }

                cluster = clusters.get(i);
                double dist;
                int m = 0;
                PointD[] trajA = new PointD[pointNum];
                PointD[] trajB = new PointD[pointNum];
                for (j = 0; j < pointNum; j++) {
                    PointZ point = (PointZ) shape.getPoints().get(j);
                    trajA[j] = new PointD(point.X, point.Y);                    
                    y = trajDataArray[cluster - 1][m];
                    m += 1;
                    x = trajDataArray[cluster - 1][m];
                    m += 2;
                    trajB[j] = new PointD(x, y);                    
                }
                if (disType == DistanceType.EUCLIDEAN)
                    dist = calDistance_Euclidean(trajA, trajB);
                else
                    dist = calDistance_Angle(trajA, trajB);
                tsv += dist;
                i += 1;
            }
        }

        return tsv;
    }

    /**
     * Calculate distance - Euclidean
     * @param trajA Trajectory A
     * @param trajB Trajectory B
     * @return Euclidean distance
     */
    public static double calDistance_Euclidean(PointD[] trajA, PointD[] trajB) {
        double dist = 0.0;
        int n = trajA.length;
        PointD pA, pB;
        for (int j = 0; j < n; j++) {
            pA = trajA[j];
            pB = trajB[j];
            dist += (pA.X - pB.X) * (pA.X - pB.X) + (pA.Y - pB.Y) * (pA.Y - pB.Y);
        }
        dist = Math.sqrt(dist);

        return dist;
    }

    /**
     * Calculate distance - angle
     * @param trajA Trajectory A
     * @param trajB Trajectory B
     * @return Angle distance
     */
    public static double calDistance_Angle(PointD[] trajA, PointD[] trajB) {
        double dist = 0.0;
        double angle;
        int n = trajA.length;
        PointD pA, pB;
        double A, B, C;
        double X0 = trajA[0].X;
        double Y0 = trajB[0].Y;
        for (int j = 1; j < n; j++) {
            pA = trajA[j];
            pB = trajB[j];
            A = Math.pow((pA.X - X0), 2) + Math.pow((pA.Y - Y0), 2);
            B = Math.pow((pB.X - X0), 2) + Math.pow((pB.Y - Y0), 2);
            C = Math.pow((pB.X - pA.X), 2) + Math.pow((pB.Y - pA.Y), 2);
            if (A == 0 | B == 0) {
                angle = 0;
            } else {
                angle = 0.5 * (A + B - C) / Math.sqrt(A * B);
            }
            if ((Math.abs(angle) > 1.0)) {
                angle = 1.0;
            }
            dist += Math.acos(angle);
        }
        dist = dist / n;

        return dist;
    }
}
