package odafim;

public class Results {
    public int lossCounter = 0;
    public int gainCounter = 0;
    public int neutralCounter = 0;
    public int bothAffectedCounter = 0;
    public int noEffectOnSigningPartiesButAffectsOthers = 0;
    static Histogram lossByNumParties = new Histogram();
    static Histogram gainByNumParties = new Histogram();

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
