public class LossyFlowIdentifier{
	public static ReverseLookUpTables
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

	}

	public static void main(String[] args){
		// constants for this LossyFlowIdentifier
		final int K = 5;			// number of buckets in each hash table
		final int N = 15;			// total number of unique keys
		final int H = 4; 			// number of hash functions

		// pass the filename from which the packet data needs to be parsed
		ArrayList<Packet> originalPacketStream = parseData(args[1]);

		// read the flow to be lost from the command line and create a new stream with that flow lost
		long flowToBeLost = Integer.parseInt(args[2]);
		ArrayList<Packet> lossyPacketStream = LossInducer.createSingleLossyFlow(originalPacketStream, flowToBeLost);

		// collect stream data at the start point of the link
		FlowHashTable startSketch = new FlowHashTable(K, H, N);
		for (Packet p : originalStream){
			startSketch.updateCount(p);
		}

		// collect stream data at the end point of the link
		FlowHashTable endSketch = new FlowHashTable(K, H, N);
		for (Packet p : lossyPacketStream){
			endSketch.updateCount(p);
		}

		// subtract the two sketches and store the result in the startSketch
		startSketch.subtract(endSketch);

		// identify all the heavy buckets that contributed to the loss
		ArrayList<BucketMatrixIndex> lossyBuckets = identifyLossyBuckets(startSketch, threshold);

		// perform intersections on the buckets to get the lossy flows
		ArrayList<long> lossyFlows = identifyFlowsFromBuckets(lossyBuckets);
		for (long flow : lossyFlows)
			System.out.println(flow);
	}
	
}