package odafim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Odafim {

	private static boolean use_bader_ofer = false;

	public static String formula() {
		return "\n" + "שיטת חישוב מנדטים : "
		    + (use_bader_ofer ? "באדר עופר" : "מוניציפאלי");
	}

	/**
	 * Eliminate parties that did not pass ahuz hasima
	 * @param votes array with number of votes for each party
	 * @param T total seats in the committee
	 * @param heskem heskemey odafim: heskem[i]=j means i&j have heskem odafim
	 */
	public static void preprocess(Integer[] votes, int T, Integer[] heskem) {
		int total = 0;
		for (int i = 0; i < votes.length; i++) {
			total += votes[i];
		}
		double moded = total / T * 0.75;
		for (int i = 0; i < votes.length; i++) {
			if (votes[i] < moded) {
				votes[i] = 0;
				if (heskem[i] != -1) {
					heskem[heskem[i]] = -1;
					heskem[i] = -1;
				}
			}
		}
	}

	/**
	 * Calculate number of mandatim
	 * @param votes array with number of votes for each party
	 * @param T total seats in the committee
	 * @param heskem heskemey odafim: heskem[i]=j means i&j have heskem odafim
	 * @return number of mandatim per party
	 */
	public static Integer[] mandates(Integer[] votes, int T, Integer[] heskem) {
		preprocess(votes, T, heskem);
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < votes.length; i++) {
			if (heskem[i] > i) {
				list.add(votes[i] + votes[heskem[i]]);
			} else if (heskem[i] < 0) {
				list.add(votes[i]);
			}
		}
		Integer[] votes0 = list.toArray(new Integer[]{});
		Integer[] mandates0 = use_bader_ofer ? baderofer(votes0, T) : municipal(votes0, T);
		int j=0;
		Integer[] mandates2 = new Integer[votes.length];
		for (int i = 0; i < heskem.length; i++) {
			if (heskem[i]>i) {
				Integer[] votes1 = new Integer[]{votes[i], votes[heskem[i]]};
				int T1 = mandates0[j];
				Integer[] mandates1 = use_bader_ofer ? baderofer(votes1, T1) : municipal(votes1, T1);
				mandates2[i] = mandates1[0];
				mandates2[heskem[i]] = mandates1[1];
				j++;
			} else if (heskem[i]<0) {
				mandates2[i] = mandates0[j];
				j++;
			}
		}
		
		return mandates2;
	}
	
	/**
	 * Calcualte number of manadatim without heskem odafim
	 * @param votes array with number of votes for each party or pair of parties
	 * @param T total seats in the committee
	 * @return number of mandatim per party
	 */
	static Integer[] municipal(Integer[] votes, int T) {
		int N = votes.length;
		int total = total(votes);
		int moded = d2i(Math.floor(total / T));

		// Round 1
		Integer[] mandates = new Integer[N];
		for (int i = 0; i < N; i++) {
			mandates[i] = d2i(Math.floor(votes[i] / moded));
		}
		// mandates left for round 2
		int spare = T - total(mandates);

		// Round 2
		Integer[] odafim = new Integer[N];
		for (int i = 0; i < N; i++) {
			odafim[i] = votes[i] - mandates[i] * moded;
		}
		Integer[] sorted = Arrays.copyOf(odafim, odafim.length);
		Arrays.sort(sorted);
		int M = N - 1; // index down the sorted array
		while (spare > 0) {
			int L = -1;
			for (int i = 0; i < N; i++) {
				if (odafim[i] == sorted[M]) {
					L = i;
					break;
				}
			}
			mandates[L]++;
			spare--;
			M--;
		}
		
		return mandates;
	}

	static Integer[] baderofer(Integer[] votes, int T) {
		int N = votes.length;
		int total = total(votes);
		int moded = d2i(Math.floor(total / T));

		// Round 1
		Integer[] mandates = new Integer[N];
		for (int i = 0; i < N; i++) {
			mandates[i] = d2i(Math.floor(votes[i] / moded));
		}
		// mandates left for round 2
		int spare = T - total(mandates);

		// Round 2
		while (spare > 0) {
			int L = -1;
			int maxPrice = 0;
			for (int i = 0; i < N; i++) {
				int price = d2i(votes[i] / (mandates[i] + 1));
				if (price > maxPrice) {
					maxPrice = price;
					L = i;
				}
			}
			mandates[L]++;
			spare--;
		}

		return mandates;
	}

	/**
	 * Convert double to int.
	 * @param d double
	 * @return int
	 */
	static int d2i(double d) {
		return Double.valueOf(d).intValue();
	}
	
	static int total(Integer[] a) {
		int t = 0;
		for (int i = 0; i < a.length; i++) t += a[i];
		return t;
	}

	private static void print(Integer[] mandates) {
		System.out.println(Arrays.asList(mandates));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Mevaseret 2008
		System.out.println("\nMevaseret");
		String[] reshimot2008 = new String[]{"zeirim","haziza","shas","shamam","galit","yoram"};
		Integer[] mevaseret2008 = new Integer[]{2,1,2,4,3,3};
		Integer[] votes2008 = new Integer[]{1529,729,1317,2177,1736,2222};
		Integer[] heskem2008 = new Integer[]{3,-1,-1,0,5,4};
		Integer[] heskem2008_2 = new Integer[]{-1,-1,-1,-1,5,4};
		Integer[] heskem2008_3 = new Integer[]{3,-1,-1,0,-1,-1};
		print(municipal(votes2008,15));
		print(mandates(votes2008,15,heskem2008));
		print(mandates(votes2008,15,heskem2008_2));
		print(mandates(votes2008,15,heskem2008_3));
		

		/*
		String[] reshimot_2013 = new String[]{"yoram","boaz","shas","tzeirim","tashtit","haziza","shamam","shteinitz"};
		Integer[] votes_2013 = new Integer[]{1809,1759,1725,1440,998,730,703,602};
		Integer[] odafim_2013 = new Integer[]{-1,3,-1,1,5,4,-1,-1};
		Integer[] odafim_2013_1 = new Integer[]{-1,-1,-1,-1,5,4,-1,-1};
		Integer[] odafim_2013_2 = new Integer[]{-1,3,-1,1,-1,-1,-1,-1};
		print(mandates(votes_2013,15));
		print(mandates(votes_2013,15,odafim_2013));
		print(mandates(votes_2013,15,odafim_2013_1));
		print(mandates(votes_2013,15,odafim_2013_2));
		*/

		System.out.println("Ashkelon");
		String[] ashkelon_parties = {
				"א", "אז", "אח", "ג", "ד", "דף", "ה", "זך",
				"חי", "ט", "טב", "ך", "כן", "ל", "ם", "מחל",
				"נץ", "סו", "ע", "עם", "ף", "רצ", "רק", "שס"
		};
		Integer[] ashkelon_votes = {
				3403, 867, 942, 2110, 5200, 2106, 1233, 2181,
				3605, 2454, 1216, 574, 3086, 3020, 975, 2126,
				1389, 719, 713, 1108, 1391, 619, 4551, 5000
		};
		Map<String, String> ashkelon_odafim_map = new HashMap<>();
		ashkelon_odafim_map.put("א", "ם");
		ashkelon_odafim_map.put("אז", "נץ");
		ashkelon_odafim_map.put("אח", "ל");
		ashkelon_odafim_map.put("דף", "ט");
		ashkelon_odafim_map.put("זך", "מחל");
		ashkelon_odafim_map.put("חי", "כן");
		ashkelon_odafim_map.put("ע", "סו");
		ashkelon_odafim_map.put("טב", "ף");
		ashkelon_odafim_map.put("ד", "רק");
		ashkelon_odafim_map.put("ג", "שס");
		Integer[] ashkelon_odafim = new Integer[ashkelon_votes.length];
		Arrays.fill(ashkelon_odafim,  -1);
		for (Entry<String, String> odef : ashkelon_odafim_map.entrySet()) {
			int i = find(ashkelon_parties, odef.getKey());
			int j = find(ashkelon_parties, odef.getValue());
			ashkelon_odafim[i] = j;
			ashkelon_odafim[j] = i;
		}
		int total_all = 0;
		for (int i = 0; i < ashkelon_votes.length; i++) {
			total_all += ashkelon_votes[i];
		}
		System.out.println(total_all);
		Integer[] results = mandates(ashkelon_votes, 23, ashkelon_odafim);
		int total = 0;
		for (int i = 0; i < results.length; i++) {
			total += results[i] > 0 ? ashkelon_votes[i] : 0;
		}
		System.out.println(total);
		System.out.println(d2i(total/23));
		System.out.print("הכל ");
		print(results);
		System.out.print("כלום ");
		print(municipal(ashkelon_votes, 23));
		for (int i = 0; i < ashkelon_odafim.length; i++) {
			if (ashkelon_odafim[i] >= i) {
				System.out.print("בלי " + ashkelon_parties[i] + "-" + ashkelon_parties[ashkelon_odafim[i]] + " ");
				Integer[] odafim = Arrays.copyOf(ashkelon_odafim, ashkelon_odafim.length);
				odafim[i] = -1;
				odafim[ashkelon_odafim[i]] = -1;
				print(mandates(ashkelon_votes, 23, odafim));
			}
		}
	}
	
	static int find(String[] array, String val) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(val)) {
				return i;
			}
		}
		return -1;
	}
}