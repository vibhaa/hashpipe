import java.util.*;

/* high level procedure that uses packet info from a csv file, parses it,
   induces loss on it, and produces a sketch for the lost packet on which
   the big loser identification process is performed 

   initially written for the sketches to all be reversible so that the 
   reversibility procedure would identify the lossy buckets - unused 
   code in the context of the hash table approach*/
public class LossyFlowIdentifier{
	private static HashSet<Long> expectedLossyFlows;
	/*public static PriorityQueue<LossyFlow> HeapOfLossyFlows; 
	private class BucketMatrixIndex{
		int hashfunctionIndex;
		int bucketIndex;

		public BucketMatrixIndex(int i, int j){
			hashfunctionIndex = i;
			bucketIndex = j;
		}
	}

	public static ArrayList<BucketMatrixIndex> identifyLossyBuckets(FlowHashTable sketch, double threshold){
		ArrayList<BucketMatrixIndex> lossyBuckets = new ArrayList<BucketMatrixIndex>();

		// process the sketch and identify the flows with high loss
		for (int i = 0; i < sketch.getNumberOfHashFunctions(); i++){
			for (int j = 0; j < sketch.getSize(); j++){
				if (sketch.hashMatrix[i][j] > threshold*sketch.totalNumberOfPackets)
					lossyBuckets.add(new BucketMatrixIndex(i, j));
			}
		}
	}

	public static ArrayList<long> identifyFlowsFromBuckets(ArrayList<BucketMatrixIndex> lossyBuckets){
		for (matrixindex : lossyBuckets){
			// break the bucketindex into its words
		}

	}*/

	public static void runLossIdentificationTrials(SummaryStructureType type, Sketch originalPacketSketch, ArrayList<Packet> lostPacketStream, double threshold, int tableSize){
		int numberOfTrials = 1000;
		int cumDroppedPacketInfoCount = 0;
		int numberOfFalsePositives = 0;
		int numberOfFalseNegatives = 0;

		for (int t = 0; t < numberOfTrials; t++){
			
			// track the unique lost flows
			DLeftHashTable lostFlowHashTable = new DLeftHashTable(tableSize, type, lostPacketStream.size());
			for (Packet p : lostPacketStream){
				//lostPacketSketch.updateCountInSketch(p);
				//System.out.println(p.getSrcIp());
				lostFlowHashTable.processData(p.getSrcIp());
			}

			cumDroppedPacketInfoCount += lostFlowHashTable.getDroppedPacketInfoCount();

			HashSet<Long> observedLossyFlows = new HashSet<Long>();
			if (type == SummaryStructureType.EvictionWithoutCount){
				Sketch lossEstimateSketch = lostFlowHashTable.getSketch();
				for (Long f : lostFlowHashTable.getFlowIdBuckets()){
					if (lossEstimateSketch.estimateLossCount(f) > threshold*lostPacketStream.size()){
						observedLossyFlows.add(f);
					//System.out.println(f.flowid);
					}
				}
			}
			else {
				for (FlowWithCount f : lostFlowHashTable.getBuckets()){
					if (f.count > threshold*lostPacketStream.size()){
						observedLossyFlows.add(f.flowid);
					//System.out.println(f.flowid);
					}
				}
			}


			int bigLoserPacketsLost = 0;
			int flag = 0;

			for (long flowid : expectedLossyFlows){
				if (!observedLossyFlows.contains(flowid)){
					numberOfFalseNegatives++;
				}
			}

			for (long flowid : observedLossyFlows){
				//System.out.println(FlowDataParser.convertLongToAddress(flowid));
				if (!expectedLossyFlows.contains(flowid)){
					numberOfFalsePositives++;
				}
			}
		}

		System.out.println(threshold + "," + (double) numberOfFalsePositives/numberOfTrials + "," + (double) numberOfFalseNegatives/numberOfTrials);

	}
	public static void main(String[] args){
		// constants for this LossyFlowIdentifier
		//final int K = 5;			// number of buckets in each hash table
		//final int N = 15;			// total number of unique keys
		//final int H = 4; 			// number of hash functions

		double threshold = 0.000001;
		// pass the filename from which the packet data needs to be parsed
		ArrayList<Packet> originalPacketStream = FlowDataParser.parsePacketData(args[0]);

		// read the flow to be lost from the command line and create a new stream with that flow lost
		String[] flowsToBeLost = new String[args.length - 1];
		for (int i = 1; i < args.length; i++)
			flowsToBeLost[i - 1] = args[i];

		// compare observed and expected lossy flows and compute the probability of error
		expectedLossyFlows = new HashSet<Long>();
		for (int i = 0; i < flowsToBeLost.length; i++){
			expectedLossyFlows.add(FlowDataParser.convertAddressToLong(flowsToBeLost[i]));
		}

		HashSet<Packet> finalPacketStream = LossInducer.createSingleLossyFlow(originalPacketStream, flowsToBeLost);

		// perform the tracking of orginal packets at the entry point so that we can find the perflow size
		Sketch originalPacketSketch = new Sketch(500, 3, originalPacketStream.size());
		for (Packet p : originalPacketStream){
			originalPacketSketch.updateCountInSketch(p);
		}

		// collect stream data at the observation point where the statistics are collected for the link
		ArrayList<Packet> lostPacketStream = Packet.computeDiff(originalPacketStream, finalPacketStream);
		//Sketch lostPacketSketch = new Sketch(K, H, lostPacketStream.size());
		
		
		runLossIdentificationTrials(SummaryStructureType.DLeft, originalPacketSketch, lostPacketStream, 0.0001, 100);
		runLossIdentificationTrials(SummaryStructureType.BasicHeuristic, originalPacketSketch, lostPacketStream, 0.0001, 100);
		runLossIdentificationTrials(SummaryStructureType.EvictionWithoutCount, originalPacketSketch, lostPacketStream, 0.0001, 100);
		runLossIdentificationTrials(SummaryStructureType.EvictionWithCount, originalPacketSketch, lostPacketStream, 0.0001, 100);
	}
	
}