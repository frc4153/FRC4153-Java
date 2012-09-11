/*
 * State Model For Shooter.
 * Eight Possible States based on Three Sensors
 * 
 *  #     Bottom  Middle  Top     Transition                Possible States
 *  (0)      0       0       0       Run Roller                 4
 *  (1)      0       0       1       Run Roller & Shoot         0,5
 *  (2)      0       1       0       Run Roller & Belt & Feed   1,6
 *  (3)      0       1       1       Run Roller & Shoot         7,2
 *  (4)      1       0       0       Run Roller & Belt          2,6
 *  (5)      1       0       1       Run Belt & Shoot           3,7
 *  (6)      1       1       0       Run Belt & Feed            3,7
 *  (7)      1       1       1       Shoot                      6
 *   
 */
package com.team4153.oppie2012;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 *
 * @author Colin/Jim
 */
public class StateModel {

    public static int debugLevel = 0;
    public static final int SHOOT_MOTOR_CAN_ADDRESS = 7;
    public static final int METERING_DRIVE_RELAY_ADDRESS = 1;
    public static final int BELD_DRIVE_CAN_ADDRESS = 3;
    public static final int ROLLER_DRIVE_RELAY_ADDRESS = 2;
    public static final int TOP_PHOTOEYE_CHANNEL = 8;
    public static final int MIDDLE_SWITCH_CHANNEL = 9;
    public static final int BOTTOM_PHOTOEYE_CHANNEL = 7;   //8;

    static class FutureStates {

        int[] futureState;
        /**
         * True to run the roller to cause a transition out of this state.
         */
        boolean roller;
        /**
         * True to run the belt to cause a transition out of this state.
         */
        boolean belt;
        /**
         * True to run the feed to cause a transition out of this state.
         */
        boolean metering;

        /**
         * Constructor.
         *
         * @param one one of the possible transitions, express as an index into
         * the FutureStates array
         * @param two one of the possible transitions, express as an index into
         * the FutureStates array
         * @param three one of the possible transitions, express as an index
         * into the FutureStates array
         */
        public FutureStates(int one, int two, int three, boolean roller, boolean belt, boolean metering) {
            futureState = new int[3];
            futureState[0] = one;
            futureState[1] = two;
            futureState[2] = three;
            // transitions.
            this.roller = roller;
            this.belt = belt;
            this.metering = metering;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (int counter = 0; counter < 3; counter++) {
                sb.append("State ");
                sb.append(counter);
                sb.append(" ");
                sb.append(futureState[counter]);
            }
            sb.append(" action r ");
            sb.append(roller);
            sb.append(" b ");
            sb.append(belt);
            sb.append(" m ");
            sb.append(metering);
            return (sb.toString());
        }
    }
    /**
     * The motor that shoots the balls.
     */
    protected CANJaguar shootDrive;
    /**
     * The motor at the top of the head that feeds balls into the shooting
     * position. The top photoeye is here and can detect balls that are in the
     * waiting to shoot position.
     */
    protected Relay meteringDrive;
    /**
     * The drive for the belt.
     */
    protected CANJaguar beltDrive;
    /**
     * The drive for the roller.
     */
    protected Relay rollerDrive;
    /**
     * The bottom switch input.
     */
    protected DigitalInput bottomPhotoEye;
    /**
     * The middle switch input.
     */
    protected DigitalInput middleSwitch;
    /**
     * The top switch input.
     */
    protected DigitalInput topPhotoEye;
    /**
     * The state model singleton.
     */
    protected static StateModel stateModel;
    /**
     * The last state that we saw. The next state must be a valid transition
     * from this state.
     */
    protected int lastState;
    /**
     * Description of all future states.
     */
    FutureStates[] futureStates;

    /**
     * State model is a singleton.
     */
    public static StateModel getStateModel() {
        if (stateModel == null) {
            stateModel = new StateModel();
        }
        return stateModel;
    }

