import java.util.*;

public class EvictingHashTableSimulation{
	public static void main(String[] args){
		int numberOfTrials = Integer.parseInt(args[0]);
		int numberOfFlows = Integer.parseInt(args[1]);
		int tableSize = Integer.parseInt(args[2]);
		FlowWithCount[] buckets = new FlowWithCount[tableSize];
		int lostPacketCount = 0;
		int D = 2;

		/*Sketch that maintains the loss of each flow*/
		Sketch countMinSketch = new Sketch(100, 3, numberOfFlows);

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

			// reset counters
			countMinSketch.reset();
			// each packets comes in as a lost packet, put it in the count min sketch and also the hash table
			for (int j = 0; j < packets.size(); j++){
				// update the count-min sketch for this flowid
				countMinSketch.updateCount(packets.get(j));

				/* uniform hashing into a chunk N/d and then dependent picking of the choice*/
				int index = 0;
				int k = 0;
				for (k = 0; k < D; k++){
					index = ((hashA[k]*packets.get(j) + hashB[k]) % P) % (tableSize/D) + (k*tableSize/D);
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

				// none of the D locations were free - hash collission
				// decide if one of them is worth evicting

				// TODO: figure out if the incoming flow has a higher loss than one of the existing flows in the table
				// find a way of tracking the information of the incoming flow because it isnt the hash table
				// so we don't have information on what its loss count is nd the very first time it comes in, loss is 0
				if (k == D) {
					if (countMinSketch.estimateLossCount(buckets[index].flowid) < countMinSketch.estimateLossCount(packets.get(j))){
						flowsLostAtIndex[packets.get(j) - 1] = 0;
						flowsLostAtIndex[buckets[index].flowid] = buckets[index].count;
						lostPacketCount = lostPacketCount + buckets[index].count - (int) countMinSketch.estimateLossCount(packets.get(j));
					}
					else{
						flowsLostAtIndex[packets.get(j) - 1]++;
						lostPacketCount++;
					}
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
			//System.out.println("non-zero buckets " + nonzero + " lost flows " + lostPacketCount);
			//System.out.println(lostPacketCount);
		}

		/* compare the probabilities of losing the ith packet against the recursive formula */
		/*for (int i = 0; i < numberOfPackets; i++){
			observedProbFlowLostAtIndex[i] = (double) flowsLostAtIndex[i]/numberOfTrials;
			System.out.println(observedProbFlowLostAtIndex[i]);
		}*/
		System.out.println(lostPacketCount/(double) numberOfTrials);

		for (int i = 1; i <= numberOfFlows; i++){
			System.out.println(countMinSketch.estimateLossCount(i));
		}

		/*long[][] matrix = countMinSketch.getMatrix();
		for (int i = 0; i < matrix.length; i++){
			for (int j = 0; j < countMinSketch.getSize(); j++)
				System.out.print(matrix[i][j] + " ");
			System.out.println();
		}*/
	}
}