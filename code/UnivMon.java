import java.util.*;

/*+----------------------------------------------------------------------
 ||
 ||  Class UnivMon
 ||
 ||         Author:  Vibhaa Sivaraman
 ||
 ||        Purpose:  To simulate the UnivMon algorithm
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
	private int k;

	private HashMap<Long, Long> flowMemory;
	private Sketch cSketches[];
	private ArrayList<HashMap<Long, Long>> heavyhitterList;

	private SummaryStructureType type;

	public UnivMon(int totalMemory, SummaryStructureType type, int numberOfPackets, int k){
		this.tableSize = totalMemory;
		// keys take twice as much space as counters and 2 sets of counters

		droppedPacketInfoCount = 0;
		cumDroppedPacketInfoCount = 0;
		totalNumberOfPackets = numberOfPackets;
		this.numberOfSubstreams = 15;
		this.k = k;

		this.type = type;
		
		flowMemory = new HashMap<Long, Long>();	
		/*for (int j = 0; j < tableSize; j++){
			flowMemory[j] = new FlowWithCount(0, 0);
		}*/

		cSketches = new Sketch[numberOfSubstreams + 1]; // extra zero level

		heavyhitterList = new ArrayList<HashMap<Long, Long>>(numberOfSubstreams + 1);
		for (int i = 0; i <= numberOfSubstreams; i++){
			//cSketches[i] = new Sketch(totalMemory/numberOfSubstreams/3, 3, numberOfPackets);
			cSketches[i] = new Sketch(1000, 5, numberOfPackets);
			heavyhitterList.add(new HashMap<Long, Long>());
		}

	}

	private void performOneUpdate(int i, long key){
		//cSketches[i].updateCountInCountSketch(key);
		cSketches[i].updateCountInSketch(key);
		long minKey = -1;
		long minCount = -1;
		boolean flag = false;
		for (Long k : heavyhitterList.get(i).keySet()){
			if (flag == false || heavyhitterList.get(i).get(k) < minCount){
				minCount = heavyhitterList.get(i).get(k);
				minKey = k;
				flag = true;
			}
		}

		HashMap<Long, Long> curList = heavyhitterList.get(i);
		/* count sketch version
		if (curList.size() == k && cSketches[i].estimateCountinCountSketch(key) > minCount){
			curList.put(key, cSketches[i].estimateCountinCountSketch(key));
			curList.remove(minKey);
		} 
		else if (curList.size() < k)
			curList.put(key, cSketches[i].estimateCountinCountSketch(key));*/


		if (curList.size() == k && cSketches[i].estimateCount(key) > minCount){
			curList.put(key, cSketches[i].estimateCount(key));
			curList.remove(minKey);
		} 
		else if (curList.size() < k)
			curList.put(key, cSketches[i].estimateCount(key));
		heavyhitterList.set(i, curList);
	}

	public void processData(long key){
		// hardcoded values for the hash functions given that the number of flows is 100
		final int P = 5171;
		final int hashA[] = {  1783, 883, 307, 199, 2719, 2851, 1453, 421, 149, 311, 8461, 9839, 10597, 9241, 7027, 11329};
		final int hashB[] = {  73, 109, 233, 2927, 3061, 409, 3079, 2341, 179, 1213, 9871, 10159, 7577, 8147, 11113, 9901};

		totalNumberOfPackets++;

		
		performOneUpdate(numberOfSubstreams, key);
		for (int i = 0; i < numberOfSubstreams; i++){
			// sample, update count sketch, keep track of heavy hitters
			for (int j = 0; j < i; j++){
				int hash = (int) ((hashA[j]*key + hashB[j]) % P) % 2;
				if (hash != 1)
					return;
			}
			performOneUpdate(i, key);
		}
	}

	public int getDroppedPacketInfoCount(){
		return droppedPacketInfoCount;
	}

	public HashMap<Long, Long> getBuckets(){
		return flowMemory;
	}

	public HashMap<Long, Long> getHeavyHitters(){
		HashMap<Long, Long> superList = new HashMap<Long, Long>();
		for (int i = 0; i <= numberOfSubstreams; i++){
			for (Long k : heavyhitterList.get(i).keySet())
				superList.put(k, heavyhitterList.get(i).get(k));
		}
		return superList;
	}

	public Sketch[] getSketches(){
		return cSketches;
	}

}