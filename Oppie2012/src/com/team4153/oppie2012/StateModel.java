/*
 * State Model For Shooter.
 * Eight Possible States based on Three Sensors
 * 
 *  #     Bottom  Middle  Top     Transition                Possible States
 *  (0)      0       0       0       Run Roller                 1,7
 *  (1)      0       0       1       Run Roller & Shoot         0,4,1
 *  (2)      0       1       0       Run Roller & Belt & Feed   7,4,3
 *  (3)      0       1       1       Run Roller & Shoot         6,2,4
 *  (4)      1       0       0       Run Roller & Belt          2,7
 *  (5)      1       0       1       Run Belt & Shoot           5,6,7
 *  (6)      1       1       0       Run Belt & Feed            6,5
 *  (7)      1       1       1       Shoot                      7,5,6
 *   
 */
package com.team4153.oppie2012;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 *
 * @author Colin/Jim
 */
public class StateModel {

    public static final int SHOOT_MOTOR_CAN_ADDRESS = -1;
    public static final int FEED_DRIVE_CAN_ADDRESS = -1;
    public static final int BELD_DRIVE_CAN_ADDRESS = -1;
    public static final int ROLLER_DRIVE_CAN_ADDRESS = -1;
    public static final int TOP_PHOTOEYE_CHANNEL = -1;
    public static final int MIDDLE_SWITCH_CHANNEL = -1;
    public static final int BOTTOM_PHOTOEYE_CHANNEL = -1;

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
        boolean feed;

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
        public FutureStates(int one, int two, int three, boolean roller, boolean belt, boolean feed) {
            futureState = new int[3];
            futureState[0] = one;
            futureState[1] = two;
            futureState[2] = three;
            // transitions.
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
    protected CANJaguar feedDrive;
    /**
     * The drive for the belt.
     */
    protected CANJaguar beltDrive;
    /**
     * The drive for the roller.
     */
    protected CANJaguar rollerDrive;
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
            shootMotor = new CANJaguar(SHOOT_MOTOR_CAN_ADDRESS);
            feedDrive = new CANJaguar(FEED_DRIVE_CAN_ADDRESS);
            beltDrive = new CANJaguar(BELD_DRIVE_CAN_ADDRESS);
            rollerDrive = new CANJaguar(ROLLER_DRIVE_CAN_ADDRESS);
        } catch (CANTimeoutException ex) {
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
        futureStates[0] = new StateModel.FutureStates(1, 7, -1, true, false, false);
        futureStates[1] = new StateModel.FutureStates(0, 4, 1, true, false, false);
        futureStates[2] = new StateModel.FutureStates(7, 4, 3, true, true, true);
        futureStates[3] = new StateModel.FutureStates(6, 2, 4, true, false, false);
        futureStates[4] = new StateModel.FutureStates(2, 7, -1, true, true, false);
        futureStates[5] = new StateModel.FutureStates(5, 6, 7, false, true, false);
        futureStates[6] = new StateModel.FutureStates(6, 5, -1, false, true, true);
        futureStates[7] = new StateModel.FutureStates(7, 5, 6, false, false, false);
    }

    /**
     * Called periodically to check the current status of the state model.
     */
    public int task() {
        int bottom = bottomPhotoEye.get() ? 1 : 0;
        int middle = middleSwitch.get() ? 1 : 0;
        int top = topPhotoEye.get() ? 1 : 0;
        int currentState = (bottom << 2) | (middle << 1) | top;
        FutureStates possibleState = futureStates[lastState];
        boolean validTransition = false;
        for (int counter = 0; counter < 3; counter++) {
            if (possibleState.futureState[counter] == currentState) {
                validTransition = true;
                break;
            }
        }
        if (validTransition) {
            try {
                // stop the current transition, start the new transition
                // we were in lastState and we are now in currentstate.
                lastState = currentState; // assume successful transition even on an exception - there's no better choice
                FutureStates current = futureStates[currentState];
                runRoller(current.roller);
                runBelt(current.roller);
                runFeed(current.roller);
            } catch (CANTimeoutException ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public void runRoller(boolean roller) throws CANTimeoutException {
        if (roller) {
            rollerDrive.setX(0.1);
        } else {
            rollerDrive.setX(0);
        }
    }

    public void runBelt(boolean belt) throws CANTimeoutException {
        if (belt) {
            beltDrive.setX(0.1);
        } else {
            beltDrive.setX(0);
        }
    }

    public void runFeed(boolean feed) throws CANTimeoutException {
        if (feed) {
            feedDrive.setX(0.1);
        } else {
            feedDrive.setX(0);
        }
    }

    public void shoot(boolean shoot) throws CANTimeoutException {
        if (shoot) {
            shootDrive.setX(0.1);
        } else {
            shootDrive.setX(0);
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
            runFeed(false);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
}
