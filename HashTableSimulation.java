import java.util.*;

public class HashTableSimulation{
	public static void main(String[] args){
		int numberOfTrials = Integer.parseInt(args[0]);
		int numberOfPackets = Integer.parseInt(args[1]);
		int tableSize = Integer.parseInt(args[2]);
		int[] buckets = new int[tableSize];
		int lostFlowCount = 0;

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
				int index = (int) (Math.random()*tableSize);
				if (buckets[index] != 0) {
					flowsLostAtIndex[j]++;
					lostFlowCount++;
				}
				buckets[index] = flows.get(j);			
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