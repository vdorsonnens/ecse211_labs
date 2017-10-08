package ca.mcgill.ecse211.lab4;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class LocalizationLab {
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 13.0;
	public static final double TILE_SIZE = 30.48;
	public static final double SENSOR_TO_WHEELS = 4.75;
	
	private static final Port usPort = LocalEV3.get().getPort("S3");
	private static final Port lightPort = LocalEV3.get().getPort("S2");
	
	public static void main(String[] args) {
		final TextLCD textLCD = LocalEV3.get().getTextLCD();
		final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	
		// Ultrasonic sensor
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];
		
		// Light sensor
		EV3ColorSensor lightSensor = new EV3ColorSensor(lightPort);
		SampleProvider lightIntensity = lightSensor.getRedMode();
		float[] lightData = new float[lightIntensity.sampleSize()];
		
		
		// Set up
		WheelsController wheels = new WheelsController(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		Odometer odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		
		// Handlers
		UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(wheels, odometer);
		LightLocalizer lightLocalizer = new LightLocalizer(wheels, odometer);
		
		// Pollers
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, usLocalizer);
		LightPoller lightPoller = new LightPoller(lightIntensity, lightData, lightLocalizer);
		
		// Display
		LocalizationDisplay display = new LocalizationDisplay(textLCD, odometer, usLocalizer, lightLocalizer);
		
		// Menu
		textLCD.drawString("< Rising edge", 0, 2);
		textLCD.drawString("> Falling edge", 0, 4);
		int option = 0;
		while (option == 0)
			option = Button.waitForAnyPress();
		
		// Start threads
		odometer.start();
		display.start();
		//usPoller.start();
		/*		
		switch (option) {
		case Button.ID_LEFT:
			usLocalizer.risingEdge();
			break;
		case Button.ID_RIGHT:
			usLocalizer.fallingEdge();
			break;
		 default:
			 System.out.println("Bad choice");
			 System.exit(1);
		}
		usPoller.setHandler(null);
		*/
		// wait for measurements
		Button.waitForAnyPress();
		
		// Light localize routine
		wheels.turn(45, true);
		wheels.forward();
		lightPoller.start();
		while (! lightLocalizer.isLineDetected()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException err) {}
		}
		wheels.stop();
		wheels.forward(3, true);
		lightLocalizer.lineDetected = false;
		
		lightLocalizer.localize();
		
		Button.waitForAnyPress();
		System.exit(0);
	}
	
	public static void lightLocalize() {
		
	}
}
