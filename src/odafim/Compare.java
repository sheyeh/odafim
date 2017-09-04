package odafim;

import java.util.Arrays;

/**
 * Comparison between two parties and two sets of results.
 *
 */
public class Compare {
	// true of the two allocations are identical
    public boolean noChange;
    // true if party p1 gains a seat in allocation #2 vs #1
    public boolean p1Gain;
    // true if party p2 gains a seat in allocation #2 vs #1
    public boolean p2Gain;
    // true if party p1 loses a seat in allocation #2 vs #1
    public boolean p1Loss;
    // true if party p2 loses a seat in allocation #2 vs #1
    public boolean p2Loss;
    // true if party p1 gains or loses a seat in allocation #2 vs #1
    public boolean p1Change;
    // true if party p2 gains or loses a seat in allocation #2 vs #1
    public boolean p2Change;

    /**
     * Comparison between two parties and two sets of results.
     * @param seats1 allocation  #1 of seats in the counsel
     * @param seats2 allocation {@link #p2Change} of seats in the counsel
     * @param p1 index of party #1
     * @param p2 index of party #2
     */
    public Compare(Integer[] seats1, Integer[] seats2, int p1, int p2) {
        noChange = Arrays.equals(seats1, seats2);
        p1Gain = seats1[p1] < seats2[p1];
        p2Gain = seats1[p2] < seats2[p2];
        p1Loss = seats1[p1] > seats2[p1];
        p2Loss = seats1[p2] > seats2[p2];
        p1Change = p1Gain || p1Loss;
        p2Change = p2Gain || p2Loss;
//        if (noChange) System.out.println("ללא שינוי");
        if (p1Gain) System.out.println(p1 + " מרוויח ");
        if (p1Loss) System.out.println(p1 + " מפסיד ");
        if (p2Gain) System.out.println(p2 + " מרוויח ");
        if (p2Loss) System.out.println(p2 + " מפסיד ");
        if (p1Gain && p2Gain) System.out.println("דאבל");
    }
}