package ca.mcgill.ecse211.lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class WheelsController {
	
	private static final int FORWARD_SPEED = 150;
	private static final int TURN_SPEED = 100;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	
	private double leftRadius;
	private double rightRadius;
	private double trackLength;
	
	public WheelsController(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right, double leftRadius, double rightRadius, double trackLength) {
		this.leftMotor = left;
		this.rightMotor = right;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.trackLength = trackLength;
	}
	
	public void forward(double distance) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(leftRadius, distance), true);
  	  	rightMotor.rotate(convertDistance(rightRadius, distance), false);
	}
	
	public void turn(double angle) {
		// Assuming angle is in degrees, to change if not
		//angle = radianToDegree(angle);
		leftMotor.setSpeed(TURN_SPEED);
		rightMotor.setSpeed(TURN_SPEED);
		rightMotor.rotate(convertAngle(leftRadius, trackLength, angle), true);
	    leftMotor.rotate(-convertAngle(rightRadius, trackLength, angle), false);
	}
	
	public void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}
	
	public void setSpeed(int speed) {
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
	}
	
	private static int convertDistance(double radius, double distance) {
	    return (int) ((180.0 * distance) / (Math.PI * radius));
	  }
	
	private static int convertAngle(double radius, double width, double angle) {
	    return convertDistance(radius, Math.PI * width * angle / 360.0);
	  }
	
	private static double radianToDegree(double rad) {
		return 180 * rad / Math.PI;
	}
	
}