package lab5;

import lab5.main.Global;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Display extends Thread {

	public static int SLEEP_TIME = 500;
	private static final long DISPLAY_PERIOD = 250;
	private Odometer odometer;
	private TextLCD t;
	
	
	public Display(Odometer odometer) {
		this.odometer = odometer; 
	}
	
	public void run() {
		final TextLCD t = LocalEV3.get().getTextLCD();
	    double[] position = new double[3];
		
	
		while (true) {
			
			if(Global.initializing) {
			    t.clear();
				t.drawString(Global.firstLine, 0, 0);
				t.drawString(Global.secondLine, 0, 1);
				t.drawString(Global.thirdLine, 0, 2);
				t.drawString(Global.forthLine, 0, 3);
				t.drawString(Global.fifthLine, 0, 4);
				
			} else {
				
				t.clear();
			      // clear the lines for displaying odometry information
			      t.drawString("X:              ", 0, 0);
			      t.drawString("Y:              ", 0, 1);
			      t.drawString("T:              ", 0, 2);
			      
			      // get the odometry information
			      odometer.getPosition(position, new boolean[] {true, true, true});

			      // display odometry information
			      for (int i = 0; i < 3; i++) {
			        t.drawString(formattedDoubleToString(position[i], 2), 3, i);
			      }		      
			}
			
			
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch bl
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
