import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

/* high level procedure that uses packet info from a csv file, parses it,
   induces loss on it, and produces a sketch for the lost packet on which
   the big loser identification process is performed 

   initially written for the sketches to all be reversible so that the 
   reversibility procedure would identify the lossy buckets - unused 
   code in the context of the hash table approach*/
public class LossyFlowIdentifier{
	private static HashSet<Long> expectedLossyFlows;
	private static HashMap<Long, Integer> lostFlowSizes;
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
		long bigLoserPacketReported = 0;
		long bigLoserPacketCount = 0;

		double cumDeviation = 0;
		HashMap<Long, Long> observedLossyFlows = new HashMap<Long, Long>();
		for (int t = 0; t < numberOfTrials; t++){
			
			// track the unique lost flows
			DLeftHashTable lostFlowHashTable = new DLeftHashTable(tableSize, type, lostPacketStream.size());
			for (Packet p : lostPacketStream){
				//lostPacketSketch.updateCountInSketch(p);
				//System.out.println(p.getSrcIp());
				lostFlowHashTable.processData(p.getSrcIp());
			}

			cumDroppedPacketInfoCount += lostFlowHashTable.getDroppedPacketInfoCount();

			observedLossyFlows = new HashMap<Long, Long>();
			if (type == SummaryStructureType.EvictionWithoutCount){
				Sketch lossEstimateSketch = lostFlowHashTable.getSketch();
				for (Long f : lostFlowHashTable.getFlowIdBuckets()){
					if (f != 0 && lossEstimateSketch.estimateLossCount(f) > threshold*lostPacketStream.size()){
						observedLossyFlows.put(f, lossEstimateSketch.estimateLossCount(f));
					//System.out.println(f.flowid);
					}
				}
			}
			else {
				for (FlowWithCount f : lostFlowHashTable.getBuckets()){
					if (f.count > threshold*lostPacketStream.size()){
						observedLossyFlows.put(f.flowid, f.count);
					//System.out.println(f.flowid);
					}
				}
			}


			int bigLoserPacketsLost = 0;
			int flag = 0;
			double deviation = 0;
			double denominator = 0;

			for (long flowid : expectedLossyFlows){
				if (!observedLossyFlows.containsKey(flowid)){
					numberOfFalseNegatives++;
				}
				else
					bigLoserPacketReported += lostFlowSizes.get(flowid);
				bigLoserPacketCount += lostFlowSizes.get(flowid);
			}

			for (long flowid : observedLossyFlows.keySet()){
				//System.out.println("hello");
				//System.out.println(FlowDataParser.convertLongToAddress(flowid));
				if (!expectedLossyFlows.contains(flowid)){
					numberOfFalsePositives++;
				}
				if (lostFlowSizes.get(flowid) == null)
					System.out.println(FlowDataParser.convertLongToAddress(flowid));
				deviation += Math.abs(observedLossyFlows.get(flowid) - lostFlowSizes.get(flowid));
				denominator += lostFlowSizes.get(flowid);
			}

			cumDeviation += deviation/denominator;
		}

		System.out.print(tableSize + "," + threshold + ",");
		System.out.print((double) numberOfFalsePositives/numberOfTrials/observedLossyFlows.size() + ",");
		System.out.print((double) numberOfFalseNegatives/numberOfTrials/expectedLossyFlows.size() + ",");
		System.out.print(expectedLossyFlows.size() + "," + observedLossyFlows.size() + "," + (double) bigLoserPacketReported/bigLoserPacketCount);
		System.out.println("," + cumDeviation/numberOfTrials);

	}
	public static void main(String[] args){
		// constants for this LossyFlowIdentifier
		//final int K = 5;			// number of buckets in each hash table
		//final int N = 15;			// total number of unique keys
		//final int H = 4; 			// number of hash functions

		//double threshold = 0.000001;
		// pass the filename from which the packet data needs to be parsed
		ArrayList<Packet> originalPacketStream = FlowDataParser.parsePacketData(args[0]);

		// read the flows to be lost from a file mentioned in the command line and create a new stream with that flow lost
		ArrayList<String> flowsToBeLost = new ArrayList<String>();
		lostFlowSizes = new HashMap<Long, Integer>();
		int totalPacketsLost = 0;
		File file = new File(args[1]);
		try
		{
			Scanner scanner = new Scanner(file);
			String line;
			//int linenumber = 0;
			String[] fields = new String[24];
			while (scanner.hasNextLine())
			{
				line = scanner.nextLine();
				fields = line.split(",");
				flowsToBeLost.add(fields[0]);
				lostFlowSizes.put(FlowDataParser.convertAddressToLong(fields[0]), Integer.parseInt(fields[1]));
				totalPacketsLost+= Integer.parseInt(fields[1]);
			}
			scanner.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.format("Exception occurred trying to read '%s'.", args[1]);
			e.printStackTrace();
			return;
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
		
		final int tableSize[] = {50, 100, 150, 200, 250, 300, 350, 400, 450, 500};
		//final double threshold[] = {0.008, 0.006, 0.0035, 0.0025, 0.001, 0.0008, 0.0006, 0.00035, 0.00025, 0.0001};
		final double threshold[] = {/*0.0035, 0.003, */0.002/*, 0.001, 0.0009, 0.00075, 0.0006, 0.00045, 0.0003, 0.00015*/};

		System.out.println("tableSize" + "," + "threshold" + "," + "FalsePositive %" + "," + "False Negative %" + "," + "expected number, reported number, bigLoserReportedFraction, deviation");
		for (int tableSize_index = 0; tableSize_index < tableSize.length; tableSize_index++) { 
			for (int thr_index = 0; thr_index < threshold.length; thr_index++){

				// change the expected flows to be lost accordingly
				// compare observed and expected lossy flows and compute the probability of error
				expectedLossyFlows = new HashSet<Long>();
				for (String f : flowsToBeLost){
					if (lostFlowSizes.get(FlowDataParser.convertAddressToLong(f)) > (int) (threshold[thr_index] * totalPacketsLost)){
						expectedLossyFlows.add(FlowDataParser.convertAddressToLong(f));
					}
				}

				//System.out.println(expectedLossyFlows.size() + " " + totalPacketsLost);

			 	//System.out.println(totalPacketsLost + " " + count);
				//runLossIdentificationTrials(SummaryStructureType.DLeft, originalPacketSketch, lostPacketStream, threshold[thr_index], tableSize[tableSize_index]);
				//runLossIdentificationTrials(SummaryStructureType.BasicHeuristic, originalPacketSketch, lostPacketStream, threshold[thr_index], tableSize[tableSize_index]);
				runLossIdentificationTrials(SummaryStructureType.EvictionWithoutCount, originalPacketSketch, lostPacketStream, threshold[thr_index], tableSize[tableSize_index]);
				//runLossIdentificationTrials(SummaryStructureType.EvictionWithCount, originalPacketSketch, lostPacketStream, threshold[thr_index], tableSize[tableSize_index]);
			}
		}
	}
	
}