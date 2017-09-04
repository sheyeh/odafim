package odafim;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {

    // Method 1: Compare the case when all agreements are in place to the case
    // when all agreements but the one under test are in place
    static Results results_method_1 = new Results();
    // Method 2: Compare the case when all agreements are cancelled to the case
    // when there is just one agreement which is the one under test.
    static Results results_method_2 = new Results();
    static int singleParty = 0;
    static int totalAgreements = 0;
    static int lossCounter = 0;
    static int gainCounter = 0;
    static int neutralCounter = 0;
    static int bothAffectedCounter = 0;
    static int allCancelNeutralCounter = 0;
    static int allCancelChangeCounter = 0;
    static int nullAgreements = 0; // agreement where at least one party did not pass
    static int doubleNullAgreements = 0; // both parties did not pass
    static int partyDidNotPassBecauseOfAgreement = 0;
    static int noEffectOnSigningPartiesButAffectsOthers = 0;
    static int totalPartiesPassThreshold = 0;
    static Histogram partiesPassThresholdHistogram = new Histogram();
    static Histogram foo = new Histogram();

    public static void loadFile(String fileName) throws Exception {
        try (FileInputStream input = new FileInputStream(fileName)) {
            loadStream(input);
        }
    }

    private static Map<Integer, String> townsMap = new HashMap<>();
    private static Map<Integer, Integer> votersMap = new HashMap<>();
    private static Map<Integer, Map<String, String>> partyNames = new HashMap<>();
    private static Map<Integer, Map<String, Integer>> partyVotes = new HashMap<>();
    private static Map<Integer, Map<String, Integer>> partySeats = new HashMap<>();
    private static Map<Integer, Map<String, String>> agreements = new HashMap<>();

    private static final String HITKASHRUYOT = "התקשרויות בין רשימות";
    private static final String YISHUVIM = "רשימת הישובים";
    private static final String MANDATIM = "רשימות ומועמדים";

    private static void loadStream(FileInputStream stream) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        NodeList rows = doc.getElementsByTagName("Row");
        for (int i = 0; i < rows.getLength(); i++) {
            Node row = rows.item(i);
            String rowType = row
                    .getParentNode() // Table
                    .getParentNode() // Worksheet
                    .getAttributes().getNamedItem("ss:Name").getTextContent();
            if (row.getAttributes().getNamedItem("ss:Height") != null) { // Header row
                continue;
            }
            NodeList cells = row.getChildNodes();
            switch (rowType) {
            case YISHUVIM: yishuv(cells);
                break;
            case HITKASHRUYOT: hitkashrut(cells);
                break;
            case MANDATIM: mandatim(cells);
            default:
            }
        }
    }

    /**
     * Print all towns.
     */
    private static void print() {
        townsMap.keySet().stream().forEach(Main::printTown);
    }

    /**
     * Print one town.
     * @param townId the id of the town to print
     */
    private static void printTown(int townId) {
        Map<String, Integer> seats = partySeats.get(townId);
        int totalSeats = seats.values().stream().mapToInt(Integer::valueOf).sum();
        System.out.println(townsMap.get(townId) + " [" + townId + "] : " + votersMap.get(townId) + " בז\"ב \\ " + totalSeats + " מושבים");
        for (String party : partyNames.get(townId).keySet()) {
            System.out.println(
                    "    " + partyNames.get(townId).get(party) + " (" + party + ") : "
                    + partyVotes.get(townId).get(party) + " \\ " + seats.get(party));
        }
        System.out.println(agreements.get(townId).isEmpty() ? "" : "    " + agreements.get(townId) + "\n");
        long nullAgreement = agreements.get(townId).entrySet().stream()
                .filter(e -> seats.get(e.getKey()) == 0 || seats.get(e.getValue()) == 0)
                .count();
        nullAgreements += nullAgreement;
        long doubleNullAgreement = agreements.get(townId).entrySet().stream()
                .filter(e -> seats.get(e.getKey()) == 0 && seats.get(e.getValue()) == 0)
                .count();
        doubleNullAgreements += doubleNullAgreement;
    }

    private static void yishuv(NodeList cells) {
        String townName = textContent(cells, 1);
        int townId = numContent(cells, 3);
        int townTotalVoters = numContent(cells, 11);

        townsMap.put(townId, townName);
        votersMap.put(townId, townTotalVoters);

        if (!partyNames.containsKey(townId)) {
            partyNames.put(townId, new HashMap<>());
            partyVotes.put(townId, new HashMap<>());
            partySeats.put(townId, new HashMap<>());
            agreements.put(townId,  new HashMap<>());
        }
    }

    private static void hitkashrut(NodeList cells) {
        int townId = numContent(cells, 3);
        String party1 = textContent(cells, 5);
        String party2 = textContent(cells, 9);
        agreements.get(townId).put(party1, party2);
    }

    private static void mandatim(NodeList cells) {
        int townId = numContent(cells, 3);
        String partyLetters = textContent(cells, 5);
        String partyName = textContent(cells, 7);
        int votes = numContent(cells, 9);
        int seats = numContent(cells, 11);

        Map<String, String> namesMap = partyNames.get(townId);
        namesMap.put(partyLetters, partyName);
        Map<String, Integer> votesMap = partyVotes.get(townId);
        votesMap.put(partyLetters, votes);
        Map<String, Integer> seatsMap = partySeats.get(townId);
        seatsMap.put(partyLetters, seats);
    }

    public static String textContent(NodeList nodes, int i) {
        return nodes.item(i).getFirstChild().getTextContent();
    }

    public static int numContent(NodeList nodes, int i) {
        return Integer.valueOf(textContent(nodes, i));
    }

    private static void process() {
        for (int townId : townsMap.keySet()) {
            System.out.println(townsMap.get(townId));
            System.out.print("תוצאות מדווחות ");
            System.out.println(partySeats.get(townId).values());
            int partiesPassThreshold = (int)partySeats.get(townId).values().stream().filter(i -> i > 0).count();
            partiesPassThresholdHistogram.increment(partiesPassThreshold);
            totalPartiesPassThreshold += partiesPassThreshold;
            if (partySeats.get(townId).size() == 1) {
                // Only one party competing
                singleParty++;
                continue;
            }
            Map<String, Integer> partyIndex = new HashMap<>();
            Set<String> parties = partyVotes.get(townId).keySet();
            String[] letters = parties.toArray(new String[]{});
            Integer[] votes = new Integer[parties.size()];
            Integer[] heskemim = new Integer[parties.size()];
            for (int i = 0; i < parties.size(); i++) {
                votes[i] = partyVotes.get(townId).get(letters[i]);
                partyIndex.put(letters[i], i);
                heskemim[i] = -1;
            }
            int heskemimOvrim = 0;
            int totalSeats = partySeats.get(townId).values().stream().mapToInt(Integer::valueOf).sum();
            for (Entry<String, String> pair : agreements.get(townId).entrySet()) {
                int i1 = partyIndex.get(pair.getKey());
                int i2 = partyIndex.get(pair.getValue());
                heskemim[i1] = i2;
                heskemim[i2] = i1;
                if (partySeats.get(townId).get(pair.getKey()) > 0 && partySeats.get(townId).get(pair.getValue()) > 0) {
                	heskemimOvrim += 2;
                }
            }
            foo.add(partiesPassThreshold, heskemimOvrim);
            System.out.println(heskemimOvrim);
            // Method 1: compare the case when all agreements in place to the case
            // when individual agreement is cancelled but all other agreements stay.
            // All agreements in place
            Integer[] real_election_seats = Odafim.mandates(votes, totalSeats, heskemim);
            System.out.print("תוצאות מחושבות ");
            System.out.println(Arrays.asList(real_election_seats));
//            System.out.println(Arrays.asList(real_election_seats) + " תוצאות מחושבות");
            // Cancel individual agreements
            if (agreements.get(townId).isEmpty()) {
                System.out.println("אין הסכמים");
            } else {
                for (Entry<String, String> pair : agreements.get(townId).entrySet()) {
                    totalAgreements++;
                    Integer[] heskemim2 = Arrays.copyOf(heskemim, heskemim.length);
                    System.out.print("בלי " + pair + " " + partyIndex.get(pair.getKey()) + "=" + partyIndex.get(pair.getValue()) + " ");
                    int i1 = partyIndex.get(pair.getKey());
                    int i2 = partyIndex.get(pair.getValue());
                    heskemim2[i1] = -1;
                    heskemim2[i2] = -1;
                    Integer[] seats2 = Odafim.mandates(votes, totalSeats, heskemim2);
                    System.out.println(Arrays.asList(seats2));
                    results_method_1.update(new Compare(seats2, real_election_seats, i1, i2),
                    		partiesPassThreshold);
                }
                // Enact only one agreement at a time
                Integer[] heskemim_no_agreements = new Integer[heskemim.length];
                System.out.print("כל ההסכמים בטלים ");
                Arrays.fill(heskemim_no_agreements, -1);
                Integer[] no_agreements_seats = Odafim.mandates(votes, totalSeats, heskemim_no_agreements);
                System.out.println(Arrays.asList(no_agreements_seats));
                if (Arrays.equals(real_election_seats, no_agreements_seats)) {
//                    System.out.println("ללא שינוי");
                    allCancelNeutralCounter++;
                } else {
                    allCancelChangeCounter++;
                }
                for (Entry<String, String> pair : agreements.get(townId).entrySet()) {
                    Integer[] heskemim4 = Arrays.copyOf(heskemim_no_agreements, heskemim_no_agreements.length);
                    System.out.print("רק " + pair + " " + partyIndex.get(pair.getKey()) + "=" + partyIndex.get(pair.getValue()) + " ");
                    int i1 = partyIndex.get(pair.getKey());
                    int i2 = partyIndex.get(pair.getValue());
                    heskemim4[i1] = i2;
                    heskemim4[i2] = i1;
                    Integer[] one_agreement_seats = Odafim.mandates(votes, totalSeats, heskemim4);
                    System.out.println(Arrays.asList(one_agreement_seats));
                    results_method_2.update(new Compare(no_agreements_seats, one_agreement_seats, i1, i2),
                    		partiesPassThreshold);
                }
            }
            System.out.println("_______________");
        }
    }

    /**
     * Summary report of all the runs.
     */
    private static void report() {
        System.out.println(townsMap.size() + " יישובים");
        System.out.println(partyVotes.values().stream()
            .map(Map::size)
            .collect(Collectors.summingInt(Integer::intValue)) + " רשימות");
        System.out.println("רשימות שעברו את הסף " + totalPartiesPassThreshold);
        partiesPassThresholdHistogram
            .forEach((k, v) -> System.out.println("    " + v + " ישןובים עם " + k + " רשימות שעברו את הסף"));
        System.out.println("יישובים עם רשימה אחת " + singleParty);
        agreements.values().stream()
            .map(Map::size)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .forEach((k, v) -> System.out.println("    " + v + " ישובים עם " + k + " הסכמים"));
        System.out.println(agreements.values().stream().filter(map -> !map.isEmpty()).count() + " יישובים עם הסכמים");
        System.out.println(agreements.values().stream().filter(map -> map.size() > 1).count() + " יישובים עם יותר מהסכם אחד");
        System.out.println(totalAgreements + " הסכמים");
        System.out.println("הסכמים שלפחות אחת המפלגות לא עברה " + nullAgreements);
        System.out.println("הסכמים ששתי המפלגות לא עברו " + doubleNullAgreements);
        System.out.println(totalAgreements - nullAgreements + " הסכמים ששתי המפלגות עברו את אחוז החסימה");
        System.out.println("התפלגות מספר ההסכמים ששתי המפלגות עברו כפונקציה של מספר רשימות שעברו את הסף");
        System.out.println(foo);
        System.out.println();
        System.out.println("ביטול כל ההסכמים לא גורם לשינוי " + allCancelNeutralCounter);
        System.out.println("ביטול כל ההסכמים גורם לשינוי " + allCancelChangeCounter);
        System.out.println("מפלגות שלא עברו בגלל שחתמו על הסכם " + partyDidNotPassBecauseOfAgreement);

        System.out.println();
        System.out.println("שיטה 1: השוואת תוצאות אמת לתוצאות בלי הסכם העודפים הנבדק");
        results_method_1.print();
        System.out.println("שיטה 2: השוואת התוצאות ללא הסכמי עודפים בכלל להסכם עודפים יחיד");
        results_method_2.print();
        System.out.println(Odafim.formula());
    }

    public static void main(String...args) {
        try {
            loadFile("/Users/shai/Dropbox/Public/הסכמי עודפים/נתוני הבחירות לרשויות המקומיות 2008.xml");
//            loadFile("/Users/shai/Dropbox/Public/הסכמי עודפים/נתוני הבחירות לרשויות המקומיות שלא במועד הכללי 2003-2008.xml");
            print();
            process();
            report();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //TODO: How many agreements with parties that did not pass the threshold
}
