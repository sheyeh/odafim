package odafim;

/**
 * Summary of results of one comparison between two runs for the same town.
 *
 */
public class Results {
	// How many parties lost due to heskem odafim they were part of
    public int lossCounter = 0;
    // How many parties gained due to heskem odafim they were part of
    public int gainCounter = 0;
    // How many parties did not lose and did not gain.
    public int neutralCounter = 0;
    // Number of heskemim where both sides were affected (either lost or gained)
    public int bothAffectedCounter = 0;
    // Number of heskemim that affected parties that were not part of the heskem
    public int noEffectOnSigningPartiesButAffectsOthers = 0;
    // Histogram of number of parties that lost from heskem by number of parties in town
    static Histogram lossByNumParties = new Histogram();
    // Histogram of number of parties that gained from heskem by number of parties in town
    static Histogram gainByNumParties = new Histogram();

    /**
     * Update the histogram with the results from a {@link Compare} instance
     * @param c the comparison of two seat allocations for two parties in town
     * @param numParties number of parties in town
     */
    public void update(Compare c, int numParties) {
		gainCounter += val(c.p1Gain || c.p2Gain);
		lossCounter += val(c.p1Loss || c.p2Loss);
		neutralCounter += val(c.noChange);
		bothAffectedCounter += val(c.p1Change && c.p2Change);
		noEffectOnSigningPartiesButAffectsOthers += val(!c.noChange && !c.p1Change && !c.p2Change);
        if (c.p1Loss || c.p2Loss) {
        	lossByNumParties.increment(numParties);
        }
        if (c.p1Gain || c.p2Gain) {
        	gainByNumParties.increment(numParties);
        }
    }

	private int val(boolean b) {
		return b ? 1 : 0;
	}

	/**
	 * Print the aggregated results.
	 */
	public void print() {
        System.out.println("הסכמים שלא השפיעו על החותמות אבל השפיעו על רשימות אחרות " + noEffectOnSigningPartiesButAffectsOthers);
        System.out.println("הסכמים שלא שינו את התוצאות " + neutralCounter);
        System.out.println("הסכמים שהביאו לרווח " + gainCounter);
        System.out.println("הסכמים שגרמו להפסד " + lossCounter);
        System.out.println("השפעה כפולה " + bothAffectedCounter);
        System.out.println("התפלגות רווח מנדט לפי מספר רשימות שעברו את הסף");
        System.out.println(gainByNumParties);
        System.out.println("התפלגות הפסד מנדט לפי מספר רשימות שעברו את הסף");
        System.out.println(lossByNumParties);
        System.out.println();
    }
}
