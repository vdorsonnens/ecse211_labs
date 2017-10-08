package ca.mcgill.ecse211.lab4;

public class UltrasonicLocalizer implements UltrasonicHandler {
	
	private static final int D = 20;
	private static final int K= 1;
	private static final int SLEEP_TIME = 25;
	
	WheelsController wheels;
	Odometer odometer;
	private int distance;
	private int index;
	
	public UltrasonicLocalizer(WheelsController wheels, Odometer odometer) {
		this.odometer = odometer;
		this.wheels = wheels;
		this.distance = 20;
		this.index = 0;
	}
	
	public void processUSData(int distance) {
		if (distance > 250)
			distance = 250;
		this.distance = distance;
		//System.out.println(this.index + ", " + this.distance);
		//this.index++;
	};
	
	public int readUSDistance() {
		return this.distance;
	}
	
	public void fallingEdge() {
		
		this.distance = 250;
		
		// starts away from the wall
		double[] position = new double[3];
		boolean[] update = new boolean[] {true, true, true};
		double angleAlpha, angleBeta;
		
		
		// get first angle
		wheels.turn(-270, false);
		while(true) {	
			if (this.distance < D-K) {
				wheels.stop();
				odometer.getPosition(position, update);
				angleAlpha = position[2];
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err){}
		}
		
		// second angle
		wheels.turn(90, true);
		
		wheels.turn(270, false);
		while(true) {
			if (this.distance < D-K) {
				wheels.stop();
				odometer.getPosition(position, update);
				angleBeta = position[2];
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err){}
		}
		
		// correct angle
		double correction = meanHeading(angleAlpha, angleBeta);
		
		odometer.getPosition(position, update);
		odometer.setAngle(position[2] + correction);
		
		wheels.turn(360-(180*(position[2]+correction) / Math.PI), true);
	}
	
	public void risingEdge() {
		// starts facing the wall
		
		this.distance = 0;
		
		// starts away from the wall
		double[] position = new double[3];
		boolean[] update = new boolean[] {true, true, true};
		double angleAlpha, angleBeta;
		
		
		// get first angle
		wheels.turn(270, false);
		while(true) {	
			if (this.distance > D+K) {
				wheels.stop();
				odometer.getPosition(position, update);
				angleAlpha = position[2];
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err){}
		}
		
		//System.out.println("alpha: " + angleAlpha);
		
		// second angle
		wheels.turn(-30, true);
		
		wheels.turn(-270, false);
		while(true) {
			if (this.distance > D+K) {
				wheels.stop();
				odometer.getPosition(position, update);
				angleBeta = position[2];
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException err){}
		}
		
		//System.out.println("beta: " + angleBeta);
		
		// correct angle
		double correction = meanHeading(angleAlpha, angleBeta);
		
		odometer.getPosition(position, update);
		odometer.setAngle(position[2] + correction);
		
		wheels.turn(360-(180*(position[2]+correction) / Math.PI), true);
		
		
	}
	
	public double meanHeading(double a1, double a2) {
		if (a1 > a2) {
			return 5.0*Math.PI/4.0 - (a1+a2)/2;
		} else {
			return Math.PI/4.0 - (a1+a2)/2;
		}
	}
	
}
