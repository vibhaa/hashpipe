import java.util.*;

/* eviction heuristic (compare the incoming flow's loss against that of
	the flow  with minimum loss amongst flows at all d locations) 
	added to the standard hash table simulation to track 
	the unique flows experiencing loss using the D-Left hashing procedure */

/* version of hash table simulation that uses the eviction heuristic
 but only stores the flow id in the hash table
 and uses the count-min sketch to estimate the loss count

 	runs experiments on a certain number of flows and for a given table size
	across multiple thresholds and averages out the results across a preset
	number of trials and reports the results in a csv format
*/

public class SmartEvictionHashTableWithoutCountSimulation{
	public static void main(String[] args){
		final int numberOfTrials = 1000;

		// print the header for the csv
		System.out.print("numberOfFlows" + ", " + "tableSize" + ", " + "threshold" + ",");
		System.out.println("errorInBinaryAnswer" + "," + "Error Margin" + ", Number of Dropped Packets" + ",");

		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 1019;
		final int hashA[] = {421, 149};
		final int hashB[] = {73, 109};

		final int numberOfFlows[] = {/*200, 300, */400/*, 500, 600, 700, 800, 850*/};
		final int tableSize[] = {50, 100, 150, 200, 250, 300, 350, 400, 450, 500};
		//final double threshold[] = {0.008, 0.006, 0.0035, 0.0025, 0.001, 0.0008, 0.0006, 0.00035, 0.00025, 0.0001};
		final double threshold[] = {0.0035, 0.003, 0.0025, 0.002, 0.0015, 0.001, 0.00095, 0.0009, 0.00085, 0.0008};

		for (int flowSize_index = 0; flowSize_index < numberOfFlows.length; flowSize_index++) {
			for (int tableSize_index = 0; tableSize_index < tableSize.length; tableSize_index++) {
				//for (int thr_index = flowSize_index; thr_index <= flowSize_index + 2; thr_index++) {
				for (int thr_index = 0; thr_index < threshold.length; thr_index++) {
					int[] buckets = new int[tableSize[tableSize_index]];
					int droppedPacketInfoCount = 0;
					int cumDroppedPacketInfoCount = 0;
					int totalNumberOfPackets = 0;
					int D = 2;


					/*Sketch that maintains the loss of each flow* --- CHANGE THIS SIZE TOO */
					Sketch countMinSketch = new Sketch((int) tableSize[tableSize_index]/3, 3, numberOfFlows[flowSize_index]);

					// create a set of lost packets which consists of i lost packets of flow i
					ArrayList<Integer> packets = new ArrayList<Integer>();
					// add i packets of  flowid i
					for (int i = 1; i <= numberOfFlows[flowSize_index]; i++)
						for (int j = 0; j < i; j++)
							packets.add(i);

					// ideally the big losers should be the highest flow ids until the loss falls below the threshold
					HashSet<Integer> expectedLossyFlows = new HashSet<Integer>();
					for (int i = numberOfFlows[flowSize_index]; i >= 0; i--){
						// we know that there are i lost packets of flow i
						if (i > (int) Math.floor(threshold[thr_index]*packets.size())) {
							expectedLossyFlows.add(i);
						}
					}
					//System.out.println("expected lossy flow size" + expectedLossyFlows.size());

					// array that counts the number of ith packets lost across the trials
					int packetsInfoDroppedAtFlow[] = new int[numberOfFlows[flowSize_index]];
					double observedProbPacketsDroppedAtFlow[] = new double[numberOfFlows[flowSize_index]];
					double expectedProbPacketsDroppedAtFlow[] = new double[numberOfFlows[flowSize_index]];

					// Across many trials, find the total number of packets lost, track the flows they belong to in a dleft hash table
					// at the end of the hashing procedure, look through all the tracked flows and see which of them are the big losers
					// compare this against the expected big losers and see if there is a discrepancy between the answer as to what the
					// big losers are, if yes, find how much of the loss went unreported as a fraction of the total loss
					double cumErrorMargin = 0;
					int errorInBinaryAnswer = 0;
					for (int i = 0; i < numberOfTrials; i++){
						//Collections.shuffle(packets);
						countMinSketch.reset();						
						
						//FlowWithCount.reset(buckets);
						// initialize all the flow tracking buckets to flows with id 0
						for (int t = 0; t < tableSize[tableSize_index]; t++){
							buckets[t] = 0;
						}

						droppedPacketInfoCount = 0;
						totalNumberOfPackets = 0; // needed for the denominator to compute the threshold for loss count

						// data plane operation - as lost packets flow, hash them using d-left hashing to track the lossy flows
						for (int j = 0; j < packets.size(); j++){
							totalNumberOfPackets++;

							// update the count-min sketch for this flowid
							countMinSketch.updateCount(packets.get(j));

							/* uniform hashing into a chunk N/d and then dependent picking of the choice*/
							int k = 0;

							// keep track of which of the d locations has the minimum lost packet count
							// use this location to place the incoming flow if there is a collision
							int minIndex = 0;
							int minValue = -1;

							for (k = 0; k < D; k++){
								int index = ((hashA[k]*packets.get(j) + hashB[k]) % P) % (tableSize[tableSize_index]/D) + (k*tableSize[tableSize_index]/D);
								//int index = (int) ((packets.get(j)%(tableSize/D)) *(tableSize/D) + k*tableSize/D);
								// this flow has been seen before
								if (buckets[index] == packets.get(j)) {
									break;
								}

								// new flow
								if (buckets[index] == 0) {
									buckets[index] = packets.get(j);
									break;
								}

								// track min - first time explicitly set the value
								if (countMinSketch.estimateLossCount(buckets[index]) < minValue || k == 0){
									minValue = (int) countMinSketch.estimateLossCount(buckets[index]);
									minIndex = index;
								}
							}

							// TODO: figure out if the incoming flow has a higher loss than one of the existing flows in the table
							// find a way of tracking the information of the incoming flow because it isnt the hash table
							// so we don't have information on what its loss count is nd the very first time it comes in, loss is 0
							if (k == D) {
								//System.out.println("Min Index: " + minIndex + "minValue: " + minValue + "current id: " + packets.get(j) + "existing id: " + buckets[minIndex]);
								if (countMinSketch.estimateLossCount(buckets[minIndex]) < countMinSketch.estimateLossCount(packets.get(j))){
									packetsInfoDroppedAtFlow[packets.get(j) - 1] = 0;
									packetsInfoDroppedAtFlow[buckets[minIndex] - 1] = (int) countMinSketch.estimateLossCount(buckets[minIndex]);
									droppedPacketInfoCount = droppedPacketInfoCount + (int) countMinSketch.estimateLossCount(buckets[minIndex]) - (int) countMinSketch.estimateLossCount(packets.get(j));
									buckets[minIndex] = packets.get(j);
								}
								else{
									packetsInfoDroppedAtFlow[packets.get(j) - 1]++;
									droppedPacketInfoCount++;
								}
							}						
						}

						cumDroppedPacketInfoCount += droppedPacketInfoCount;

						/* print out the status of the hashtable - the buckets and the counts
						int nonzero = 0;
						for (int j = 0; j < tableSize[tableSize_index]; j++){
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
						for (int f : buckets){
							if (countMinSketch.estimateLossCount(f) > threshold[thr_index]*totalNumberOfPackets){
								observedLossyFlows.add(f);
								//System.out.println(f);
							}
						}

						// compare observed and expected lossy flows and compute the probability of error
						int bigLoserPacketsLost = 0;
						int flag = 0;
						for (Integer flowid : expectedLossyFlows){
							if (!observedLossyFlows.contains(flowid)){
								if (flag != 1){
									// if there is even one point of difference, there is an error in binary yes/no or what flows contributed to loss
									errorInBinaryAnswer++; 
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
					System.out.print(numberOfFlows[flowSize_index] + ", " + tableSize[tableSize_index] + ", " + threshold[thr_index] + ",");
					System.out.print(errorInBinaryAnswer/(double) numberOfTrials + "," + cumErrorMargin/ numberOfTrials + ",");
					System.out.println(cumDroppedPacketInfoCount/(double) numberOfTrials + ",");
				}
			}
		}
	}
}