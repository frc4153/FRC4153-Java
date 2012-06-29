/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package com.team4153.oppie2012;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Oppie extends SimpleRobot {

    public static final int LEFT_FRONT_MOTOR = -1;
    public static final int LEFT_REAR_MOTOR = -1;
    public static final int RIGHT_FRONT_MOTOR = -1;
    public static final int RIGHT_REAR_MOTOR = -1;
    public static final int LEFT_JOYSTICK = 1;
    public static final int RIGHT_JOYSTICK = 2;
    public static final int MANIPULATOR_JOYSTICK = 2;
    RobotDrive drive = new RobotDrive(LEFT_FRONT_MOTOR, LEFT_REAR_MOTOR, RIGHT_FRONT_MOTOR, RIGHT_REAR_MOTOR);
    Joystick leftStick = new Joystick(LEFT_JOYSTICK);
    Joystick rightStick = new Joystick(RIGHT_JOYSTICK);
    Joystick manipulatorStick = new Joystick(MANIPULATOR_JOYSTICK);

    /**
     * Create the state model for the shooter.
     */
    protected void robotInit() {
        super.robotInit();
        // oops, wait, it's a singleton, don't need to do this.
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
        while (isOperatorControl() && isEnabled()) {
            try {
                //drive.tankDrive(leftStick, rightStick);  // no driving yet.
                StateModel.getStateModel().runRoller(manipulatorStick.getRawButton(3));
                StateModel.getStateModel().runBelt(manipulatorStick.getRawButton(4));
                StateModel.getStateModel().runFeed(manipulatorStick.getRawButton(5));
                StateModel.getStateModel().shoot(manipulatorStick.getRawButton(1));
                Timer.delay(0.005);
            } catch (CANTimeoutException ex) {
                ex.printStackTrace();
            }
        }

    }
}
