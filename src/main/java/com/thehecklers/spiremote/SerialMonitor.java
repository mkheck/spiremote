package com.thehecklers.spiremote;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thehecklers.spiremote.LogMonkey.logIt;

@Service
public class SerialMonitor {
    private static final short NODE0 = 0;   // Default 0==Mark's utility shed concentrator

    private boolean isConnected;
    private String readBuffer = "";
    private String curCmd = "";
    private final Serial serial = new Serial();

    private int nodeId;
    private int pubFreq = 5; // Default to publishing every fifth reading (overridable)
    private int readingCount = 1;

    Properties applicationProps = new Properties();

    private SerialThread thread;
    
    private ReadingSender sender;

    public SerialMonitor(ReadingSender sender) {
        this.sender = sender;
        
        // Load application properties
        loadProperties();

        // Log detected ports
        Serial.listPorts();

        String nodeProp = getProperty("nodeId");
        if (nodeProp.isEmpty()) {
            logIt("Default nodeId=0");
            nodeId = 0; // It's a default for a reason. :)
        } else {
            logIt("Loaded nodeId from properties: '" + nodeId + "'");
            nodeId = Integer.parseInt(nodeProp);

            String portName = getProperty("serialPort");
            if (portName.isEmpty()) {
                // Get out of here!
                logIt("ERROR: Property 'serialPort' missing from PiRemote.properties file.");
                Exception e = new Exception("ERROR: Property 'serialPort' missing from PiRemote.properties file.");
                try {
                    throw e;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                try {
                    logIt("Connecting to serial port " + portName);
                    //executor.submit(new PiRemote.SerialThread(portName));
                    thread = new SerialThread(portName);
                    //isConnected = true; This is set in the serial.connect() method for realz...
                } catch (Exception e) {
                    logIt("Exception: Connection to serial port " + portName + " failed: "
                            + e.getMessage());
                    isConnected = false;
                }
            }
        }

        String freqProp = getProperty("pubFreq");
        if (!freqProp.isEmpty()) {
            pubFreq = Integer.parseInt(freqProp);
        }

        //return isConnected;
    }

    @PreDestroy
    public boolean disconnect() {
        logIt("Closing serial port");

        if (isConnected) {
            logIt("Closing serial port");
            isConnected = serial.disconnect();
            //executor.shutdownNow();
        }

        return isConnected;
    }

    /*
        Configuration file methods - begin
    */
    private void loadProperties() {
        FileInputStream in = null;
        File propFile = new File("PiRemote.properties");

        if (!propFile.exists()) {
            // If it doesn't exist, create it.
            try {
                propFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            in = new FileInputStream(propFile);
            applicationProps.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getProperty(String propKey) {
        if (applicationProps.containsKey(propKey)) {
            return applicationProps.getProperty(propKey, "");
        } else {
            logIt("ERROR: Property not found: '" + propKey + "'.");
        }
        return "";
    }
    /*
        Configuration file methods - end
    */

    private Reading createNewReading(String reading) {
        Reading newReading = new Reading();

        newReading.setNode((short)nodeId);

        // Remove braces from reading "set"
        reading = reading.substring(1, reading.length() - 2);

        String[] values = reading.split("\\,");
        for (int x = 0; x < values.length; x++) {
            try {
                if(nodeId == NODE0) {
                    switch (x) {
                        case Reading.HUMIDITY:
                            newReading.setHum(Double.parseDouble(values[x]) / 100);
                            break;
                        case Reading.TEMPERATURE:
                            newReading.setTemp(Double.parseDouble(values[x]) / 100);
                            break;
                        case Reading.VOLTAGE:
                            newReading.setVolts(Double.parseDouble(values[x]) / 1000);
                            break;
                        case Reading.CURRENT:
                            newReading.setCurrent(Double.parseDouble(values[x]) / 1000);
                            break;
                        case Reading.STATUS:
                            newReading.setStatus(Integer.parseInt(values[x]));
                            break;
                    }
                    //} else { //if (nodeId == NODE1){
                    //
                }
            } catch (NumberFormatException nfe) {
                logIt("Error parsing " + reading);
            }
        }

        return newReading;
    }

    public void addToCommand(String cmd) {
        curCmd += cmd;
    }

    private class SerialThread implements Runnable, SerialPortEventListener {

        public SerialThread(String portName) {
            logIt("Creating SerialThread for port " + portName);
            try {
                isConnected = serial.connect(portName, this);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) { // Data is available
                try {
                    // Read all available data from serial port and add to buffer
                    readBuffer += serial.getSerialPort().readString(event.getEventValue());
                    if (readBuffer.contains("\n")) {
                        // Remove NewLine character
                        readBuffer = readBuffer.substring(0, readBuffer.length()-1);
                        if (readingCount % pubFreq != 0) {
                            logIt(readBuffer);  // Write entry to file w/o annotation
                            readingCount++;
                        } else {
                            // Only publish if we want to publish (by count)
                            //setChanged();  MAH (Spring rewrite): This is for Observable

                            //notifyObservers(readBuffer);
                            Reading reading = createNewReading(readBuffer);
                            sender.sendReading(reading);

                            // MAH (Spring rewrite): Have to write to Rabbit MQ queue here!

//                            if (reading.getHum() > -1d) { // Valid reading (MAH: revisit, refactor)
//                                notifyObservers(reading);
//                            }

                            logIt("--> " + readBuffer);  // Write published entry to file (w/annotation)
                            readingCount = 1;   // Reset counter
                        }
                        readBuffer = "";
                    }
                } catch (SerialPortException ex) {
                    logIt("Exception reading serial port: " + ex.getLocalizedMessage());
                }
            } else if (event.isCTS()) {     // CTS line has changed state
                if (event.getEventValue() == 1) { // Line is ON
                    logIt("CTS ON");
                } else {
                    logIt("CTS OFF");
                }
            } else if (event.isDSR()) {     // DSR line has changed state
                if (event.getEventValue() == 1) { // Line is ON
                    logIt("DSR ON");
                } else {
                    logIt("DSR OFF");
                }
            }
        }

        @Override
        public void run() {
            while (isConnected) {
                if (!curCmd.isEmpty()) {
                    logIt("curCmd=='" + curCmd + "'");

                    try {
                        if (serial.getSerialPort().writeString(curCmd)) {
                            curCmd = "";
                        }   // If it didn't write, don't clear buffer (else condition)
                    } catch (Exception ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                        logIt("Exception writing to serial port: " + ex.getLocalizedMessage());
                    }
                }
            }
        }
    }
}
