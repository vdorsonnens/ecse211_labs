package ca.mcgill.ecse211.lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread  {
	
	private static final long ODOMETER_PERIOD = 50; /*odometer update period, in ms*/
	
	// robot position
	private double x;
	private double y;
	private double theta;
    private int leftMotorTachoCount;
	private int rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor;
    private EV3LargeRegulatedMotor rightMotor;
	  
	private double leftRadius;
	private double rightRadius;
	private double trackLength;
	
	private Object lock;
	
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double leftRadius, double rightRadius, double trackLength) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.trackLength = trackLength;
		
		this.x = this.y = this.theta = 0.0;
		this.rightMotorTachoCount = this.leftMotorTachoCount = 0;
		
		this.lock = new Object();
	}
	
	
	
	public void run() {
		long updateStart, updateEnd;
	    
	    this.leftMotor.resetTachoCount();
	    this.rightMotor.resetTachoCount();
	    
	    int leftTachoCountNow = 0;
	    int rightTachoCountNow = 0;
	    double distLeft, distRight, deltaD, deltaT, deltaX, deltaY;

	    while (true) {
	      updateStart = System.currentTimeMillis();
	      
	      // get new tacho counts
	      leftTachoCountNow = this.leftMotor.getTachoCount();
	      rightTachoCountNow = this.rightMotor.getTachoCount();
	      
	      // Compute l/r distances
	      distLeft = (leftTachoCountNow-this.leftMotorTachoCount) * this.leftRadius * 3.14159 / 180.0;
	      distRight = (rightTachoCountNow-this.rightMotorTachoCount) * this.rightRadius * 3.14159 / 180.0;
	      
	      // update tacho counts
	      this.leftMotorTachoCount = leftTachoCountNow;
	      this.rightMotorTachoCount = rightTachoCountNow;
	      
	      deltaD = (distLeft + distRight) / 2;
	      deltaT = (distLeft- distRight) / this.trackLength;
	      
	      synchronized (lock) {
	    	  
	          // update x,y,theta
	          this.theta += deltaT;
	          if (this.theta > NavigationLab.TWO_PI)
	        	  this.theta =this.theta - NavigationLab.TWO_PI ;
	          else if (this.theta < 0.0) 
	        	  this.theta = NavigationLab.TWO_PI + this.theta;
	          
	          deltaY = deltaD * Math.sin(this.theta);
	          deltaX = deltaD * Math.cos(this.theta);
	          this.x += deltaX;
	          this.y += deltaY;
	    	}

	      // this ensures that the odometer only runs once every period
	      updateEnd = System.currentTimeMillis();
	      if (updateEnd - updateStart < ODOMETER_PERIOD) {
	        try {
	          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
	        } catch (InterruptedException e) {
	        }
	      }
	    }
	}
	
	public void getPosition(double[] position, boolean[] update) {
	    // ensure that the values don't change while the odometer is running
	    synchronized (lock) {
	      if (update[0])
	        position[0] = x;
	      if (update[1])
	        position[1] = y;
	      if (update[2])
	        position[2] = theta;
	    }
	  }
}
