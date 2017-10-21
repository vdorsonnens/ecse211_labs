package lab5;

import lab5.main.Global;
import lejos.hardware.Button;

public class Navigation extends Thread {
	
	double angle  = 0;
	
	public Navigation() {

	}

	public void run() {
		try {
			FallingEdge();
			lightPosition();
			Button.waitForAnyPress();
			travelTo(Global.startingX, Global.startingY);
			
			travelZipLine();	
			
			zipLineCorrection();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void travelZipLine() throws Exception{
		if (Global.startingX==Global.zipLineX) {
			if (Global.startingY>Global.zipLineY) {
				turn(90, false);
			}else{
				turn(270, false);
			}
		}else {
			if (Global.startingX>Global.zipLineX) {
				turn(180, false);
			}
		}
		move(Global.ZIPLINE_LENGTH, false);
	}

	public void zipLineCorrection() throws Exception{
		
		Global.colorSensorSwitch = true;

		// move until sensor sees black line
		move(Global.KEEP_MOVING, true);
		while (!Global.BlackLineDetected) {

		}
		// move back to black line
		move(0 - Global.ROBOT_LENGTH, false);
		Thread.sleep(250);

		// reset angle
		// turn until color sensor sees a black line
		turn(Global.KEEP_MOVING, true);
		while (!Global.BlackLineDetected) {

		}
		
		//face 0 degree
		turn(90-Global.COLOR_SENSOR_OFFSET_ANGLE, false);

		
	}

	

	public void travelTo(int x, int y) throws Exception {
		// start requiring threads
		Global.colorSensorSwitch = true;
		Global.odometerSwitch = true;
		Thread.sleep(Global.THREAD_SLEEP_TIME);

		double angle = 0;

		// move across x
		if (x != Odometer.getX()) {// verify if moving in x is needed

			if (x > Odometer.getX()) {
				move(Global.KEEP_MOVING, true);
				while (Odometer.getX() != x) {
					if (Global.BlackLineDetected) {
						Odometer.setX(Odometer.getX()+1);
						Thread.sleep(Global.THREAD_SHORT_SLEEP_TIME);
					}
				}
			} else {
				move(-Global.KEEP_MOVING, true);
				while (Odometer.getX() != x) {
					if (Global.BlackLineDetected) {
						Odometer.setX(Odometer.getX()-1);
						Thread.sleep(Global.THREAD_SHORT_SLEEP_TIME);
					}
				}
			}

			move(Global.STOP_MOVING, false);
		}
		move(-Global.ROBOT_LENGTH, false);
		turn(-90, false);
		if (y != Odometer.getY()) {
			if (y > Odometer.getY()) {
				move(Global.KEEP_MOVING, true);
				while (Odometer.getY() != y) {
					if (Global.BlackLineDetected) {
						Odometer.setY(Odometer.getY()+1);
						Thread.sleep(Global.THREAD_SHORT_SLEEP_TIME);
					}
				}
			} else {
				move(-Global.KEEP_MOVING, true);
				while (Odometer.getY() != y) {
					if (Global.BlackLineDetected) {
						Odometer.setY(Odometer.getY()-1);
						Thread.sleep(Global.THREAD_SHORT_SLEEP_TIME);
					}
				}
			}
		}
		move(-Global.ROBOT_LENGTH, false);
		turn(90, false);
		
	}

	public void FallingEdge() throws Exception {
		final int threshhold = 50;
		// start the corresponding sensor thread
		Global.usSwitch = true;
		Global.odometerSwitch = true;
		Thread.sleep(Global.THREAD_SLEEP_TIME);

		int Angle = 0;

		// make sure there is no wall in front
		while (Global.ObstacleDistance < threshhold) {
			turn(90, false);
		}

		// make the robot face a wall
		turn(Global.KEEP_MOVING, true);
		while (Global.ObstacleDistance > threshhold) {
		}
		turn(Global.STOP_MOVING, false);

		// set this angle as starting angle
		for (int i = 0; i < 5; i++) {
			//Odometer.setTheta(0);
		}

		// redo same thing for other side
		turn(-90, false);
		turn(0 - Global.KEEP_MOVING, true);
		while (Global.ObstacleDistance > 50) {
		}
		turn(Global.STOP_MOVING, false);

		// read angle and make it positive
		Angle = (int) Odometer.getTheta();

		// divide by 2 and add 45
		if (Angle > 360) {// small correction to make sure it make no big cercles
			Angle -= 360;
		}
		
		Angle = Angle >> 1;

		/*if(Global.startingCorner == 0) { Global.theta = 0;}
		if(Global.startingCorner == 1) { Global.theta = 90;}
		if(Global.startingCorner == 2) { Global.theta = 180;}
		if(Global.startingCorner == 3) { Global.theta = 270;}*/

		turn(Angle, false);

		// turn off ussensor and odometer
		Global.usSwitch = false;
		Global.odometerSwitch = false;
	}

	public void lightPosition() throws Exception {
		// start the corresponding sensor thread
		Global.colorSensorSwitch = true;
		Thread.sleep(Global.THREAD_SLEEP_TIME); // wait color sensor to get its values

		// reset X
		// move until sensor sees black line
		move(Global.KEEP_MOVING, true);
		while (!Global.BlackLineDetected) {

		}
		// move back to black line
		move(0 - Global.ROBOT_LENGTH, false);
		Thread.sleep(250);

		// reset angle
		// turn until color sensor sees a black line then turn to 90 degree
		turn(0 - Global.KEEP_MOVING, true);
		while (!Global.BlackLineDetected) {

		}
		turn(Global.COLOR_SENSOR_OFFSET_ANGLE, false);

		// reset Y
		// move until sensor sees black line
		move(Global.KEEP_MOVING, true);
		while (!Global.BlackLineDetected) {

		}
		// move back to black line
		move(0 - Global.ROBOT_LENGTH, false);
		Thread.sleep(250);

		turn(90, false);

		// turn off color sensor
		Global.colorSensorSwitch = false;

		// wait color sensor is turned off
		Thread.sleep(200);

		// reset coordinates
		Global.angle = 0;
		Odometer.setX(-1);
		Odometer.setY(-1);
	}

	private int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	private int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public void move(double distance, boolean immediatereturn) throws Exception {

		Global.leftMotor.setSpeed(Global.MOVING_SPEED);
		Global.rightMotor.setSpeed(Global.MOVING_SPEED);

		Global.leftMotor.rotate(convertDistance(Global.WHEEL_RADIUS, distance), true);
		Global.rightMotor.rotate(convertDistance(Global.WHEEL_RADIUS, distance), immediatereturn);

		Thread.sleep(Global.THREAD_SHORT_SLEEP_TIME);
	}

	public void turn(double angle, boolean immediatereturn) throws Exception {
		Global.turning = true;
		Global.leftMotor.setSpeed(Global.ROTATING_SPEED);
		Global.rightMotor.setSpeed(Global.ROTATING_SPEED);
		if (angle > 0) {
			Global.leftMotor.rotate(convertAngle(Global.WHEEL_RADIUS, Global.TRACK, angle), true);
			Global.rightMotor.rotate(-convertAngle(Global.WHEEL_RADIUS, Global.TRACK, angle), immediatereturn);
		} else {
			angle *= -1;
			Global.leftMotor.rotate(-convertAngle(Global.WHEEL_RADIUS, Global.TRACK, angle), true);
			Global.rightMotor.rotate(convertAngle(Global.WHEEL_RADIUS, Global.TRACK, angle), immediatereturn);
		}
		Global.turning = false;
		Thread.sleep(Global.THREAD_SHORT_SLEEP_TIME);
	}
}