    /**
     * Constructor.
     */
    protected StateModel() {
        try {
            meteringDrive = new Relay(METERING_DRIVE_RELAY_ADDRESS);
            rollerDrive = new Relay(ROLLER_DRIVE_RELAY_ADDRESS);
            //shootDrive = new CANJaguar(SHOOT_MOTOR_CAN_ADDRESS,CANJaguar.ControlMode.kCurrent);
            //beltDrive = new CANJaguar(BELD_DRIVE_CAN_ADDRESS,CANJaguar.ControlMode.kCurrent);
            shootDrive = new CANJaguar(SHOOT_MOTOR_CAN_ADDRESS);
            beltDrive = new CANJaguar(BELD_DRIVE_CAN_ADDRESS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        topPhotoEye = new DigitalInput(TOP_PHOTOEYE_CHANNEL);
        middleSwitch = new DigitalInput(MIDDLE_SWITCH_CHANNEL);
        bottomPhotoEye = new DigitalInput(BOTTOM_PHOTOEYE_CHANNEL);

        lastState = 0;
        // since the states are not maintained during transitions, we'll keep 
        // track of the next possible state and end the transition when that 
        // state is reached.
        // I'm not totally convinced of this since it depends on catching the
        // switches.  If we miss a switch we're hosed (obviously).
        futureStates = new FutureStates[8];
        futureStates[0] = new StateModel.FutureStates(4, -1, -1, true, false, false);
        futureStates[1] = new StateModel.FutureStates(0, 5, -1, true, false, false);
        futureStates[2] = new StateModel.FutureStates(1, 6, -1, true, true, true);
        futureStates[3] = new StateModel.FutureStates(7, 2, -1, true, false, false);
        futureStates[4] = new StateModel.FutureStates(2, 6, -1, true, true, false);
        futureStates[5] = new StateModel.FutureStates(3, 7, -1, true, true, false);
        futureStates[6] = new StateModel.FutureStates(3, 7, -1, true, true, true);
        futureStates[7] = new StateModel.FutureStates(6, -1, -1, false, false, false);
    }

    /**
     * Called periodically to check the current status of the state model.
     */
    public int task() {
        int bottom = bottomPhotoEye.get() ? 1 : 0;
        int middle = middleSwitch.get() ? 0 : 1;
        int top = topPhotoEye.get() ? 1 : 0;
        int currentState = (bottom << 2) | (middle << 1) | top;
        FutureStates possibleState = futureStates[lastState];
        if (debugLevel > 10) {
            System.err.println("StateModel:Task last " + lastState + " current " + currentState + " future " + possibleState);
            System.err.println("StateModel:Task sensors " + bottom + " middle " + middle + " top " + top);
        }

        boolean validTransition = false;
        for (int counter = 0; counter < 3; counter++) {
            if (possibleState.futureState[counter] == currentState) {
                if (debugLevel > 10) {
                    System.err.println("StateModel:Task valid transition from " + lastState + " to " + currentState);
                }
                validTransition = true;
                break;
            }
        }
        if (validTransition && lastState != currentState) {
            // stop the current transition, start the new transition
            // we were in lastState and we are now in currentstate.
            if (debugLevel > 10) {
                System.err.println("StateModel:Task transition " + lastState + " new " + currentState);
            }
            lastState = currentState; // assume successful transition even on an exception - there's no better choice
        }
        try {
            FutureStates current = futureStates[lastState];
            if (debugLevel > 10) {
                System.err.println("StateModel:Task actions " + lastState + " roller " + current.roller + " belt " + current.belt + " meter " + current.metering);
            }
            runRoller(current.roller);
            runBelt(current.belt);
            runMetering(current.metering && top != 1);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public void runRoller(boolean roller) throws CANTimeoutException {
        if (roller && Oppie.robotInstance.manipulatorStick.getRawButton(2)) {
            rollerDrive.set(Relay.Value.kReverse);
        } else {
            rollerDrive.set(Relay.Value.kOff);
        }
    }

    public void runBelt(boolean belt) throws CANTimeoutException {
        if (beltDrive != null) {
            if (belt) {
                beltDrive.setX(-0.5);
            } else {
                beltDrive.setX(0);
            }
        }
    }

    public void clear(boolean belt) throws CANTimeoutException {
        if (beltDrive != null) {
            if (belt) {
                beltDrive.setX(0.55);
            } else {
                beltDrive.setX(0);
            }
        }
    }

    public void runMetering(boolean metering) throws CANTimeoutException {
        if (metering) {
            meteringDrive.set(Relay.Value.kReverse);
        } else {
            meteringDrive.set(Relay.Value.kOff);
        }
    }

    public void shoot(boolean shoot) throws CANTimeoutException {
        if (shootDrive != null) {
            if (shoot) {
                meteringDrive.set(Relay.Value.kReverse);
                shootDrive.setX(-0.25);
            } else {
                shootDrive.setX(0);
            }
        }
    }

    /**
     * Resets the model.
     */
    public void reset() {
        try {
            lastState = 0;
            runRoller(false);
            runBelt(false);
            runMetering(false);
            meteringDrive.set(Relay.Value.kOff);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
}
