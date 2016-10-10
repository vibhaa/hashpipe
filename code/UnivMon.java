import java.util.*;

/*+----------------------------------------------------------------------
 ||
 ||  Class UnivMon
 ||
 ||         Author:  Vibhaa Sivaraman
 ||
 ||        Purpose:  To simulate the UnivMon example from the code
 ||
 ||  Inherits From:  None
 ||
 ||  Interfaces:  None
 ||
 |+-----------------------------------------------------------------------
 ||
 ||  Class Methods:  processData that does the sampleAndHold procedure 
 ||  on a given packet that belongs to a given new flow
 ||
 ++-----------------------------------------------------------------------*/

public class UnivMon{
	private int tableSize;
	private int droppedPacketInfoCount;
	private int cumDroppedPacketInfoCount;
	private int totalNumberOfPackets;
	private int numberOfSubstreams;
	private double threshold;

	private HashMap<Long, Long> flowMemory;
	private Sketch cSketches[];
	private HashMap<Long, Long> heavyhitterList;

	private SummaryStructureType type;

	public UnivMon(int totalMemory, SummaryStructureType type, int numberOfFlows, double threshold){
		this.tableSize = totalMemory;
		// keys take twice as much space as counters and 2 sets of counters

		droppedPacketInfoCount = 0;
		cumDroppedPacketInfoCount = 0;
		totalNumberOfPackets = 0;
		this.numberOfSubstreams = 15;
		this.threshold = threshold;

		this.type = type;
		
		flowMemory = new HashMap<Long, Long>();	
		/*for (int j = 0; j < tableSize; j++){
			flowMemory[j] = new FlowWithCount(0, 0);
		}*/

		cSketches = new Sketch[numberOfSubstreams];

		for (int i = 0; i < numberOfSubstreams; i++){
			cSketches[i] = new Sketch(totalMemory/numberOfSubstreams, 5, numberOfFlows);
		}

		heavyhitterList = new HashMap<Long, Long>();
	}

	public void processData(long key){
		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 5171;
		final int hashA[] = {  1783, 883, 307, 199, 2719, 2851, 1453, 421, 149, 311, 8461, 9839, 10597, 9241, 7027, 11329};
		final int hashB[] = {  73, 109, 233, 2927, 3061, 409, 3079, 2341, 179, 1213, 9871, 10159, 7577, 8147, 11113, 9901};

		totalNumberOfPackets++;

		for (int i = 0; i < numberOfSubstreams; i++){
			// sample, update count sketch, keep track of heavy hitters
			for (int j = 0; j < i; j++){
				int hash = (int) ((hashA[j]*key + hashB[j]) % P) % 2;
				if (hash != 1)
					return;
			}
			cSketches[i].updateCountInCountSketch(key);
			if (cSketches[i].estimateCountinCountSketch(key) > threshold)
				heavyhitterList.put(key, cSketches[i].estimateCountinCountSketch(key));
		}
	}

	public int getDroppedPacketInfoCount(){
		return droppedPacketInfoCount;
	}

	public HashMap<Long, Long> getBuckets(){
		return flowMemory;
	}

	public HashMap<Long, Long> getHeavyHitters(){
		return heavyhitterList;
	}

}