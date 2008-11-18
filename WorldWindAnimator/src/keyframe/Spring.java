package keyframe;

import path.Vector;
import path.Vector1;

public class Spring<V extends Vector<V>>
{
	private final static double DEFAULT_SPRING_K = 4d;
	private final static double DEFAULT_DAMPING_K = 4d;
	private final static double DEFAULT_TIMESTEP = 1d / 200d;

	private double springK = DEFAULT_SPRING_K;
	private double dampingK = DEFAULT_DAMPING_K; //2 x sqrt(spring)
	private double timestep = DEFAULT_TIMESTEP;

	public static final double DBL_EPSILON = 2.220446049250313E-16d;

	public static void main(String[] args)
	{
		Spring<Vector1> spring = new Spring<Vector1>();
		Vector1 start = new Vector1(0);
		Vector1 end = new Vector1(1000);
		Vector1 initialVelocity = new Vector1(0);
		boolean allowDeceleration = true;
		double time = 100;
		double velocity = spring.velocityForTime(time, start, end,
				initialVelocity, allowDeceleration);
		//double velocity = 12.63175425164036;
		double calctime = spring.timeToDestination(start, end, initialVelocity,
				velocity, allowDeceleration, 0);
		System.out.println(time + " = " + velocity);
		System.out.println(calctime + " = " + velocity);
	}

	public double velocityForTime(double time, V start, V end,
			V initialVelocity, boolean allowDeceleration)
	{
		double minVel = 0;
		double maxVel = Double.MAX_VALUE;
		double deltaVel = Double.MAX_VALUE;
		double velStep = maxVel / 10;
		double currentTime = timeToDestination(start, end, initialVelocity,
				deltaVel, allowDeceleration, DBL_EPSILON);

		//if the max velocity still doesn't get us there in time, then return -1
		if (currentTime > time)
			return -1;

		while (Math.abs(time - currentTime) > DBL_EPSILON)
		{
			deltaVel -= velStep;

			//System.out.println("Calculating time for vel = " + vel);
			currentTime = timeToDestination(start, end, initialVelocity, minVel
					+ deltaVel, allowDeceleration, DBL_EPSILON);
			//System.out.println("Time = " + currentTime);

			if (currentTime > time)
			{
				minVel = deltaVel + minVel;
				deltaVel += velStep;
				velStep /= 2;
			}
			
			System.out.println(minVel + " " + deltaVel + " " + velStep);


			/*System.out.println(velstep);
			System.out.println(vel);*/
			//System.out.println(Math.abs(currentTime - time));
			//System.out.println(vel);
		}

		return deltaVel;
	}

	/**
	 * @param start
	 *            Initial object position
	 * @param end
	 *            Final object position
	 * @param initialVelocity
	 *            Initial velocity
	 * @param maxVelocity
	 *            Maximum velocity
	 * @param allowDeceleration
	 *            Allow the object to slow down
	 * @param distanceFromEnd
	 *            Return time when object is <code>distanceFromEnd</code> from
	 *            <code>end</code>
	 * @return Time for object to travel from <code>start</code> to
	 *         <code>end</code>
	 */
	public double timeToDestination(V start, V end, V initialVelocity,
			double maxVelocity, boolean allowDeceleration,
			double distanceFromEnd)
	{
		distanceFromEnd = Math.max(distanceFromEnd, DBL_EPSILON);
		maxVelocity = Math.abs(maxVelocity);

		V step = start.createNew();
		V temp = start.createNew();

		V difference = end.subtract(start);
		V velocity = initialVelocity.clone();
		double time = 0;

		V lastDifference1 = difference.clone();
		V lastDifference2 = difference.clone();
		V lastVelocity1 = velocity.clone();
		V lastVelocity2 = velocity.clone();
		double lastTime1 = time;
		double lastTime2 = time;

		double timestep = this.timestep;
		double distance = difference.distance();
		double lastDistance = distance;
		double initialDistance = distance;

		while (distance > distanceFromEnd || lastDistance > distanceFromEnd)
		{
			lastDifference2.set(lastDifference1);
			lastVelocity2.set(lastVelocity1);
			lastTime2 = lastTime1;

			lastDifference1.set(difference);
			lastVelocity1.set(velocity);
			lastTime1 = time;

			lastDistance = distance;

			time += timestep;
			step = step(timestep, difference, velocity, maxVelocity,
					allowDeceleration, step, temp);
			distance = difference.distance();

			if (distance > lastDistance && distance < initialDistance)
			{
				timestep /= 2d;

				time = lastTime2;
				difference.set(lastDifference2);
				velocity.set(lastVelocity2);
			}
		}
		return time;
	}

