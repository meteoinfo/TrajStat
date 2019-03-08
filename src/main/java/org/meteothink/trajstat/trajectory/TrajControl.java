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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yaqiang Wang
 */
public class TrajControl {
    // <editor-fold desc="Variables">
    private Date _startTime = new Date();
    private List<Location> _locations = new ArrayList<Location>();
    private int _runHours = -24;
    private int _vertical = 0;
    private float _topOfModel = 10000.0f;
    private List<String> _meteoFiles = new ArrayList();
    private String _outPath;
    private String _trajFileName;
    
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get starting time
     * @return Starting time
     */
    public Date getStartTime(){
        return _startTime;
    }
    
    /**
     * Set starting time
     * @param value Starting time
     */
    public void setStartTime(Date value){
        _startTime = value;
    }
    
    /**
     * Get start locations
     * @return Locations
     */
    public List<Location> getLocations(){
        return _locations;
    }
    
    /**
     * Set locations
     * @param value Locations
     */
    public void setLocations(List<Location> value){
        _locations = value;
    }
    
    /**
     * Get run hours
     * @return Run hours
     */
    public int getRunHours(){
        return _runHours;
    }
    
    /**
     * Set run hours
     * @param value Run hours
     */
    public void setRunHours(int value){
        _runHours = value;
    }
    
    /**
     * Get vertical option
     * @return 
     */
    public int getVertical(){
        return _vertical;
    }
    
    /**
     * Set vertical option
     * @param value Vertical option
     */
    public void setVertical(int value){
        _vertical = value;
    }
    
    /**
     * Get top of model
     * @return Top of model
     */
    public float getTopOfModel(){
        return _topOfModel;
    }
    
    /**
     * Set top of model
     * @param value Top of model
     */
    public void setTopOfModel(float value){
        _topOfModel = value;
    }
    
    /**
     * Get meteorological data files
     * @return Meteorological data files
     */
    public List<String> getMeteoFiles(){
        return _meteoFiles;
    }
    
    /**
     * Set meteorological data files
     * @param value Meteorological data files
     */
    public void setMeteoFiles(List<String> value){
        _meteoFiles = value;
    }
    
    /**
     * Get output path
     * @return Output path
     */
    public String getOutPath(){
        return _outPath;
    }
    
    /**
     * Set output path
     * @param value Ouput path
     */
    public void setOutPath(String value){
        if (value.substring(value.length() - 1).equals(File.separator))
            _outPath = value;
        else
            _outPath = value + File.separator;
    }
    
    /**
     * Get output trajectory file name
     * @return Output trajectory file name
     */
    public String getTrajFileName(){
        return _trajFileName;
    }
    
    /**
     * Set output trajectory file name
     * @param value Output trajectory file name
     */
    public void setTrajFileName(String value){
        _trajFileName = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">   
    /**
     * Set a location
     * @param loc The location
     */
    public void setLocation(Location loc){
        this._locations.clear();
        this._locations.add(loc);
    }
    
    /**
     * Set a location
     * @param loc The location string - lat lon agl
     */
    public void setLocation(String loc){
        setLocation(new Location(loc));
    }
    
    /**
     * Add a location
     * @param loc The location
     */
    public void addLocation(Location loc){
        this._locations.add(loc);
    }
    
    /**
     * Add a location
     * @param loc The location string
     */
    public void addLocation(String loc){
        this._locations.add(new Location(loc));
    }
    
    /**
     * Load CONTROL setting from file
     * @param fileName File name
     */
    public void loadControlFile(String fileName){
        try {
            BufferedReader sr = new BufferedReader(new FileReader(new File(fileName)));
            String stimeStr = sr.readLine().trim();
            SimpleDateFormat format = new SimpleDateFormat("yy MM dd HH");
            this._startTime = format.parse(stimeStr);
            int locnum = Integer.parseInt(sr.readLine());
            this._locations = new ArrayList<Location>();
            String[] dataArray;
            for (int i = 0; i < locnum; i++){
                dataArray = sr.readLine().trim().split("\\s+");
                Location loc = new Location();
                loc.latitude = Float.parseFloat(dataArray[0]);
                loc.longitude = Float.parseFloat(dataArray[1]);
                loc.height = Float.parseFloat(dataArray[2]);
                this._locations.add(loc);
            }
            this._runHours = Integer.parseInt(sr.readLine());
            this._vertical = Integer.parseInt(sr.readLine());
            this._topOfModel = Float.parseFloat(sr.readLine());
            int metFileNum = Integer.parseInt(sr.readLine());
            this._meteoFiles = new ArrayList<String>();
            for (int i = 0; i < metFileNum; i++){
                String path = sr.readLine().trim();
                String fn = sr.readLine().trim();
                this._meteoFiles.add(path + fn);
            }
            this._outPath = sr.readLine().trim();
            this._trajFileName = sr.readLine().trim();
            
            sr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrajControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TrajControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(TrajControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Save trajctory calculation control file 
     * @param fileName File name
     */
    public void saveControlFile(String fileName){
        BufferedWriter sw = null;
        try {
            sw = new BufferedWriter(new FileWriter(new File(fileName)));
            SimpleDateFormat format = new SimpleDateFormat("yy MM dd HH");
            sw.write(format.format(_startTime));
            sw.newLine();
            sw.write(String.valueOf(this._locations.size()));
            sw.newLine();
            for (int i = 0; i < _locations.size(); i++){
                sw.write(this._locations.get(i).toString());
                sw.newLine();
            }
            sw.write(String.valueOf(_runHours));
            sw.newLine();
            sw.write(String.valueOf(_vertical));
            sw.newLine();
            sw.write(String.valueOf(_topOfModel));
            sw.newLine();
            sw.write(String.valueOf(_meteoFiles.size()));
            sw.newLine();
            for (int i = 0; i < _meteoFiles.size(); i++){
                File file = new File(_meteoFiles.get(i));
                sw.write(file.getParent() + File.separator);
                sw.newLine();
                sw.write(file.getName());
                sw.newLine();
            }
            sw.write(this._outPath);
            sw.newLine();
            sw.write(this._trajFileName);            
        } catch (IOException ex) {
            Logger.getLogger(TrajControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sw.close();
            } catch (IOException ex) {
                Logger.getLogger(TrajControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    // </editor-fold>
}
