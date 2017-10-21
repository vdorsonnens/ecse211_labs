package lab5;

import lab5.main.Global;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
  // robot position
  private static double x;
  private static double y;
  private static double theta;
  private int leftMotorTachoCount;                //Current tachometer count for left wheel
  private int rightMotorTachoCount;               //Current tachometer count for right wheel
  private int nextLeftMotorTachoCount;			  //Next tachometer count for left wheel
  private int nextRightMotorTachoCount;			  //Next tachometer count for left wheel
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;

  private static final long ODOMETER_PERIOD = 25; /*odometer update period, in ms*/

  private static Object lock; /*lock object for mutual exclusion*/

  // default constructor
  public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    Odometer.x = 0;         //As the origin is set to be the one shown
    Odometer.y = 0;         //in the lab instructions
    Odometer.theta = 0.0;
    this.leftMotorTachoCount = 0;
    this.rightMotorTachoCount = 0;
    lock = new Object();
  }

  // run method (required for Thread)
  public void run() {
    long updateStart, updateEnd;

    while (true) {
      updateStart = System.currentTimeMillis();
      
      double DistL = 0;           //Distance covered by the left wheel
      double DistR = 0;			  //Distance covered by the right wheel
      double deltaD = 0;		  //Change in distance
      double deltaT = 0;		  //Change in the theta (the angle the robot is facing)
      double dX = 0;			  //Change in X
      double dY = 0;			  //Change in Y

      nextLeftMotorTachoCount = leftMotor.getTachoCount();		//Revolutions in left motor
      nextRightMotorTachoCount= rightMotor.getTachoCount();		//Revolutions in right motor
      
      DistL = Global.WHEEL_RADIUS*Math.PI*(nextLeftMotorTachoCount - leftMotorTachoCount)/180;     //Calculating the distance covered by the left wheel
      DistR = Global.WHEEL_RADIUS*Math.PI*(nextRightMotorTachoCount - rightMotorTachoCount)/180;   //Calculating the distance covered by the right wheel
      
      this.setLeftMotorTachoCount(nextLeftMotorTachoCount);          //Setting the tachometer
      this.setRightMotorTachoCount(nextRightMotorTachoCount);		 //counts to current ones
      
      deltaD = (0.5)*(DistL + DistR);      //Calculating changed in distance traveled by the robot
      deltaT = (DistL - DistR)/(Global.TRACK);    //Calculating the change in the robot's angle
      
      
      

      synchronized (lock) {        
    	  
    	//The calculations are performed with theta in radians (values are converted from degrees to radians by multiplying with pi/180). The results are all displayed in degrees (Radians are converted to degrees by multiplying them by 180/pi).
    	Odometer.setTheta(((Odometer.getTheta()*(Math.PI/180)) + deltaT)*(180/Math.PI));
    	if((Odometer.getTheta()*(Math.PI/180)) >= 2*Math.PI){     //If the angle exceeds 360 degrees (2pi), reset it to 0 degrees
    		Odometer.setTheta(((Odometer.getTheta()*(Math.PI/180)) - 2*Math.PI)*(180/Math.PI)) ;
    	}
        
    	if(Odometer.getTheta() < 0){ //If the angle drops below 0 degrees, readjust it to drop from 360 degrees.
    		Odometer.setTheta(((Odometer.getTheta()*(Math.PI/180)) + 2*Math.PI)*(180/Math.PI));
    	}
        
        
        
        dX = deltaD * Math.sin(Odometer.getTheta()*(Math.PI/180));   //Calculating the change in X
        dY = deltaD * Math.cos(Odometer.getTheta()*(Math.PI/180));   //Calculating the change in Y
        
        Odometer.setX(Odometer.getX() + dX);   //Updating the X position
        Odometer.setY(Odometer.getY() + dY);   //Updating the Y position     
        
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

  //Given below are the getters and setters for various variables
  
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

  public static double getX() {
    double result;

    synchronized (lock) {
      result = x;
    }

    return result;
  }

  public static double getY() {
    double result;

    synchronized (lock) {
      result = y;
    }

    return result;
  }

  public static double getTheta() {
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

  public static void setX(double inX) {
    synchronized (lock) {
      Odometer.x = inX;
    }
  }

  public static void setY(double inY) {
    synchronized (lock) {
      Odometer.y = inY;
    }
  }

  public static void setTheta(double inTheta) {
    synchronized (lock) {
      Odometer.theta = inTheta;
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
