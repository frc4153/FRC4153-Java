/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.team4153.oppie2012;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Victor;

/**
 *
 * Bridge Management System.
 */
public class BMS {

    /**
     * Top limit switch
     */
    public static final int TOP_LIMIT = 11;
    /**
     * Bottom limit switch.
     */
    public static final int BOTTOM_LIMIT = 10;
    /**
     * Victor PWM.
     */
    public static final int BMS_VICTOR = 1;
    /**
     * The top limit switch
     */
    protected DigitalInput topLimitSwtich;
    /**
     * The bottom limit switch
     */
    protected DigitalInput bottomLimitSwitch;
    /**
     * The bms Victor.
     */
    protected Victor bmsController;
    /**
     * The bridge management system singleton.
     */
    protected static BMS bms;

    /**
     * State model is a singleton.
     */
    public static BMS getBMS() {
        if (bms == null) {
            bms = new BMS();
        }
        return bms;
    }

    /**
     * Constructor.
     */
    protected BMS() {
        try {
            topLimitSwtich = new DigitalInput(TOP_LIMIT);
            bottomLimitSwitch = new DigitalInput(BOTTOM_LIMIT);
            bmsController = new Victor(BMS_VICTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called periodically to check the current status of the bms.
     */
    public void task() {
        //System.err.println ("BMS Task button 5 " + Oppie.robotInstance.manipulatorStick.getRawButton(5) + topLimitSwtich.get());
        //System.err.println ("BMS Task button 4 " + Oppie.robotInstance.manipulatorStick.getRawButton(4) + bottomLimitSwitch.get());
        if (Oppie.robotInstance.manipulatorStick.getRawButton(5) && topLimitSwtich.get()) {
            System.err.println("BMS Task button 5 " + Oppie.robotInstance.manipulatorStick.getRawButton(5) + topLimitSwtich.get());
            bmsController.set(0.50);
        } else {
            if (!Oppie.robotInstance.manipulatorStick.getRawButton(4)) {
                bmsController.set(0);
            }
        }
        if (Oppie.robotInstance.manipulatorStick.getRawButton(4) && bottomLimitSwitch.get()) {
            bmsController.set(-0.50);
        } else {
            if (!Oppie.robotInstance.manipulatorStick.getRawButton(5)) {
                bmsController.set(0);
            }
        }
    }
}