	/**
	 * @param deltaTime
	 *            Time passed
	 * @param position
	 *            Current object position
	 * @param destination
	 *            Final object position
	 * @param velocity
	 *            Velocity of the object (changed by this function)
	 * @param maxVelocity
	 *            Maximum velocity of the object (
	 *            <code>velocity.distance()</code> is limited)
	 * @param allowDeceleration
	 *            Allow the object to slow down
	 * @return Amount to move the object for <code>deltaTime</code>
	 */
	public V spring(double deltaTime, V position, V destination, V velocity,
			double maxVelocity, boolean allowDeceleration)
	{
		maxVelocity = Math.abs(maxVelocity);

		V difference = destination.subtract(position);
		V result = difference.createNew();
		V step = difference.createNew();
		V temp = difference.createNew();

		int steps = (int) (deltaTime / timestep);
		for (int i = 0; i < steps; i++)
		{
			step = step(timestep, difference, velocity, maxVelocity,
					allowDeceleration, step, temp);
			result.addLocal(step);
		}
		step = step(deltaTime - steps * timestep, difference, velocity,
				maxVelocity, allowDeceleration, step, temp);
		result.addLocal(step);

		return result;
	}

	private V step(double deltaTime, V destination, V velocity,
			double maxVelocity, boolean allowDeceleration, V store, V temp)
	{
		store.set(destination);
		store.multLocal(-springK);
		store.subtractLocal(velocity.mult(dampingK, temp));
		store.multLocal(deltaTime);

		//temp = new velocity
		velocity.add(store, temp);
		double currentVelocity = velocity.distance();
		double newVelocity = temp.distance();

		//disable deceleration if deceleration is not allowed
		if (newVelocity < currentVelocity && !allowDeceleration) //decelerating
		{
			if (currentVelocity < maxVelocity)
			{
				//remove any deceleration
				temp.set(velocity);
			}
			else if (currentVelocity > maxVelocity && newVelocity < maxVelocity)
			{
				//set the velocity to the max velocity
				temp.set(velocity);
				temp.multLocal(maxVelocity / currentVelocity);
			}
		}
		else if (newVelocity > currentVelocity) //accelerating
		{
			if (newVelocity > maxVelocity)
			{
				//TODO decelerate instead of jump
				temp.multLocal(maxVelocity / newVelocity);
			}
		}

		//set the new velocity, and move the destination
		velocity.set(temp);
		velocity.mult(-deltaTime, store);
		destination.subtractLocal(store);
		return store;
	}

	public double getSpringK()
	{
		return springK;
	}

	public void setSpringK(double springK)
	{
		this.springK = springK;
	}

	public double getDampingK()
	{
		return dampingK;
	}

	public void setDampingK(double dampingK)
	{
		this.dampingK = dampingK;
	}

	public void setDampingRatio(double dampingRatio)
	{
		double dampingK = dampingRatio * (2d * Math.sqrt(getSpringK()));
		setDampingK(dampingK);
	}

	public double getDampingRatio()
	{
		return getDampingK() / (2d * Math.sqrt(getSpringK()));
	}

	public double getTimestep()
	{
		return timestep;
	}

	public void setTimestep(double timestep)
	{
		this.timestep = timestep;
	}
}
