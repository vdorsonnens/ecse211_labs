package ca.mcgill.ecse211.lab1;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {
  private static final int SPEED = 125;
  private static final int SPEEDDELTA = 70;
  private static final int WMA_N = 20;
  private static final int BANDCENTER = 30;
  private static final int BANDWIDTH = 3;
  private static final int FILTER_OUT = 10;

  private int distance;
  private int filterControl;

  public BangBangController() {
    // Default Constructor
    WallFollowingLab.leftMotor.setSpeed(SPEED); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(SPEED);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
    int correctedDistance = (int)(Math.sqrt(2.0) * (double)distance);
    
    // Filtering
    if (correctedDistance >= 70 && filterControl < FILTER_OUT) {
      filterControl++;
    } else if (correctedDistance >= 70) {
      this.distance = 70;
    } else {
      filterControl = 0;
      this.distance = correctedDistance;
    }

    int error = BANDCENTER - this.distance;   
    int absError = error > 0 ? error: -1*error;
    
    // just right
    if (absError < BANDWIDTH)
      setMotorsSpeed(SPEED +  SPEEDDELTA, SPEED + SPEEDDELTA);
    // too close
    else if (error > 0) {
    	WallFollowingLab.leftMotor.setSpeed(SPEED + 2* SPEEDDELTA);
    	WallFollowingLab.rightMotor.setSpeed(50);
    	WallFollowingLab.leftMotor.forward();
    	WallFollowingLab.rightMotor.backward();
    }
    // too far
    else 
      setMotorsSpeed(SPEED, SPEED + 90);
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
