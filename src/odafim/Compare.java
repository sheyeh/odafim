package odafim;

import java.util.Arrays;

public class Compare {
    public boolean noChange;
    public boolean p1Gain;
    public boolean p2Gain;
    public boolean p1Loss;
    public boolean p2Loss;
    public boolean p1Change;
    public boolean p2Change;

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