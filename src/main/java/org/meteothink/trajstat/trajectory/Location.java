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

/**
 *
 * @author Yaqiang Wang
 */
public class Location {
    /**
     * Longitude
     */
    public float longitude;
    /**
     * Latitude
     */
    public float latitude;
    /**
     * Height above ground level - meters
     */
    public float height;
    
    /**
     * Constructor
     */
    public Location(){
        
    }
    
    /**
     * Constructor with location string
     * @param locStr Location string
     */
    public Location(String locStr){
        String[] strs = locStr.trim().split("\\s+");
        latitude = Float.parseFloat(strs[0]);
        longitude = Float.parseFloat(strs[1]);
        height = Float.parseFloat(strs[2]);
    }
    
    /**
     * Convert to string - for CONTROL file
     * @return 
     */
    @Override
    public String toString(){
        return String.format("%1$.2f", latitude) + " " + String.format("%1$.2f", longitude) + 
                " " + String.format("%1$.2f", height);
    }    
}
