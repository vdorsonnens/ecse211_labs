package ca.mcgill.ecse211.lab1;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 200;
  private static final int FILTER_OUT = 20;
  private static final double PROP_CONSTANT = 1.0;
  private static final int MAX_CORRECTION = 100;

  private final int bandCenter;
  private final int bandWidth;
  private int distance;
  private int filterControl;

  public PController(int bandCenter, int bandwidth) {
    this.bandCenter = bandCenter;
    this.bandWidth = bandwidth;
    this.filterControl = 0;

    WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // Initalize motor rolling forward
    WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {

    // rudimentary filter - toss out invalid samples corresponding to null
    // signal.
    // (n.b. this was not included in the Bang-bang controller, but easily
    // could have).
    //
    if (distance >= 255 && filterControl < FILTER_OUT) {
      // bad value, do not set the distance var, however do increment the
      // filter value
      filterControl++;
    } else if (distance >= 255) {
      // We have repeated large values, so there must actually be nothing
      // there: leave the distance alone
      this.distance = distance;
    } else {
      // distance went below 255: reset filter and leave
      // distance alone.
      filterControl = 0;
      this.distance = distance;
    }

    // TODO: process a movement based on the us distance passed in (P style)
    int error = this.bandCenter - distance;
    int absError = error > 0 ? error: -1*error;
    int leftSpeed, rightSpeed;
    // just right
    if (absError < this.bandWidth) {
    	leftSpeed = MOTOR_SPEED;
    	rightSpeed = MOTOR_SPEED;
    } else {
    	int correction = calculateCorrection(absError);
    	// too close
        if (error > 0) {
        	leftSpeed = MOTOR_SPEED + correction;
        	rightSpeed = MOTOR_SPEED - correction;
        }
        // too far
        else {
        	leftSpeed = MOTOR_SPEED - correction;
        	rightSpeed = MOTOR_SPEED + correction;
        }
    }
    
    setMotorsSpeed(leftSpeed, rightSpeed);
  }
  
  private int calculateCorrection(int absError) {
	  // y = ax, maybe try y = ae^x
	  int correction = (int) (PROP_CONSTANT * (double) absError );
	  correction = correction < MAX_CORRECTION ? correction: MAX_CORRECTION;
	  return correction;
  }
  
  private void setMotorsSpeed(int leftSpeed, int rightSpeed) {
	  WallFollowingLab.leftMotor.setSpeed(leftSpeed);
	  WallFollowingLab.rightMotor.setSpeed(rightSpeed);
	  WallFollowingLab.leftMotor.forward();
	  WallFollowingLab.rightMotor.forward();
  }
  
  @Override
  public int readUSDistance() {
    return this.distance;
  }

}