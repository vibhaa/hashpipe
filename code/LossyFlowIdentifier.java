import java.util.*;

/* high level procedure that uses packet info from a csv file, parses it,
   induces loss on it, and produces a sketch for the lost packet on which
   the big loser identification process is performed 

   initially written for the sketches to all be reversible so that the 
   reversibility procedure would identify the lossy buckets - unused 
   code in the context of the hash table approach*/
public class LossyFlowIdentifier{
	public static PriorityQueue<LossyFlow> HeapOfLossyFlows; 
	private class BucketMatrixIndex{
		int hashfunctionIndex;
		int bucketIndex;

		public BucketMatrixIndex(int i, int j){
			hashfunctionIndex = i;
			bucketIndex = j;
		}
	}

	public class LossyFlow{
		long flowid;
		int losscount;

		LossyFlow(long flowid, losscount){
			this.flowid = flowid;
			this.losscount = losscount;
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
		ArrayList<Packet> finalPacketStream = LossInducer.createSingleLossyFlow(originalPacketStream, flowToBeLost);

		// collect stream data at the start point of the link
		ArrayList<Packet> lostPacketStream = computeDiff(originalPacketStream, finalPacketStream);
		FlowHashTable lostPacketSketch = new FlowHashTable(K, H, N);
		for (Packet p : lostPacketStream){
			lostPacketSketch.updateCount(p);
		}

		/* collect stream data at the end point of the link
		FlowHashTable endSketch = new FlowHashTable(K, H, N);
		for (Packet p : lossyPacketStream){
			endSketch.updateCount(p);
		}

		// subtract the two sketches and store the result in the startSketch
		startSketch.subtract(endSketch);*/

		// identify all the heavy buckets that contributed to the loss
		ArrayList<BucketMatrixIndex> lossyBuckets = identifyLossyBuckets(startSketch, threshold);

		// perform intersections on the buckets to get the lossy flows
		ArrayList<long> lossyFlows = identifyFlowsFromBuckets(lossyBuckets);
		for (long flow : lossyFlows)
			System.out.println(flow);
	}
	
}