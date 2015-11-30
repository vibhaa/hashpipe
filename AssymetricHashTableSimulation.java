import java.util.*;

public class AssymetricHashTableSimulation{
	public static void main(String[] args){
		int numberOfTrials = Integer.parseInt(args[0]);
		int numberOfPackets = Integer.parseInt(args[1]);
		int tableSize = Integer.parseInt(args[2]);
		int[] buckets = new int[tableSize];
		int lostFlowCount = 0;
		int D = 2;

		// array that counts the number of ith packets lost across the trials
		int flowsLostAtIndex[] = new int[numberOfPackets];
		double observedProbFlowLostAtIndex[] = new double[numberOfPackets];
		double expectedProbFlowLostAtIndex[] = new double[numberOfPackets];

		List<Integer> flows = new ArrayList<>();
		for (int i = 1; i <= numberOfPackets; i++)
			flows.add(i);

		for (int i = 0; i < numberOfTrials; i++){
			Arrays.fill(buckets, 0);
			Collections.shuffle(flows);
			lostFlowCount = 0;
			for (int j = 0; j < numberOfPackets; j++){

				/* uniform hashing into a location
				int index = (int) (Math.random()*tableSize);*/

				/* uniform hashing into a chunk N/d and then dependent picking of the choice*/
				int k = 0;
				for (k = 0; k < D; k++){
					int index = (int) (Math.random()*(tableSize/D) + k*tableSize/D);
					if (buckets[index] == 0) {
						buckets[index] = flows.get(j);
						break;
					}
				}

				// none of the D locations were free
				if (k == D) {
					flowsLostAtIndex[j]++;
					lostFlowCount++;
				}			
			}
			//System.out.println(lostFlowCount);
		}

		/* compare the probabilities of losing the ith packet against the recursive formula*/
		for (int i = 0; i < numberOfPackets; i++){
			observedProbFlowLostAtIndex[i] = (double) flowsLostAtIndex[i]/numberOfTrials;
			System.out.println(observedProbFlowLostAtIndex[i]);
		}
	}
}