package com.thehecklers.spiremote;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thehecklers.spiremote.LogMonkey.logIt;
import static java.lang.Thread.sleep;

public class Serial {
    private SerialPort serialPort;

    private boolean isConnected = false;

    public boolean connect(String portName, SerialPortEventListener listener) throws Exception {
        isConnected = connect(portName);
        serialPort.addEventListener(listener);

        return isConnected;
    }

    public boolean connect(String portName) throws Exception {
        serialPort = new SerialPort(portName);
        try{
            if (serialPort.openPort()) {
                logIt("Port '" + portName + "' open.");
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                /*
                A sleep(2000) call is the minimum required to allow for Arduino
                to retrieve values from the serial port. No touchy!  ;)
                */
                sleep(2500);
                isConnected = true;
            }
        } catch (SerialPortException | InterruptedException e) {
            System.out.println("Exception trying to open port: " + e.getLocalizedMessage());
        }

        return isConnected;
    }

    public void addEventListener(SerialPortEventListener listener) throws Exception {
        serialPort.addEventListener(listener);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean disconnect(){
        if (serialPort != null) {
            try {
                try {
                    serialPort.removeEventListener();
                } catch (Exception ex) {
                    // Absorb it. Replace visibility/accessibility to underlying
                    // serialPort (directly) and handle properly. MAH
                }
                serialPort.closePort();
            } catch (SerialPortException ex) {
                Logger.getLogger(Serial.class.getName()).log(Level.SEVERE, null, ex);
            }
            logIt("Disconnecting: serial port closed.");
        }

        return !isConnected;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public static void listPorts() {
        logIt("Ports detected:");
        String[] portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            logIt(portName);
        }
    }
}
