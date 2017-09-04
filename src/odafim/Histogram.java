package odafim;

import java.util.HashMap;
import java.util.concurrent.atomic.LongAdder;

public class Histogram extends HashMap<Integer, LongAdder> {

    public LongAdder get(int i) {
    	return computeIfAbsent(i, t -> new LongAdder());
    }

    public void increment(int i) {
		get(i).increment();
	}

	public void add(int i, int j) {
		get(i).add(j);
	}
}
