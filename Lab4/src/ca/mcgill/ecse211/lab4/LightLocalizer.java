package ca.mcgill.ecse211.lab4;

import lejos.hardware.Button;
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
				//Sound.beep();
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
		
		double thetaYNeg, thetaXNeg;
		double angle1, angle2;
		
		
		// find first line on X
		wheels.turn(-90, false);
		stopAfterNLines(1);
		odometer.getPosition(position, update);
		angle1 = position[2];
		
		wheels.turn(45, true);
		this.lineDetected = false;
		
		// find second line on X
		wheels.turn(180, false);
		stopAfterNLines(2);
		odometer.getPosition(position, update);
		angle2 = position[2];
		thetaXNeg = angle2;
		
		// calculate y
		double thetaX = angle2-angle1;
		double y = -1 * LocalizationLab.SENSOR_TO_WHEELS * Math.cos(thetaX/2.0);
		odometer.setY(y);
		
		
		
		// find first line on Y
		wheels.turn(180, false);
		stopAfterNLines(1);
		odometer.getPosition(position, update);
		angle1 = position[2];
		thetaYNeg = angle1;
		
		// find second line on Y
		wheels.turn(270, false);
		stopAfterNLines(2);
		odometer.getPosition(position, update);
		angle2 = position[2];
		
		
		// calculate X
		double thetaY = angle1-angle2;
		double x =1* LocalizationLab.SENSOR_TO_WHEELS * Math.cos(thetaY/2.0);
		odometer.setX(x);
		
		
		// calculate delta_theta for x and y
		double deltaTheta1 =  - (thetaYNeg - Math.PI) + thetaY/2.0;		
		double deltaTheta2 = -Math.PI/2.0 - (thetaXNeg - Math.PI) + thetaX/2.0;
		// average
		double deltaTheta = (deltaTheta1 + deltaTheta2)/2;
		
		// update the angle
		odometer.getPosition(position, update);
		odometer.setAngle(position[2]+deltaTheta);
				
		// Travel to (0, 0) if needed
		if (Math.abs(position[0]) > 2 || Math.abs(position[1]) > 2) {
			
			double dx = Math.abs(position[0]);
			double dy = Math.abs(position[1]);
			
			double angle = 90.0 - 180.0/Math.PI * Math.atan(dx/dy);
			wheels.turn(position[2]-angle, true);
			wheels.forward(Math.sqrt(Math.pow(dx,  2) + Math.pow(dy, 2)), true);
			
		} else if (Math.abs(position[0]) > 2  ) {
			wheels.forward(-1*position[0], true);
		}

		
		// Set the direction to 0 degree;
		odometer.getPosition(position, update);
		wheels.turn(360-position[2]*180/Math.PI, true);
	}
	
	public int readLightData() {
		return this.lightIntensity;
	}
	
	public void stopAfterNLines(int n) {
		int numLines = 0;
		while (true) {
			if (isLineDetected()) {
				numLines++;
				if (numLines == n) {
					wheels.stop();
					break;
				}
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err) {}
		}
	}
}
