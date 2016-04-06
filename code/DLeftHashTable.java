import java.util.*;

/* hash table simulation to track the unique flows experiencing loss
	using the D-Left hashing procedure where each flow id is hashed exactly
	d times to generate d locations where the flow and its loss might be
	stored* */

public class DLeftHashTable{
	private int tableSize;
	private int droppedPacketInfoCount;
	private int cumDroppedPacketInfoCount;
	private int totalNumberOfPackets;

	private Sketch countMinSketch;
	private FlowWithCount[] buckets;
	private long[] flowIdBuckets;
	private HashSet<Long> bloomfilter;

	private SummaryStructureType type;
	private final int D;

	public DLeftHashTable(int tableSize, SummaryStructureType type, int numberOfFlows){
		this.tableSize = tableSize;

		droppedPacketInfoCount = 0;
		cumDroppedPacketInfoCount = 0;
		totalNumberOfPackets = 0;
		D = 4;

		this.type = type;

		if (type == SummaryStructureType.DLeft || type == SummaryStructureType.BasicHeuristic || type == SummaryStructureType.MinReplacementHeuristic){
			buckets = new FlowWithCount[tableSize];
		
			for (int j = 0; j < tableSize; j++){
				buckets[j] = new FlowWithCount(0, 0);
			}
		}
		else if (type == SummaryStructureType.EvictionWithCount){
			buckets = new FlowWithCount[tableSize];
		
			for (int j = 0; j < tableSize; j++){
				buckets[j] = new FlowWithCount(0, 0);
			}

			countMinSketch = new Sketch(tableSize/3, 3, numberOfFlows);
		}
		else if (type == SummaryStructureType.RollingMinWithBloomFilter || type == SummaryStructureType.RollingMinWihoutCoalescense){
			buckets = new FlowWithCount[tableSize];

			for (int j = 0; j < tableSize; j++){
				buckets[j] = new FlowWithCount(0,0);
			}

			//bloom filter
			bloomfilter = new HashSet<Long>();
		}
		else {
			flowIdBuckets = new long[tableSize];
		
			for (int j = 0; j < tableSize; j++){
				flowIdBuckets[j] = 0;
			}

			countMinSketch = new Sketch(tableSize/3, 3, numberOfFlows);
		}
	}

	public void processData(long key, int keynum){
		if (type == SummaryStructureType.EvictionWithoutCount)
			processDataWithoutCountInTable(key);
		else
			processDataWithCountInTable(key, keynum);
	}

	public void processDataWithoutCountInTable(long key){
		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 1019;
		final int hashA[] = {421, 149, 311, 701};
		final int hashB[] = {73, 109, 233, 31};

		totalNumberOfPackets++;

		// update the count-min sketch for this flowid
		countMinSketch.updateCount(key);

		/* uniform hashing into a chunk N/d and then dependent picking of the choice*/
		int k = 0;

		// keep track of which of the d locations has the minimum lost packet count
		// use this location to place the incoming flow if there is a collision
		int minIndex = 0;
		int minValue = -1;

		for (k = 0; k < D; k++){
			int index = (int) ((hashA[k]*key + hashB[k]) % P) % (tableSize/D) + (k*tableSize/D);
								
			// this flow has been seen before
			if (flowIdBuckets[index] == key) {
				break;
			}

			// new flow
			if (flowIdBuckets[index] == 0) {
				flowIdBuckets[index] = key;
				break;
			}

			// track min - first time explicitly set the value
			if (countMinSketch.estimateLossCount(flowIdBuckets[index]) < minValue || k == 0){
				minValue = (int) countMinSketch.estimateLossCount(flowIdBuckets[index]);
				minIndex = index;
			}
		}

		// TODO: figure out if the incoming flow has a higher loss than one of the existing flows in the table
		// find a way of tracking the information of the incoming flow because it isnt the hash table
		// so we don't have information on what its loss count is nd the very first time it comes in, loss is 0
		if (k == D) {
			//System.out.println("Min Index: " + minIndex + "minValue: " + minValue + "current id: " + packets.get(j) + "existing id: " + buckets[minIndex]);
			if (countMinSketch.estimateLossCount(flowIdBuckets[minIndex]) < countMinSketch.estimateLossCount(key)){
				//packetsInfoDroppedAtFlow[packets.get(j) - 1] = 0;
				//packetsInfoDroppedAtFlow[buckets[minIndex] - 1] = (int) countMinSketch.estimateLossCount(buckets[minIndex]);
				droppedPacketInfoCount = droppedPacketInfoCount + (int) countMinSketch.estimateLossCount(flowIdBuckets[minIndex]) - (int) countMinSketch.estimateLossCount(key);
				flowIdBuckets[minIndex] = key;
			}
			else{
				//packetsInfoDroppedAtFlow[packets.get(j) - 1]++;
				droppedPacketInfoCount++;
			}
		}
	}

