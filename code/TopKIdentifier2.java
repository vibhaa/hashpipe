import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

/* high level procedure that uses packet info from a csv file, parses it,
   induces loss on it, and produces a sketch for the lost packet on which
   the big loser identification process is performed 

   initially written for the sketches to all be reversible so that the 
   reversibility procedure would identify the lossy buckets - unused 
   code in the context of the hash table approach*/
public class TopKIdentifier2{
	private static HashSet<Long> expectedHH;
	private static HashMap<Long, Integer> flowSizes;
	private static ArrayList<FlowWithCount> flowAggWithSizes;

	private static double accuracy = 0.99;

	public static void runTopKIdentificationTrials(SummaryStructureType type, ArrayList<Packet> inputStream, int[] k, int tableSize, int D){
		int numberOfTrials = 1000;
		int cumDroppedPacketInfoCount = 0;
		int observedSize[] = new int[k.length];
		int expectedSize[] = new int[k.length];
		int numberOfFalsePositives[] = new int[k.length];
		int numberOfFalseNegatives[] = new int[k.length];
		int missingFromTable[] = new int[k.length];
		long bigLoserPacketReported[] = new long[k.length];
		long bigLoserPacketCount[] = new long[k.length];
		float occupiedSlots[] = new float[k.length];
		float duplicates[] = new float[k.length];

		// track the unique lost flows
		DLeftHashTable lostFlowHashTable = null;
		GroupCounters gcHashTable = null;

		double cumDeviation[] = new double[k.length];
		HashMap<Long, Long> observedHH = new HashMap<Long, Long>();
		ArrayList<FlowWithCount> outputFlowsList = new ArrayList<FlowWithCount>();
		for (int t = 0; t < numberOfTrials; t++){

			// given input, so ideal order of heavy hitters
			FlowWithCount[] inputFlowArray = new FlowWithCount[flowAggWithSizes.size()];
			inputFlowArray = flowAggWithSizes.toArray(inputFlowArray);
			Arrays.sort(inputFlowArray);

			if (type == SummaryStructureType.GroupCounters)
				gcHashTable = new GroupCounters(tableSize, type,  inputStream.size(), D);
			else
				lostFlowHashTable = new DLeftHashTable(tableSize, type, inputStream.size(), D);

			Collections.shuffle(inputStream); // randomizing the order

			int count = 0;
			for (Packet p : inputStream){
				//lostPacketSketch.updateCountInSketch(p);
				//System.out.println(p.getSrcIp());
				if (type == SummaryStructureType.GroupCounters)
					gcHashTable.processData(p.getSrcIp());
				else
					lostFlowHashTable.processData(p.getSrcIp(), count++);
				
			}

			if (type == SummaryStructureType.GroupCounters)
				cumDroppedPacketInfoCount += gcHashTable.getDroppedPacketInfoCount();
			else
				cumDroppedPacketInfoCount += lostFlowHashTable.getDroppedPacketInfoCount();
			

			for (int k_index = 0; k_index < k.length; k_index++){
				outputFlowsList = new ArrayList<FlowWithCount>();


				if (type == SummaryStructureType.EvictionWithoutCount){
					Sketch lossEstimateSketch = lostFlowHashTable.getSketch();
					for (Long f : lostFlowHashTable.getFlowIdBuckets()){
						if (f != 0)
							occupiedSlots[k_index]++;

						outputFlowsList.add(new FlowWithCount(f, lossEstimateSketch.estimateCount(f)));
						//System.out.println(f.flowid);
					}
				}
				else if (type == SummaryStructureType.RollingMinSingleLookup){
					HashMap<Long, Long> currentFlows = new HashMap<Long, Long>();
					for (FlowWithCount f : lostFlowHashTable.getBuckets()){
						if (f.flowid != 0)
							occupiedSlots[k_index]++;

						//System.out.println(f.flowid + " " + f.count); 
						//if (/*f.flowid != 0 && */f.count > k*lostPacketStream.size()){
						if (currentFlows.containsKey(f.flowid)){
							currentFlows.put(f.flowid, currentFlows.get(f.flowid) + f.count);
							duplicates[k_index]++;
						}
						else
							currentFlows.put(f.flowid, f.count);
						//System.out.println(f.flowid);
					}

					for (Long f : currentFlows.keySet()){
						outputFlowsList.add(new FlowWithCount(f, currentFlows.get(f)));
					}
				}
				else {
					HashMap<Long, Long> currentFlows = new HashMap<Long, Long>();
					for (FlowWithCount f : lostFlowHashTable.getBuckets()){
						if (f.flowid != 0)
							occupiedSlots[k_index]++;

						//System.out.println(f.flowid + " " + f.count); 
						//if (/*f.flowid != 0 && */f.count > threshold*lostPacketStream.size()){
						if (currentFlows.containsKey(f.flowid)){
							currentFlows.put(f.flowid, currentFlows.get(f.flowid));
							duplicates[k_index]++;
						}
						else
							currentFlows.put(f.flowid, f.count);
						//System.out.println(f.flowid);
					}

					for (FlowWithCount f : lostFlowHashTable.getBuckets()){
						outputFlowsList.add(new FlowWithCount(f.flowid, f.count));
					}
				}

				// observed flows in sorted order so that we can pick the hh as the top k
				FlowWithCount[] outputFlowBuckets = new FlowWithCount[outputFlowsList.size()];
				outputFlowBuckets = outputFlowsList.toArray(outputFlowBuckets);
				Arrays.sort(outputFlowBuckets);

				observedHH = new HashMap<Long, Long>();
				for (int i = 0; i < k[k_index]; i++){
					observedHH.put(outputFlowBuckets[i].flowid, outputFlowBuckets[i].count);
				}

				int bigLoserPacketsLost = 0;
				int flag = 0;
				double deviation = 0;
				double denominator = 0;


				// first k in inputStream are expected hh - fix which heavy hitters you look at for cdf of competitors
				expectedHH = new HashSet<Long>();
				for (int i = 0; i < k[k_index]; i++){
					expectedHH.add(inputFlowArray[i].flowid);
				}

				for (long flowid : expectedHH){
					if (!observedHH.containsKey(flowid)){
						numberOfFalseNegatives[k_index]++;
					}
					else
						bigLoserPacketReported[k_index] += flowSizes.get(flowid);

					int tempFlag = 0;
					for (FlowWithCount f : outputFlowsList){
						if (f.flowid == flowid){
							tempFlag = 1;
							break;
						}
					}
					if (tempFlag == 0)
						missingFromTable[k_index]++;
					bigLoserPacketCount[k_index] += flowSizes.get(flowid);
				}

				expectedSize[k_index] = expectedHH.size();
				observedSize[k_index] = observedHH.size();

				for (long flowid : observedHH.keySet()){
					//System.out.println("hello");
					//System.out.println(FlowDataParser.convertLongToAddress(flowid));
					if (!expectedHH.contains(flowid)){
						//System.out.println(FlowDataParser.convertLongToAddress(flowid));
						numberOfFalsePositives[k_index]++;
					}
					if (flowSizes.get(flowid) == null)
						System.out.println(FlowDataParser.convertLongToAddress(flowid));
					//System.out.print(observedHH.get(flowid));
					//System.out.print(" flowid=" + flowid + " " + flowSizes.get(flowid));
					/*if (!expectedHH.contains(flowid) && observedHH.get(flowid) > flowSizes.get(flowid)){
						System.out.println(flowid + " " + observedHH.get(flowid) + " " + flowSizes.get(flowid));
					}*/

					deviation += Math.abs(observedHH.get(flowid) - flowSizes.get(flowid));
					denominator += flowSizes.get(flowid);
				}

				cumDeviation[k_index] += deviation/denominator;
			}
		}

		for (int k_index = 0; k_index < k.length; k_index++){
			System.out.print(tableSize + "," + k[k_index] + "," + D + ",");
			System.out.print((double) numberOfFalsePositives[k_index]/numberOfTrials/observedSize[k_index] + ",");
			System.out.print((double) numberOfFalseNegatives[k_index]/numberOfTrials/expectedSize[k_index] + ",");
			System.out.print(expectedSize[k_index] + "," + observedSize[k_index] + "," + (double) bigLoserPacketReported[k_index]/bigLoserPacketCount[k_index]);
			System.out.print("," + cumDeviation[k_index]/numberOfTrials + "," + occupiedSlots[k_index]/tableSize/numberOfTrials + "," + duplicates[k_index]/tableSize/numberOfTrials);
			System.out.println("," + (double) missingFromTable[k_index]/numberOfTrials/expectedSize[k_index]);
		}

		//lostFlowHashTable.printBuckets();
	}

