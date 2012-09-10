/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package com.team4153.oppie2012;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Oppie extends SimpleRobot {

    public static final int LEFT_FRONT_MOTOR = 2;
    public static final int LEFT_REAR_MOTOR = 6;
    public static final int RIGHT_FRONT_MOTOR = 4;
    public static final int RIGHT_REAR_MOTOR = 8;
    public static final int LEFT_JOYSTICK = 1;
    public static final int RIGHT_JOYSTICK = 2;
    public static final int MANIPULATOR_JOYSTICK = 3;
    Joystick leftStick = new Joystick(LEFT_JOYSTICK);
    Joystick rightStick = new Joystick(RIGHT_JOYSTICK);
    Joystick manipulatorStick = new Joystick(MANIPULATOR_JOYSTICK);
    RobotDrive drive;
    
    public static Oppie robotInstance;

    /**
     * Create the state model for the shooter.
     */
    protected void robotInit() {
        super.robotInit();
        // oops, wait, it's a singleton, don't need to do this.
        try {
            drive = new RobotDrive(new CANJaguar(LEFT_FRONT_MOTOR), new CANJaguar(LEFT_REAR_MOTOR), new CANJaguar(RIGHT_FRONT_MOTOR), new CANJaguar(RIGHT_REAR_MOTOR));
            drive.setSafetyEnabled(false);
       } catch (Exception any) {
            any.printStackTrace();
        }
        robotInstance = this;
     }

    /**
     * This function is called once each time the robot enters autonomous mode.
     */
    public void autonomous() {
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        System.err.println ("Entering Teleop");
                    StateModel.getStateModel().reset();
      while (isOperatorControl() && isEnabled()) {
            try {
                drive.tankDrive(rightStick, leftStick);  
                //System.err.println("Teleop");
                //StateModel.getStateModel().runRoller(manipulatorStick.getRawButton(3));
                //StateModel.getStateModel().runBelt(manipulatorStick.getRawButton(4));
                //StateModel.getStateModel().runMetering(manipulatorStick.getRawButton(5));
                StateModel.getStateModel().task();
                StateModel.getStateModel().shoot(manipulatorStick.getRawButton(1));
                //StateModel.getStateModel().runRoller(manipulatorStick.getRawButton(2));
                if ( manipulatorStick.getRawButton(8) ) {
                  StateModel.getStateModel().reset();
                }
                //Gyro g = new Gyro();
                //System.err.println("Teleop - running BMS");
                BMS.getBMS().task();
                ShooterHead.getShooterHead().task();
                Timer.delay(0.005);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
