package ca.mcgill.ecse211.lab3;

import ca.mcgill.ecse211.lab3.Odometer;
import lejos.hardware.lcd.TextLCD;

public class OdometerDisplay extends Thread {
	
	private static final long DISPLAY_PERIOD = 250;
	private Odometer odometer;
	private TextLCD textLCD;
    private Driver driver;
    private UltrasonicController avoider;
	
	public OdometerDisplay(TextLCD textLCD, Odometer odometer, Driver driver, UltrasonicController avoider) {
		this.textLCD = textLCD;
		this.odometer = odometer;
        this.driver = driver;
        this.avoider = avoider;
	}
	
	public void run() {
		long displayStart, displayEnd;
	    double[] position = new double[3];
        double[] direction = new double[2];

	    // clear the display once
	    textLCD.clear();

	    while (true) {
	      displayStart = System.currentTimeMillis();

	      // clear the lines for displaying odometry information
	      textLCD.drawString("X:              ", 0, 0);
	      textLCD.drawString("Y:              ", 0, 1);
	      textLCD.drawString("T:              ", 0, 2);

          textLCD.drawString("distance:          ", 0, 4);
          textLCD.drawString("angle:       ", 0, 5);
          
          textLCD.drawString("sensor:       ", 0, 6);
	   
	      // get the odometry information
	      odometer.getPosition(position, new boolean[] {true, true, true});
	      driver.getNext(direction);

	      // change rad to deg
	      position[2] = 180.0*position[2] / 3.14159;
	      
	      // display odometry information
	      for (int i = 0; i < 3; i++) {
	        textLCD.drawString(formattedDoubleToString(position[i], 2), 3, i);
	      }

          // display driver information
          for (int i=0; i<2; i++) {
            textLCD.drawString(formattedDoubleToString(direction[i], 2), 8, i+4);    
          }
          
          // display sensor info
          textLCD.drawString(Integer.toString(this.avoider.readUSDistance()), 8, 6);


	      // throttle the OdometryDisplay
	      displayEnd = System.currentTimeMillis();
	      if (displayEnd - displayStart < DISPLAY_PERIOD) {
	        try {
	          Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
	        } catch (InterruptedException e) {
	          // there is nothing to be done here because it is not
	          // expected that OdometryDisplay will be interrupted
	          // by another thread
	        }
	      }
	    }
	}
	
	private static String formattedDoubleToString(double x, int places) {
	    String result = "";
	    String stack = "";
	    long t;

	    // put in a minus sign as needed
	    if (x < 0.0)
	      result += "-";

	    // put in a leading 0
	    if (-1.0 < x && x < 1.0)
	      result += "0";
	    else {
	      t = (long) x;
	      if (t < 0)
	        t = -t;

	      while (t > 0) {
	        stack = Long.toString(t % 10) + stack;
	        t /= 10;
	      }

	      result += stack;
	    }

	    // put the decimal, if needed
	    if (places > 0) {
	      result += ".";

	      // put the appropriate number of decimals
	      for (int i = 0; i < places; i++) {
	        x = Math.abs(x);
	        x = x - Math.floor(x);
	        x *= 10.0;
	        result += Long.toString((long) x);
	      }
	    }

	    return result;
	  }
}
