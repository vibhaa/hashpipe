import java.util.*;

/* hash table simulation to track the unique flows experiencing loss
	using the D-Left hashing procedure where each flow id is hashed exactly
	d times to generate d locations where the flow and its loss might be
	stored* */

public class CountMinWithCache{
	private int tableSize;
	private int droppedPacketInfoCount;
	private int cumDroppedPacketInfoCount;
	private int totalNumberOfPackets;

	private Sketch countMinSketch;
	private FlowWithCount[] cache;
	private int cacheSize;
	private ArrayList<Long> heavyhitterList;

	private SummaryStructureType type;
	private final int numHashFunctions;

	private final double threshold1;
	private final double threshold2;

	public CountMinWithCache(int totalMemory, SummaryStructureType type, int numberOfFlows, int D, int cacheSize, double threshold1, double threshold2){
		this.tableSize = tableSize;
		this.numHashFunctions = D;
		droppedPacketInfoCount = 0;
		cumDroppedPacketInfoCount = 0;
		totalNumberOfPackets = 0;

		this.type = type;
		this.cacheSize = cacheSize;

		this.threshold1 = threshold1;
		this.threshold2 = threshold2;

		if (true/*type == SummaryStructureType.EvictionWithCount*/){
			cache = new FlowWithCount[cacheSize];
		
			for (int j = 0; j < cacheSize; j++){
				cache[j] = new FlowWithCount(0, 0);
			}

			countMinSketch = new Sketch((totalMemory - cacheSize)/numHashFunctions, numHashFunctions, numberOfFlows);
		}

		heavyhitterList = new ArrayList<Long>();
	}

	public void processData(long key){
		processDataWithoutCountInTable(key);
	}

	public void processDataWithoutCountInTable(long key){
		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 5171;
		final int hashA[] = {  421, 149, 311, 701, 557, 1667, 773, 2017, 1783, 883, 307, 199, 2719, 2851, 1453};
		final int hashB[] = {  73, 109, 233, 31, 151, 3359, 643, 1103, 2927, 3061, 409, 3079, 2341, 179, 1213};

		totalNumberOfPackets++;

		// update the count-min sketch for this flowid
		// TODO: modify this to update and read minimum parallelly so that
		// there is no going back to previously updated stages involved
		countMinSketch.updateCount(key);

		if (countMinSketch.estimateCount(key) > threshold1 * totalNumberOfPackets){
			/* hash to find index in cache and update*/
			int curKeyIndex = (int) ((hashA[0]*key + hashB[0]) % P) % (cacheSize);

			cache[curKeyIndex].count++;

			if (cache[curKeyIndex].count > threshold2 *totalNumberOfPackets) {
				// report flow to the controllergi
				// keep "reported" bit so that you aren't reporting continuously
				heavyhitterList.add(key);
			}
		}
		
	}

	public int getDroppedPacketInfoCount(){
		return droppedPacketInfoCount;
	}

	public FlowWithCount[] getCache(){
		return cache;
	}

	public ArrayList<Long> getHeavyHitters(){
		return heavyhitterList;
	}

	public Sketch getSketch(){
		return countMinSketch;
	}	
}