import java.util.*;

public class SmartEvictionHashTableSimulation{
	public static void main(String[] args){
		int numberOfTrials = Integer.parseInt(args[0]);
		int numberOfFlows = Integer.parseInt(args[1]);
		int tableSize = Integer.parseInt(args[2]);
		double threshold = Double.parseDouble(args[3]);
		FlowWithCount[] buckets = new FlowWithCount[tableSize];
		int droppedPacketInfoCount = 0;
		int cumDroppedPacketInfoCount = 0;
		int totalNumberOfPackets = 0;
		int D = 2;

		// hardcoded values for the hash functions given that the number of flows doesn't exceed 850
		final int P = 1019;
		final int hashA[] = {421, 149};
		final int hashB[] = {73, 109};

		/*final int numberOfFlows[] = {200, 300, 400, 500, 600, 700, 800, 850};
		final int tableSize[] = {50, 100, 150, 200, 250, 300, 350, 400, 450, 500};
		final double threshold[] = {0.008, 0.006, 0.0035, 0.0025, 0.001, 0.0008, 0.0006, 0.00035, 0.00025, 0.0001};*/

		// create a set of lost packets which consists of i lost packets of flow i
		ArrayList<Integer> packets = new ArrayList<Integer>();
		// add i packets of  flowid i
		for (int i = 1; i <= numberOfFlows; i++)
			for (int j = 0; j < i; j++)
				packets.add(i);

		// ideally the big losers should be the highest flow ids until the loss falls below the threshold
		HashSet<Integer> expectedLossyFlows = new HashSet<Integer>();
		for (int i = numberOfFlows; i >= 0; i--){
			// we know that there are i lost packets of flow i
			if (i > (int) Math.floor(threshold*packets.size())) {
				expectedLossyFlows.add(i);
			}
		}
		System.out.println("expected lossy flow size" + expectedLossyFlows.size());

		// array that counts the number of ith packets lost across the trials
		int packetsInfoDroppedAtFlow[] = new int[numberOfFlows];
		double observedProbPacketsDroppedAtFlow[] = new double[numberOfFlows];
		double expectedProbPacketsDroppedAtFlow[] = new double[numberOfFlows];

		// initialize all the flow tracking buckets to flows with id 0 and count 0
		buckets = new FlowWithCount[tableSize];
		for (int j = 0; j < tableSize; j++){
			buckets[j] = new FlowWithCount(0, 0);
		}

		// Across many trials, find the total number of packets lost, track the flows they belong to in a dleft hash table
		// at the end of the hashing procedure, look through all the tracked flows and see which of them are the big losers
		// compare this against the expected big losers and see if there is a discrepancy between the answer as to what the
		// big losers are, if yes, find how much of the loss went unreported as a fraction of the total loss
		double cumErrorMargin = 0;
		int errorInBinaryAnswer = 0;
		for (int i = 0; i < numberOfTrials; i++){
			Collections.shuffle(packets);
			FlowWithCount.reset(buckets);
			droppedPacketInfoCount = 0;
			totalNumberOfPackets = 0; // needed for the denominator to compute the threshold for loss count

			// data plane operation - as lost packets flow, hash them using d-left hashing to track the lossy flows
			for (int j = 0; j < packets.size(); j++){
				totalNumberOfPackets++;

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
					packetsInfoDroppedAtFlow[packets.get(j) - 1]++;
					droppedPacketInfoCount++;
				}						
			}

			cumDroppedPacketInfoCount += droppedPacketInfoCount;

			/* print out the status of the hashtable - the buckets and the counts
			int nonzero = 0;
			for (int j = 0; j < tableSize; j++){
				if (buckets[j].flowid != 0){
					//System.out.println("index " + i + " has flow " + buckets[i].flowid + " with count " + buckets[i].count);
					nonzero++;
				}			
			}
			System.out.println("non-zero buckets " + nonzero + " lost flows " + lostPacketCount);

			System.out.println(lostPacketCount);*/

			// controller operation at regular intervals
			// go through all the entries in the hash table and check if any of them are above the total loss count
			HashSet<Integer> observedLossyFlows = new HashSet<Integer>();
			for (FlowWithCount f : buckets){
				if (f.count > threshold*totalNumberOfPackets)
					observedLossyFlows.add(f.flowid);
			}

			// compare observed and expected lossy flows and compute the probability of error
			int bigLoserPacketsLost = 0;
			int flag = 0;
			for (Integer flowid : expectedLossyFlows){
				if (!observedLossyFlows.contains(flowid)){
					if (flag != 1){
						errorInBinaryAnswer++; // if there is even one point of difference, there is an error in binary yes/no or what flows contributed to loss
						flag = 1;
					}
					bigLoserPacketsLost += flowid; // as many packets as the flowid have been lost from the information gathered
				}
			}
			double errorMargin = bigLoserPacketsLost/(double) totalNumberOfPackets;
			cumErrorMargin += errorMargin;
		}


		/* compare the probabilities of losing the ith packet against the recursive formula */
		/*for (int i = 0; i < numberOfPackets; i++){
			observedProbFlowLostAtIndex[i] = (double) flowsLostAtIndex[i]/numberOfTrials;
			System.out.println(observedProbFlowLostAtIndex[i]);
		}*/
		// chances of an error in binary answer
		System.out.print(errorInBinaryAnswer/(double) numberOfTrials + "," + cumErrorMargin/ numberOfTrials + ",");
		System.out.println(cumDroppedPacketInfoCount/(double) numberOfTrials + "," + tableSize);
	}
}