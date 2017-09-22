/*
 * SquareDriver.java
 */
package ca.mcgill.ecse211.lab2;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class SquareDriver {
  private static final int FORWARD_SPEED = 250;
  private static final int ROTATE_SPEED = 180;

  public static void drive(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      double leftRadius, double rightRadius, double width) {
    // reset the motors
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
      motor.stop();
      motor.setAcceleration(3000);
    }
    
    double straightLineDistX = OdometryLab.TILE_SIZE * OdometryLab.NUM_TILE_X;
    double straightLineDistY = OdometryLab.TILE_SIZE * OdometryLab.NUM_TILE_Y;
    
    // wait 5 seconds
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // there is nothing to be done here because it is not expected that
      // the odometer will be interrupted by another thread
    }

    for (int i = 0; i < 4; i++) {
      // drive forward two tiles
      leftMotor.setSpeed(FORWARD_SPEED);
      rightMotor.setSpeed(FORWARD_SPEED);
      
      if (i %2 == 0) {
    	  leftMotor.rotate(convertDistance(leftRadius, straightLineDistX), true);
    	  rightMotor.rotate(convertDistance(rightRadius, straightLineDistX), false);
      } else {
    	  leftMotor.rotate(convertDistance(leftRadius, straightLineDistY), true);
    	  rightMotor.rotate(convertDistance(rightRadius, straightLineDistY), false);  
      }

      // turn 90 degrees clockwise
      leftMotor.setSpeed(ROTATE_SPEED);
      rightMotor.setSpeed(ROTATE_SPEED);

      rightMotor.rotate(convertAngle(leftRadius, width, 90), true);
      leftMotor.rotate(-convertAngle(rightRadius, width, 90), false);
    }
  }

  private static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  private static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }
}
