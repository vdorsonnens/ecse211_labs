package ca.mcgill.ecse211.lab3;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class NavigationLab {
    
    // TODO add other paths
    private static final double[][][] MAPS = new double[][][]{
        {{2, 0}, {1, -1}, {2, -2}, {1, -2}, {0, -1}},
        {{1, -1}, {2, 0}, {2, -2}, {1, -2}, {0, -1}},
        {{0, -1}, {1, -2}, {2, -2}, {2, 0}, {1, -1}},
        {{1, 0}, {2, -1}, {0, -1}, {1, -2}, {2, -2}}
    };
    private static final int MAP_INDEX = 3; // just change this to navigate a different map

	public static final double PI = 3.14159;
	public static final double TWO_PI = 6.28319;
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK_LENGTH = 13.0;
	public static final double TILE_SIZE = 30.48;
	
	private static final Port usPort = LocalEV3.get().getPort("S3");
	
	public static void main(String[] args) {
	
        final double[][] path = MAPS[MAP_INDEX];
        for (int i=0; i<path.length; i++) {
        	path[i][0] *= TILE_SIZE;
        	path[i][1] *= TILE_SIZE;
        }
		
		final TextLCD textLCD = LocalEV3.get().getTextLCD();
		final EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		final EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		
		// Setup ultrasonic sensor
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];
		
		// Get a wheel controller
		WheelsController wheelsController = new WheelsController(motorLeft, motorRight, WHEEL_RADIUS, WHEEL_RADIUS, TRACK_LENGTH);
		
		// Instantiate threads controlling the robot
		Odometer odometer = new Odometer(motorLeft, motorRight, WHEEL_RADIUS, WHEEL_RADIUS, TRACK_LENGTH);
		UltrasonicController obstacleAvoider = new ObstacleAvoider(wheelsController);
		Driver driver = new Driver(path, odometer, wheelsController, (ObstacleAvoider) obstacleAvoider);
		OdometerDisplay display = new OdometerDisplay(textLCD, odometer, driver, obstacleAvoider);
		UltrasonicPoller poller = new UltrasonicPoller(usSensor, usData, obstacleAvoider);
		
		// Wait for a button to start
		textLCD.clear();
		textLCD.drawString("Press any button", 0, 0);
		Button.waitForAnyPress();
		textLCD.clear();
		
		odometer.start();
		display.start();
		poller.start();
		driver.start();
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
	    System.exit(0);
	}
}
