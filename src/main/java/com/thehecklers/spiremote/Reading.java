package com.thehecklers.spiremote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Reading {
    public static final int HUMIDITY = 0,
            TEMPERATURE = 1,
            VOLTAGE = 2,
            CURRENT = 3,
            STATUS = 4;

    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private Integer id;
    private short node;
    private double hum, temp, volts, current;
    private int status;

    public Reading() {
        //this.id = -1;
        this.node = -1;
        this.hum = -1d;
        this.temp = -1d;
        this.volts = -1d;
        this.current = -1d;
        this.status = 0;
    }

    public Reading(Integer id, short node, double hum, double temp,
                   double volts, double current, int status) {
        this.id = id;
        this.node = node;
        this.hum = hum;
        this.temp = temp;
        this.volts = volts;
        this.current = current;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public short getNode() {
        return node;
    }

    public void setNode(short node) {
        this.node = node;
    }

    public double getHum() {
        return hum;
    }

    public void setHum(double hum) {
        this.hum = hum;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getVolts() {
        return volts;
    }

    public void setVolts(double volts) {
        this.volts = volts;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String toJson() {
        String json = "";

        try {
            json = objectWriter.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(Reading.class.getName()).log(Level.SEVERE, null, ex);
        }

        return json;
    }

    @Override
    public String toString() {
        return "Id=" + id + ", node=" + node +
                ", hum=" + hum + ", temp=" + temp +
                ", volts=" + volts + ", current=" + current +
                ", status" + status + ".";
    }
}