	public void processDataWithCountInTable(long key, int keynum){
		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 1019;
		final int hashA[] = {421, 149, 311, 701};
		final int hashB[] = {73, 109, 233, 31};

		totalNumberOfPackets++;

		// keep track of which of the d locations has the minimum lost packet count
		// use this location to place the incoming flow if there is a collision
		int minIndex = 0;
		long minValue = -1;
		boolean isNew = false;
		long keyBeingCarried = key;
		long valueBeingCarried = 1;

		// update the count-min sketch for this flowid if the data type is so
		if (type == SummaryStructureType.EvictionWithCount)
			countMinSketch.updateCount(key);

		// Look up the bloomfilter for whether the key is expected
		if (type == SummaryStructureType.RollingMinWithBloomFilter){
			isNew = !(bloomfilter.contains(key));
		}

		/* uniform hashing into a chunk N/d and then dependent picking of the choice*/
		int k = 0;
		int firstLocation = 0; // how to track this in hardware

		if (key == 0)
		{
			System.out.print("invalid Key");
			return;
		}

		for (k = 0; k < D; k++){
			int index = (int) ((hashA[k]*keyBeingCarried + hashB[k]) % P) % (tableSize/D) + (k*tableSize/D);
			int curKeyIndex = (int) ((hashA[k]*key + hashB[k]) % P) % (tableSize/D) + (k*tableSize/D);
			if (k == 0) firstLocation = index;
			
			if (type != SummaryStructureType.RollingMinWithBloomFilter && type != SummaryStructureType.RollingMinWihoutCoalescense){
				// this flow has been seen before
				if (buckets[index].flowid == key) {
					buckets[index].count++;
					break;
				}

				// new flow
				if (buckets[index].flowid == 0) {
					buckets[index].flowid = key;
					buckets[index].count = 1;
					break;
				}

				// track min - first time explicitly set the value
				if (buckets[index].count < minValue || k == 0){
					minValue = buckets[index].count;
					minIndex = index;
				}
			}

			if (type == SummaryStructureType.RollingMinWithBloomFilter || type == SummaryStructureType.RollingMinWihoutCoalescense){
				//if (isNew){
					//zeroing out, or coalescing multipe occurences of the same flow
					/*if (keyBeingCarried == key && k != 0)
						System.out.println("Safety check failed");*/

					if (buckets[curKeyIndex].flowid == key && k != 0){
						if (buckets[firstLocation].flowid != key){
							System.out.println("causing inconsistency " + k);
						}

						

						// coalenscence case
						if (type != SummaryStructureType.RollingMinWihoutCoalescense){
							buckets[firstLocation].count += buckets[curKeyIndex].count; // handled by the next packet coz it goes back on table stages
							buckets[curKeyIndex].flowid = 0;
							buckets[curKeyIndex].count = 0;
						 	// how would this manifest in a multi stage table - can you break out
						}
						else{
							
							buckets[curKeyIndex].count++; // for this packet
							/*if (buckets[curKeyIndex].flowid == 1110210987)
								System.out.println("incrementing in table "+ k + " trial#" + keynum + " value" + buckets[curKeyIndex].count);*/
							// causes multiple occurences
						}
						if (buckets[curKeyIndex].flowid == 0 && buckets[curKeyIndex].count != 0){
							System.out.println("inconsistency case 5");
						}

						if (buckets[firstLocation].flowid == 0 && buckets[firstLocation].count != 0){
							System.out.println("firstLocation = " + firstLocation + " " + buckets[firstLocation].flowid + " " + buckets[firstLocation].count);
							System.out.println("inconsistency case 6");
						}
					}

					// new flow - this may have been zeroed out from the previous case, so idk how to handle that in hardware
					if (buckets[index].flowid == 0 && k != 0) {
						buckets[index].flowid = keyBeingCarried;
						buckets[index].count = valueBeingCarried;

						/*if (buckets[index].flowid == 1110210987)
								System.out.println("just storing trial #" + keynum + " value" + buckets[index].count);*/

						//bloomfilter.add(key);

						//clear out the current value being carried, but continue process to update on the key
						if (type == SummaryStructureType.RollingMinWihoutCoalescense){
							keyBeingCarried = 0;
							valueBeingCarried = 0;
						}
						else
							break;
					}
					else if (buckets[index].flowid == keyBeingCarried){ // retain the later value alone
						keyBeingCarried = 0;
						valueBeingCarried = 0;

						/*if (buckets[index].flowid == 1110210987)
								System.out.println("just ignoring trial #" + keynum + " value" + buckets[index].count);*/
					}

					if (buckets[index].flowid == key && k == 0){
						buckets[index].count++;

						/*if (buckets[index].flowid == 1110210987)
								System.out.println("incrementing in table 0 trial#" + keynum + " value" + buckets[index].count);*/
						
						// but continue process to update on the key that came in
						if (type != SummaryStructureType.RollingMinWihoutCoalescense)
							break; // how would this manifest in a multi stage table - can you break out
					}
					else if (k == 0){
						// place the new value here and carry the rest over
						valueBeingCarried = buckets[index].count;
						keyBeingCarried = buckets[index].flowid;

						/*if (buckets[index].flowid == 1110210987 && key != 1110210987)
								System.out.println("kicking out from first table trial#" + keynum + " value" + buckets[index].count);*/

						buckets[index].flowid = key;
						buckets[index].count = 1; // minValue + 1 insertion here ??

						if (buckets[index].flowid == 0 && buckets[index].count != 0){
							System.out.println("inconsistency case 3");
						}

						/*if (buckets[index].flowid == 1110210987)
								System.out.println("inserting in table1 trial #" + keynum + " value" + buckets[index].count);*/

						//System.out.println("index = " + index + " " + buckets[index].flowid + " " + buckets[index].count);
					}
					else if (buckets[index].count < valueBeingCarried){
					// swap out key being carried for value in this location
						/*if (buckets[index].flowid == 1110210987 && keyBeingCarried != 1110210987)
								System.out.println("kicking out from first table " + k + " trial#" + keynum + " value" + buckets[index].count);*/

						long temp = valueBeingCarried;
						valueBeingCarried = buckets[index].count;
						buckets[index].count = temp;

						temp = keyBeingCarried;
						keyBeingCarried = buckets[index].flowid;
						buckets[index].flowid = temp;

						/*if (buckets[index].flowid == 1110210987)
								System.out.println("inserting in table " + k + " trial #" + keynum + " value" + buckets[index].count);*/

						if (buckets[index].flowid == 0 && buckets[index].count != 0){
							System.out.println("inconsistency case 4");
						}
					}

					
					
				/*}
				else{
					// TODO: more cases here since false positives are possible
					if (buckets[index].flowid == key){
						buckets[index].count++;
					}
				}*/
				if (!bloomfilter.contains(key))
					bloomfilter.add(key);
			}
		}


		// none of the D locations were free
		if (k == D) {
			//packetsInfoDroppedAtFlow[packets.get(j) - 1]++;
			if (type == SummaryStructureType.DLeft)
				doNothing();
			else if (type == SummaryStructureType.BasicHeuristic)
				basicHeuristic(minIndex, key);
			else if (type == SummaryStructureType.MinReplacementHeuristic)
				minReplacementHeuristic(minIndex, key);
			else if (type == SummaryStructureType.EvictionWithCount)
				evictionByComparisonWithCount(minIndex, key);
			else droppedPacketInfoCount += valueBeingCarried;
		}
	}

