package ca.mcgill.ecse211.lab3;

public class Driver extends Thread {
	
	private Odometer odometer;
	private WheelsController wheelsController;
	private ObstacleAvoider avoider;
	private double[][] path;
	private int pathLength;
	private int nextCoordinatesIndex;
	private double[] nextCoordinates;
	private double[] currentDirectionVector;
    
    private double distance;
    private double angle;
    
    public boolean atCoordinates;
	public boolean avoiding;
    
	public Driver(double[][] path, Odometer odometer, WheelsController wheelsController, ObstacleAvoider avoider) {
		this.path = path;
		this.odometer = odometer;
		this.wheelsController = wheelsController;
		this.pathLength = path.length;
		this.nextCoordinatesIndex = 0;
		this.nextCoordinates = null;
		this.currentDirectionVector = new double[] {1.0, 0.0}; // starts towards x axis
	    this.distance = 0;
        this.angle = 0;
        this.atCoordinates = false;
        this.avoider = avoider;
        this.avoiding = false;
    }
	
	// stupid driver without who do not avoid objects
	public void run() {
		double[] robotPosition = new double[3];
		boolean[] update = new boolean[] {true, true, true};
		double[] nextDirectionVector = new double[2];
		
		// loop through the coordinates to get from one to another
		while (nextCoordinatesIndex < pathLength) {
			
            // fetch robot's position
            nextCoordinates = path[nextCoordinatesIndex];
			odometer.getPosition(robotPosition, update);
			
			// calculate the angle to turn to the next coordinates and the distance to travel
			nextDirectionVector[0] = nextCoordinates[0] - robotPosition[0];
			nextDirectionVector[1]  = nextCoordinates[1] - robotPosition[1];
			//normalize(nextDirectionVector);
            this.angle = angleToCoordinates(currentDirectionVector, nextDirectionVector, robotPosition[2]);
			this.distance = vectorLength(nextDirectionVector);
			
			// blocking methods
			avoider.setTurning(true);
			wheelsController.turnTo(angle);
			avoider.setTurning(false);
			wheelsController.travelTo(distance, false);
			this.atCoordinates = false;
			
			while (!this.atCoordinates) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				if (! avoider.avoiding()) {
					odometer.getPosition(robotPosition, update);
					double dX = Math.abs(nextCoordinates[0] - robotPosition[0]);
					double dY = Math.abs(nextCoordinates[1] - robotPosition[1]);
					if (dX < 2 && dY < 2) {
						this.atCoordinates = true;
					}
				} else {
					this.avoiding = true;
					break;
				}
			}
			
			if (this.avoiding) {
				while (avoider.avoiding()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
				odometer.getPosition(robotPosition, update);
				currentDirectionVector[0] = Math.cos(robotPosition[2]);
				currentDirectionVector[1] = Math.sin(robotPosition[2]);
				this.avoiding = false;
				
			} else {
				currentDirectionVector[0] = nextDirectionVector[0];
	            currentDirectionVector[1] = nextDirectionVector[1];
				nextCoordinatesIndex++;
			}
			
			if (nextCoordinatesIndex == pathLength)
				avoider.setTurning(true);
		}
	}
	
	private double angleToCoordinates(double[] currentDirection, double[] nextDirection, double currentAngle) {
		// no division by 0 in here
		if (nextDirection[0] == 0) {
			nextDirection[0] += 0.0001;
		}
		
		// angle from +x counter-clockwise to next direction
		double angle = Math.atan(nextDirection[1]/nextDirection[0]);
		angle = Math.abs(angle);
		
		if (nextDirection[0] < 0 && nextDirection[1] > 0)
			angle = Math.PI - angle;
		else if (nextDirection[0] < 0 && nextDirection[1] < 0)
			angle += Math.PI;
		else if (nextDirection[0] > 0 && nextDirection[1] < 0)
			angle = 2*Math.PI - angle;
		
		// minimal angle
		angle = angle - currentAngle;
		
		if (angle < -Math.PI)
			angle = 2*Math.PI + angle;
		else if (angle > Math.PI) {
			angle = angle - 2*Math.PI;
		}
		
		// convert angle to degrees
		return 180.0*angle/Math.PI;	
	}
	
	private double vectorLength(double[] vec) {
		return Math.sqrt(Math.pow(vec[0], 2) + Math.pow(vec[1], 2));
	}

    public void getNext(double[] buffer) {
        buffer[0] = this.distance;
        buffer[1] = this.angle;
    }
   
}
