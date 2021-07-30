/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brunomnsilva.smartgraph.example;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;

/**
 *
 * @author brunomnsilva
 */
public class City {
    private String name;
    private int population;

    public City(String name, int age) {
        this.name = name;
        this.population = age;
    }

    @SmartLabelSource
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    @Override
    public String toString() {
        return "City{" + "name=" + name + ", population=" + population + '}';
    }
   
    
}
