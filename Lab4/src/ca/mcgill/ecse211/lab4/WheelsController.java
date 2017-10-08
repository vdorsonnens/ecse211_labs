package ca.mcgill.ecse211.lab4;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class WheelsController {
	
	private static final int FORWARD_SPEED = 75;
	private static final int TURN_SPEED = 100;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	
	private double leftRadius;
	private double rightRadius;
	private double trackLength;
	
	public WheelsController(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right, double radius, double trackLength) {
		this.leftMotor = left;
		this.rightMotor = right;
		this.leftRadius = radius;
		this.rightRadius = radius;
		this.trackLength = trackLength;
	}
	
	public void forward(double distance, boolean block) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(leftRadius, distance), true);
  	  	rightMotor.rotate(convertDistance(rightRadius, distance), !block);
	}
	
	public void forward() {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	public void turn(double angle, boolean block) {
		// angle is in degrees
		leftMotor.setSpeed(TURN_SPEED);
		rightMotor.setSpeed(TURN_SPEED);
		rightMotor.rotate(-convertAngle(leftRadius, trackLength, angle), true);
	    leftMotor.rotate(convertAngle(rightRadius, trackLength, angle), !block);
	}
	
	public void stop() {
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
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
	
}
