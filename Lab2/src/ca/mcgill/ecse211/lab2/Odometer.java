package ca.mcgill.ecse211.lab2;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	private static final double TWO_PI = 6.28319;
	
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
  private double track;
  
  private static final long ODOMETER_PERIOD = 50; /*odometer update period, in ms*/

  private Object lock; /*lock object for mutual exclusion*/

  // default constructor
  public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,  double leftRadius, double rightRadius, double track) {
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.leftRadius = leftRadius;
    this.rightRadius = rightRadius;
    this.track = track;
    this.x = 0.0;
    this.y = 0.0;
    this.theta = 0.0;
    this.leftMotorTachoCount = 0;
    this.rightMotorTachoCount = 0;
    lock = new Object();
  }

  // run method (required for Thread)
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
      deltaT = (distLeft- distRight) / this.track;
      
      synchronized (lock) {
    	  
          // update x,y,theta
          this.theta += deltaT;
          if (this.theta > TWO_PI)
        	  this.theta =this.theta - TWO_PI ;
          else if (this.theta < 0.0) 
        	  this.theta = TWO_PI + this.theta;
          
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
          // there is nothing to be done here because it is not
          // expected that the odometer will be interrupted by
          // another thread
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

  public double getX() {
    double result;

    synchronized (lock) {
      result = x;
    }

    return result;
  }

  public double getY() {
    double result;

    synchronized (lock) {
      result = y;
    }

    return result;
  }

  public double getTheta() {
    double result;

    synchronized (lock) {
      result = theta;
    }

    return result;
  }

  // mutators
  public void setPosition(double[] position, boolean[] update) {
    // ensure that the values don't change while the odometer is running
    synchronized (lock) {
      if (update[0])
        x = position[0];
      if (update[1])
        y = position[1];
      if (update[2])
        theta = position[2];
    }
  }

  public void setX(double x) {
    synchronized (lock) {
      this.x = x;
    }
  }

  public void setY(double y) {
    synchronized (lock) {
      this.y = y;
    }
  }

  public void setTheta(double theta) {
    synchronized (lock) {
      this.theta = theta;
    }
  }

  /**
   * @return the leftMotorTachoCount
   */
  public int getLeftMotorTachoCount() {
    return leftMotorTachoCount;
  }

  /**
   * @param leftMotorTachoCount the leftMotorTachoCount to set
   */
  public void setLeftMotorTachoCount(int leftMotorTachoCount) {
    synchronized (lock) {
      this.leftMotorTachoCount = leftMotorTachoCount;
    }
  }

  /**
   * @return the rightMotorTachoCount
   */
  public int getRightMotorTachoCount() {
    return rightMotorTachoCount;
  }

  /**
   * @param rightMotorTachoCount the rightMotorTachoCount to set
   */
  public void setRightMotorTachoCount(int rightMotorTachoCount) {
    synchronized (lock) {
      this.rightMotorTachoCount = rightMotorTachoCount;
    }
  }
}
