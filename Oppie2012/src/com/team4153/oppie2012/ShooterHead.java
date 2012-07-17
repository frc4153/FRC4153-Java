/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.team4153.oppie2012;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 *
 * Control of the shooter head. low limit 1.19 high limit 4.72
 */
public class ShooterHead {

    /**
     * The top limit - 4.72V.
     */
    public static final double TOP_LIMT = 4.72;
    /**
     * The lower limit - 1.19V.
     */
    public static final double LOWER_LIMT = 1.19;
    /**
     * Top limit switch
     */
    public static final int ANALOG_INPUT = 1;
    /**
     * JAG for the shooter.
     */
    public static final int SHOOTER_JAG = 5;
    /**
     *
     * /**
     * The motor that moves the shooter head.
     */
    protected CANJaguar headDrive;
    /**
     * The analog input that gives the position.
     */
    protected AnalogChannel positionInput;
    /**
     * The bridge management system singleton.
     */
    protected static ShooterHead shooter;

    /**
     * State model is a singleton.
     */
    public static ShooterHead getShooterHead() {
        if (shooter == null) {
            shooter = new ShooterHead();
        }
        return shooter;
    }

    /**
     * Constructor.
     */
    protected ShooterHead() {
        try {
            positionInput = new AnalogChannel(ANALOG_INPUT);
            headDrive = new CANJaguar(SHOOTER_JAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called periodically to check the current status of the bms.
     */
    public void task() {
        boolean buttonPressed = Oppie.robotInstance.manipulatorStick.getRawButton(6) || Oppie.robotInstance.manipulatorStick.getRawButton(11);
        System.err.println("Shooter head throttle " + " " + buttonPressed + " x " + Oppie.robotInstance.manipulatorStick.getX() + " y " + Oppie.robotInstance.manipulatorStick.getY() + " " + positionInput.getAverageVoltage());
        if (buttonPressed) {
            try {
                double amount = Oppie.robotInstance.manipulatorStick.getY();
                if (positionInput.getAverageVoltage() < TOP_LIMT && amount < 0) {
                    System.err.println("Shooter head throttle " + Oppie.robotInstance.manipulatorStick.getThrottle() + " " + positionInput.getAverageVoltage());
                    headDrive.setX(amount);
                } else {
                    if (amount < 0) {
                        headDrive.setX(0);
                    }

                }
                if ( positionInput.getAverageVoltage() > LOWER_LIMT && amount > 0) {
                    System.err.println("Shooter head throttle " + Oppie.robotInstance.manipulatorStick.getThrottle() + " " + positionInput.getAverageVoltage());
                    headDrive.setX(amount);
                } else {
                    if (amount > 0) {
                        headDrive.setX(0);
                    }

                }
            } catch (CANTimeoutException ex) {
                ex.printStackTrace();
            }
        }
    }
}