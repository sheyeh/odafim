package odafim;

import java.util.HashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * A simple implementation of a histogram.
 *
 */
public class Histogram extends HashMap<Integer, LongAdder> {

	/**
	 * get the value of bin #i in the histogram
	 * @param i bin number
	 * @return the value in bin #i
	 */
    public LongAdder get(int i) {
    	return computeIfAbsent(i, t -> new LongAdder());
    }

    /**
     * Increment by 1 the value in bin #i.
     * @param i bin numb er to increment
     */
    public void increment(int i) {
		get(i).increment();
	}

    /**
     * Increment by v the value in bin #i.
     * @param i bin number to increment
     * @param v the value to add to bin #i
     */
	public void add(int i, int v) {
		get(i).add(v);
	}
}
