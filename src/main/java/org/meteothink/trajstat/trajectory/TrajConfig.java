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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.meteoinfo.global.util.GlobalUtil;

/**
 *
 * @author Yaqiang Wang
 */
public class TrajConfig extends TrajControl {
    // <editor-fold desc="Variables">
    private List<Integer> _startHours = new ArrayList<Integer>();
    private int _startDay = 1;
    private int _endDay = 30;
    private String _trajExcuteFileName;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public TrajConfig(){
        super();
        _startHours.add(6);
        String pluginDir = GlobalUtil.getAppPath(TrajUtil.class);
        String workDir = pluginDir + File.separator + "working";
        _trajExcuteFileName = workDir + File.separator + "hyts_std.exe";        
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get start hours
     * @return Start hours
     */
    public List<Integer> getStartHours(){
        return _startHours;
    }
    
    /**
     * Set start hours
     * @param value Start hours
     */
    public void setStartHours(List<Integer> value){
        _startHours = value;
    }
    
    /**
     * Get start day of the month
     * @return Start day
     */
    public int getStartDay(){
        return _startDay;
    }
    
    /**
     * Set start day of the month
     * @param value Start day
     */
    public void setStartDay(int value){
        _startDay = value;
    }
    
    /**
     * Get end day of the month
     * @return End day
     */
    public int getEndDay(){
        return _endDay;
    }
    
    /**
     * Set end day of the month
     * @param value End day
     */
    public void setEndDay(int value){
        _endDay = value;
    }
    
    /**
     * Get trajectory excute file name
     * @return Trajectory excute file name
     */
    public String getTrajExcuteFileName(){
        return _trajExcuteFileName;
    }
    
    /**
     * Set trajectory excute file name
     * @param value Trajectory excute file name
     */
    public void setTrajExcuteFileName(String value){
        _trajExcuteFileName = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get start hours string
     * @return Start hours string
     */
    public String getStartHoursString(){
        String str = "";
        for (int hour : _startHours){            
            str = str + " " + String.format("%1$2d", hour);
        }
        str = str.trim();
        
        return str;
    }
    
    /**
     * Read start hours from the string
     * @param value String
     */
    public void setStartHours(String value){
        String[] strs = value.trim().split("\\s+");
        this._startHours.clear();
        for (String str : strs){
            this._startHours.add(Integer.parseInt(str));
        }
    }
    
    /**
     * Update start and end days by year month
     */
    public void upateStartEndDays(){
        this._startDay = 1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getStartTime());
        this._endDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    @Override
    public void loadControlFile(String fileName){
        super.loadControlFile(fileName);
        this._startHours = new ArrayList<Integer>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getStartTime());
        this._startHours.add(cal.get(Calendar.HOUR_OF_DAY));
        this.upateStartEndDays();
    }
    
    /**
     * Get start hours number
     * @return Start hours number
     */
    public int getStartHoursNum(){
        return _startHours.size();
    }
    
    /**
     * Get day number
     * @return Day number
     */
    public int getDayNum(){
        return _endDay - _startDay + 1;
    }
    
    /**
     * Get start time number
     * @return Start time number
     */
    public int getTimeNum(){
        return getDayNum() * getStartHoursNum();
    }
    
    /**
     * 
     * @param dayIdx
     * @param hourIdx 
     */
    public void upateStartTime(int dayIdx, int hourIdx){
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getStartTime());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = _startDay + dayIdx;
        int hour = _startHours.get(hourIdx);
        cal.set(year, month, day, hour, 0);
        this.setStartTime(cal.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHH");
        this.setTrajFileName(format.format(cal.getTime()));
    }
    
    /**
     * Initialize start time
     * @param year The year - 4 digits
     * @param month The month
     */
    public void initStartTime(int year, int month){
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, this._startDay, this._startHours.get(0), 0, 0);
        this.setStartTime(cal.getTime());
    }
    
    // </editor-fold>
}
