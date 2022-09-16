/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.intrigus.smartgraph.example;

import org.intrigus.smartgraph.graphview.SmartLabelSource;

/**
 *
 * @author brunomnsilva
 */
public class Distance {
    private int distance;

    public Distance(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
    
    @SmartLabelSource
    public String getDisplayDistance() {
        /* If the above annotation is not present, the toString()
        will be used as the edge label. */
        
        return distance + " km";
    }

    @Override
    public String toString() {
        return "Distance{" + "distance=" + distance + '}';
    }

    

    
    
}
