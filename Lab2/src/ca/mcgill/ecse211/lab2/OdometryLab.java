// Lab2.java

package ca.mcgill.ecse211.lab2;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class OdometryLab {

  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  
  private static final Port lsPort = LocalEV3.get().getPort("S3");
  
  public static final double TILE_SIZE = 30.48;
  public static final double NUM_TILE_X = 3.0;
  public static final double NUM_TILE_Y = 3.0;

  public static final double WHEEL_RADIUS = 2.1;
  public static final double TRACK = 13.0;

  public static void main(String[] args) {
    int buttonChoice;	

    // setup sensor
    EV3ColorSensor sensor = new EV3ColorSensor(lsPort);
    SampleProvider lsColor = sensor.getColorIDMode();
    float[] lsData = new float[lsColor.sampleSize()];
    
    final TextLCD t = LocalEV3.get().getTextLCD();
    Odometer odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK);
    OdometryCorrection odometryCorrection = new OdometryCorrection(odometer, sensor);
    OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, odometryCorrection, t);
 
    
    do {
      // clear the display
      t.clear();

      // ask the user whether the motors should drive in a square or float
      t.drawString("< Left | Right >", 0, 0);
      t.drawString("       |        ", 0, 1);
      t.drawString(" Float | Drive  ", 0, 2);
      t.drawString("motors | in a   ", 0, 3);
      t.drawString("       | square ", 0, 4);

      buttonChoice = Button.waitForAnyPress();
    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

    if (buttonChoice == Button.ID_LEFT) {

      leftMotor.forward();
      leftMotor.flt();
      rightMotor.forward();
      rightMotor.flt();

      odometer.start();
      odometryDisplay.start();

    } else {
      // clear the display
      t.clear();

      // ask the user whether the motors should drive in a square or float
      t.drawString("< Left | Right >", 0, 0);
      t.drawString("  No   | with   ", 0, 1);
      t.drawString(" corr- | corr-  ", 0, 2);
      t.drawString(" ection| ection ", 0, 3);
      t.drawString("       |        ", 0, 4);
      
      buttonChoice = Button.waitForAnyPress();
 
      if(buttonChoice == Button.ID_RIGHT){
          odometryCorrection.start();
        }
      
      odometer.start();
      odometryDisplay.start();
 
      
      // spawn a new Thread to avoid SquareDriver.drive() from blocking
      (new Thread() {
        public void run() {
          SquareDriver.drive(leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK);
        }
      }).start();
    }

    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}
