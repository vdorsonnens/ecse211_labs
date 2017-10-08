package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;

public class LightLocalizer implements LightHandler {
	
	private static final int RISING_EDGE = 100;
	private static final int FALLING_EDGE = -100;
	
	private static final int SLEEP_TIME = 25;
	
	private boolean risingEdge;
	private boolean fallingEdge;
	public boolean lineDetected;
	
	WheelsController wheels;
	private Odometer odometer;
	int lightIntensity;
	int lastIntensity;
	int derivative;
	int idx;
	
	public LightLocalizer(WheelsController wheels, Odometer odometer) {
		this.odometer = odometer;
		this.lightIntensity = 900;
		this.lastIntensity = 900;
		this.derivative = 0;
		this.risingEdge = false;
		this.fallingEdge = false;
		this.lineDetected = false;
		this.wheels = wheels;
		this.idx = 0;
	}
	
	public void processLightData(int data) {
		this.lightIntensity = data;
		this.derivative = this.lightIntensity - this.lastIntensity;
		this.lastIntensity = this.lightIntensity;
		
		if (!this.fallingEdge && !this.risingEdge) {
			if (this.derivative < FALLING_EDGE) {
				// start of falling edge
				this.fallingEdge = true;
			}  
		}
		else if (this.fallingEdge) {
			if (this.derivative > RISING_EDGE) {
				// end of falling edge, start of rising
				this.fallingEdge = false;
				this.risingEdge = true;
				Sound.beep();
				lineDetected();
			}
		} 
		else if (this.risingEdge && this.derivative < RISING_EDGE) {
			// end of rising edge	
			this.risingEdge = false;
		}
		
	}
	
	public void lineDetected() {
		this.lineDetected = true;
	}
	
	public boolean isLineDetected() {
		if (this.lineDetected) {
			this.lineDetected = false;
			return true;
		} else {
			return false;
		}
	}
	
	public void localize() {
		
		double[] position = new double[3] ;
		boolean[] update = new boolean[] {true, true, true};
		
		double angle1, angle2;
		
		wheels.turn(-90, false);
		while (true) {
			if (isLineDetected()) {
				wheels.stop();
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err) {}
		}
		odometer.getPosition(position, update);
		angle1 = position[2];
		
		wheels.turn(45, true);
		this.lineDetected = false;
		
		int numLines = 0;
		wheels.turn(180, false);
		while (true) {
			if (isLineDetected()) {
				if (numLines == 1) {
					wheels.stop();
					break;
				} else {
					numLines++;	
				}
			}
			
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err) {}
		}
		odometer.getPosition(position, update);
		angle2 = position[2];
		
		double thetaY = angle1-angle2;
		double x = -1 * LocalizationLab.SENSOR_TO_WHEELS * Math.cos(thetaY/2.0);
		odometer.setX(x);
		
		// Y
		wheels.turn(180, false);
		while (true) {
			if (isLineDetected()) {
				wheels.stop();
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err) {}
		}
		odometer.getPosition(position, update);
		angle1 = position[2];
		
		numLines = 0;
		wheels.turn(270, false);
		while (true) {
			if (isLineDetected()) {
				if (numLines == 1) {
					wheels.stop();
					break;
				} else {
					numLines++;	
				}
			}
			
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err) {}
		}
		odometer.getPosition(position, update);
		angle2 = position[2];
		
		
		double thetaX = angle1-angle2;
		double y = -1 * LocalizationLab.SENSOR_TO_WHEELS * Math.cos(thetaX/2.0);
		odometer.setY(y);
		
		double deltaTheta = Math.PI/2.0 - (thetaY - Math.PI) + thetaY/2.0;
		System.out.println(180.0*deltaTheta/Math.PI);
		
		deltaTheta = Math.PI/2.0 - (thetaX - Math.PI) + thetaX/2.0;
		System.out.println(180.0*deltaTheta/Math.PI);
		
		odometer.getPosition(position, update);
		System.out.println(position[0]);
		System.out.println(position[1]);
		System.out.println(position[2]);
	}
	
	public int readLightData() {
		return this.lightIntensity;
	}
}
