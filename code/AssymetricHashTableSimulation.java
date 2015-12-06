import java.util.*;

public class AssymetricHashTableSimulation{
	public static void main(String[] args){
		int numberOfTrials = Integer.parseInt(args[0]);
		int numberOfFlows = Integer.parseInt(args[1]);
		int tableSize = Integer.parseInt(args[2]);
		FlowWithCount[] buckets = new FlowWithCount[tableSize];
		int lostPacketCount = 0;
		int D = 2;

		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 1019;
		final int hashA[] = {421, 149};
		final int hashB[] = {73, 109};

		// array that counts the number of ith packets lost across the trials
		int flowsLostAtIndex[] = new int[numberOfFlows];
		double observedProbFlowLostAtIndex[] = new double[numberOfFlows];
		double expectedProbFlowLostAtIndex[] = new double[numberOfFlows];

		// initialize all the flow tracking buckets to flows with id 0 and count 0
		buckets = new FlowWithCount[tableSize];
		for (int j = 0; j < tableSize; j++){
			buckets[j] = new FlowWithCount(0, 0);
		}

		List<Integer> packets = new ArrayList<>();
		// add i packets of  flowid i
		for (int i = 1; i <= numberOfFlows; i++)
			for (int j = 0; j < i; j++)
				packets.add(i);

		for (int i = 0; i < numberOfTrials; i++){
			Collections.shuffle(packets);
			//lostFlowCount = 0;
			for (int j = 0; j < packets.size(); j++){
				/* uniform hashing into a chunk N/d and then dependent picking of the choice*/
				int k = 0;
				for (k = 0; k < D; k++){
					int index = ((hashA[k]*packets.get(j) + hashB[k]) % P) % (tableSize/D) + (k*tableSize/D);
					//int index = (int) ((packets.get(j)%(tableSize/D)) *(tableSize/D) + k*tableSize/D);
					// this flow has been seen before
					if (buckets[index].flowid == packets.get(j)) {
						buckets[index].count++;
						break;
					}

					// new flow
					if (buckets[index].flowid == 0) {
						buckets[index].flowid = packets.get(j);
						buckets[index].count = 1;
						break;
					}
				}

				// none of the D locations were free
				if (k == D) {
					flowsLostAtIndex[packets.get(j) - 1]++;
					lostPacketCount++;
				}						
			}

			/* print out the status of the hashtable - the buckets and the counts*/
			int nonzero = 0;
			for (int j = 0; j < tableSize; j++){
				if (buckets[j].flowid != 0){
					//System.out.println("index " + i + " has flow " + buckets[i].flowid + " with count " + buckets[i].count);
					nonzero++;
				}			
			}
			System.out.println("non-zero buckets " + nonzero + " lost flows " + lostPacketCount);
			//System.out.println(lostPacketCount);
		}

		/* compare the probabilities of losing the ith packet against the recursive formula */
		/*for (int i = 0; i < numberOfPackets; i++){
			observedProbFlowLostAtIndex[i] = (double) flowsLostAtIndex[i]/numberOfTrials;
			System.out.println(observedProbFlowLostAtIndex[i]);
		}*/
		System.out.println(lostPacketCount/(double) numberOfTrials);
	}
}