	public static void main(String[] args){
		ArrayList<Packet> inputPacketStream;
		flowAggWithSizes = new ArrayList<FlowWithCount>(); // input stream in a convenient format
		flowSizes = new HashMap<Long, Integer>();
		if(args[0].contains("caida") || args[0].contains("Caida"))
			inputPacketStream = FlowDataParser.parseCAIDAPacketData(args[0]);
		else
			inputPacketStream = FlowDataParser.parsePacketData(args[0]);

		// read the flows to be lost from a file mentioned in the command line and create a new stream with that flow lost
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
				flowSizes.put(FlowDataParser.convertAddressToLong(fields[0]), Integer.parseInt(fields[1]));
				totalPacketsLost+= Integer.parseInt(fields[1]);
				FlowWithCount f = new FlowWithCount(FlowDataParser.convertAddressToLong(fields[0]), Integer.parseInt(fields[1]));
				flowAggWithSizes.add(f);
			}
			scanner.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.format("Exception occurred trying to read '%s'.", args[1]);
			e.printStackTrace();
			return;
		}

		//final int tableSize[] = {30, 75, 150, 300, 500, 900, 1200, 1500, 2000};
		//final int tableSize[] = {/*100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1200, 1400, 1600, 1800, 1024, 2048/*, 4096, 8192*/};
		final int k[] = {50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750};
		final int tableSize[] = {/*2520, */5040, 7560, /*10080*/}; // LCM of the first 12 integers
		//final int tableSize[] = {64};

		if (args[2].equals("runTrial"))	{
			System.out.println("tableSize" + "," + "k" + "," + "D," + "FalsePositive %" + "," + "False Negative %" + "," + "expected number, reported number, hhReportedFraction, deviation, table occupancy, duplicates, fraction missing in table");
			for (int tableSize_index = 0; tableSize_index < tableSize.length; tableSize_index++) { 
				for (int D = 2; D <= 15; D++){
					if (D == 11 || D == 13)
						continue;

		 			// run the loss identification trials for the appropriate heuristic
					if (args[3].contains("Basic"))
						runTopKIdentificationTrials(SummaryStructureType.BasicHeuristic, inputPacketStream, k, tableSize[tableSize_index], D);
					else if (args[3].contains("Multi"))
						runTopKIdentificationTrials(SummaryStructureType.RollingMinWihoutCoalescense, inputPacketStream, k, tableSize[tableSize_index], D);
					else if (args[3].contains("Single"))
						runTopKIdentificationTrials(SummaryStructureType.RollingMinSingleLookup , inputPacketStream, k, tableSize[tableSize_index], D);
					else if (args[3].contains("coalesce"))
						runTopKIdentificationTrials(SummaryStructureType.RollingMinWithBloomFilter , inputPacketStream, k, tableSize[tableSize_index], D);
				}
			}
		}
	}
}