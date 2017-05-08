package com.hungama.myplay.activity.data.dao.campaigns;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class WeightedBucket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4762993890464329809L;

	Placement[] placements;
	double totals[];
	double running_totals = 0;

	public WeightedBucket(Placement[] placements) {
		this.placements = placements;
		totals = new double[placements.length];

		for (int i = 0; i < placements.length; i++) {
			running_totals += placements[i].getWeight();
			totals[i] = running_totals;

		}

	}

	private int next() {
		double random = rand(running_totals);
		int index = Arrays.binarySearch(totals, random);

		// Used for bad java implementation...
		if (index < 0)
			index = Math.abs(index + 1);

		return index;
	}

	public Placement getPlacement() {
		return placements[next()];
	}

	public static double rand(double max) {

		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive

		return rand.nextDouble() * max;
	}

}
