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

	private SummaryStructureType type;
	private final int D;

	public DLeftHashTable(int tableSize, SummaryStructureType type, int numberOfFlows){
		this.tableSize = tableSize;

		droppedPacketInfoCount = 0;
		cumDroppedPacketInfoCount = 0;
		totalNumberOfPackets = 0;
		D = 2;

		this.type = type;

		if (type == SummaryStructureType.DLeft || type == SummaryStructureType.BasicHeuristic){
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
		else {
			flowIdBuckets = new long[tableSize];
		
			for (int j = 0; j < tableSize; j++){
				flowIdBuckets[j] = 0;
			}

			countMinSketch = new Sketch(tableSize/3, 3, numberOfFlows);
		}
	}

	public void processData(long key){
		if (type == SummaryStructureType.EvictionWithoutCount)
			processDataWithoutCountInTable(key);
		else
			processDataWithCountInTable(key);
	}

	public void processDataWithoutCountInTable(long key){
		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 1019;
		final int hashA[] = {421, 149};
		final int hashB[] = {73, 109};

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

	public void processDataWithCountInTable(long key){
		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 1019;
		final int hashA[] = {421, 149};
		final int hashB[] = {73, 109};

		totalNumberOfPackets++;


		// keep track of which of the d locations has the minimum lost packet count
		// use this location to place the incoming flow if there is a collision
		int minIndex = 0;
		long minValue = -1;

		// update the count-min sketch for this flowid if the data type is so
		if (type == SummaryStructureType.EvictionWithCount)
			countMinSketch.updateCount(key);

		/* uniform hashing into a chunk N/d and then dependent picking of the choice*/
		int k = 0;
		for (k = 0; k < D; k++){
			int index = (int) ((hashA[k]*key + hashB[k]) % P) % (tableSize/D) + (k*tableSize/D);
			
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

		// none of the D locations were free
		if (k == D) {
			//packetsInfoDroppedAtFlow[packets.get(j) - 1]++;
			if (type == SummaryStructureType.DLeft)
				doNothing();
			else if (type == SummaryStructureType.BasicHeuristic)
				basicHeuristic(minIndex, key);
			else
				evictionByComparisonWithCount(minIndex, key);
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