package ca.mcgill.ecse211.lab3;

public class Driver extends Thread {
	
	private Odometer odometer;
	private WheelsController wheelsController;
	private double[][] path;
	private int pathLength;
	private int nextCoordinatesIndex;
	private double[] nextCoordinates;
	private double[] currentDirectionVector;
	
	
	public Driver(double[][] path, Odometer odometer, WheelsController wheelsController) {
		this.path = path;
		this.odometer = odometer;
		this.wheelsController = wheelsController;
		this.pathLength = path.length;
		this.nextCoordinatesIndex = 0;
		this.nextCoordinates = null;
		this.currentDirectionVector = new double[] {1.0, 0.0}; // starts towards x axis
	}
	
	// stupid driver without who do not avoid objects
	public void run() {
		double[] robotPosition = new double[3];
		boolean[] update = new boolean[] {true, true, true};
		double[] nextDirectionVector = new double[2];
		double angle;
		double distance;
		
		// loop through the coordinates to get from one to another
		while (nextCoordinatesIndex < pathLength) {
			nextCoordinates = path[nextCoordinatesIndex];
			odometer.getPosition(robotPosition, update);
			
			// calculate the angle to turn to the next coordinates and the distance to travel
			nextDirectionVector[0] = nextCoordinates[0] - robotPosition[0];
			nextDirectionVector[1]  = nextCoordinates[1] - robotPosition[1];
			angle = angleToCoordinates(currentDirectionVector, nextDirectionVector);
			distance = Math.sqrt(Math.pow(nextDirectionVector[0], 2.0) + Math.pow(nextDirectionVector[1], 2.0));
			
			// blocking methods
			wheelsController.turn(angle);
			wheelsController.forward(distance);
			// should be at coordinates now, so get to the next one
			
			nextCoordinatesIndex++;
		}
	}
	
	private double angleToCoordinates(double[] currentDirection, double[] nextDirection) {
		// use dot product formulas
		double angle =  Math.acos(vectorDot(currentDirection, nextDirection) / (vectorLength(currentDirection) * vectorLength(nextDirection)));
		return angle;
	}
	
	private double vectorLength(double[] vec) {
		return Math.sqrt(Math.pow(vec[0], 2) + Math.pow(vec[1], 2));
	}
	
	private double vectorDot(double[] a, double[] b) {
		double dot = 0;
		for (int i=0; i<a.length; i++)
			dot += a[i] * b[i];
		return dot;
	}
	
	private double distanceToCoordinates(double[] currentPosition, double[] nextPosition) {
		double deltaX = nextPosition[0] - currentPosition[0];
		double deltaY = nextPosition[1] - currentPosition[1];
		return Math.sqrt(Math.pow(deltaX, 2.0) + Math.pow(deltaY, 2.0));
	}
}