	public int getDroppedPacketInfoCount(){
		return droppedPacketInfoCount;
	}

	public FlowWithCount[] getBuckets(){
		return buckets;
	}

	public long[] getFlowIdBuckets(){
		return flowIdBuckets;
	}

	public Sketch getSketch(){
		if (type == SummaryStructureType.EvictionWithCount || type == SummaryStructureType.EvictionWithoutCount)
			return countMinSketch;
		else
			return null;
	}

	public void basicHeuristic(int minIndex, long key){
		//packetsInfoDroppedAtFlow[buckets[minIndex].flowid - 1] = (int) buckets[minIndex].count;
		droppedPacketInfoCount = droppedPacketInfoCount + (int) buckets[minIndex].count;
		buckets[minIndex].flowid = key;
		buckets[minIndex].count = 1;
	}

	public void minReplacementHeuristic(int minIndex, long key){
		//packetsInfoDroppedAtFlow[buckets[minIndex].flowid - 1] = (int) buckets[minIndex].count;
		droppedPacketInfoCount = droppedPacketInfoCount + (int) buckets[minIndex].count;
		buckets[minIndex].flowid = key;
		buckets[minIndex].count += 1;
	}

	public void doNothing(){
		//packetsInfoDroppedAtFlow[buckets[minIndex].flowid - 1] = (int) buckets[minIndex].count;
		droppedPacketInfoCount++;
	}

	public void evictionByComparisonWithCount(int minIndex, long key){
		//packetsInfoDroppedAtFlow[buckets[minIndex].flowid - 1] = (int) buckets[minIndex].count;
		if (countMinSketch.estimateLossCount(buckets[minIndex].flowid) < countMinSketch.estimateLossCount(key)){
			//packetsInfoDroppedAtFlow[packets.get(j) - 1] = 0;
			//packetsInfoDroppedAtFlow[buckets[minIndex].flowid - 1] = (int) buckets[minIndex].count;
			droppedPacketInfoCount = droppedPacketInfoCount + (int) buckets[minIndex].count - (int) countMinSketch.estimateLossCount(key);
			buckets[minIndex].flowid = key;
			buckets[minIndex].count = (int) countMinSketch.estimateLossCount(key);
		}
		else{
			//packetsInfoDroppedAtFlow[packets.get(j) - 1]++;
			droppedPacketInfoCount++;
		}
	}
	
}