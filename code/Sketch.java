import java.*;

/* count min sketch that is used to maintain a summary data structure
   to estimate the frequency of occurence of certain items in a data
   stream

   used in this context to count the number of packets per flow that
   have been lost

	initially used to simulate reversible sketches too - hence, there
	is some commented out code that belongs to that technique
*/
public class Sketch{
	private int size;							// K or the max number of buckets in a hash table
	private int numberOfHashFunctions; 		 	// H
	private int totalNumberOfKeys;				// N or the total number of distinct keys
	private int mangler; 						// random odd number between 0 and n - 1
	private int totalNumberOfPackets; 			// total number of packets for which the sketch data has been currently collected
	
	// main data structure that maintains the sketch
	// i,jth index contains the number of packets that hashed to the the jth bucket when hashed with the ith hash function 
	private long[][]	hashMatrix; 

	// a and b to compute the hashFunctions needed, every ith index in the hashSeedA and hashSeedB arrays are
	//used to form a linear combination to get a hashfunction of the form ((ax + b) %p) %size
	private final int[] hashSeedA;
	private final int[] hashSeedB;
	private final int p;

	public Sketch(int size, int numberOfHashFunctions, int totalNumberOfKeys){
		this.size = size;
		this.numberOfHashFunctions = numberOfHashFunctions;
		this.totalNumberOfKeys = totalNumberOfKeys;
		this.mangler = (int) Math.random()*(totalNumberOfKeys - 1);
		hashMatrix = new long[numberOfHashFunctions][size];
		this.totalNumberOfPackets = 0;

		this.p = 5171;
		int hashA[] = {  701, 557, 1667, 773, 2017, 1783, 883, 307, 199, 2719, 2851, 1453, 421, 149, 311};
		this.hashSeedA = hashA;
		int hashB[] = {  31, 151, 3359, 643, 1103, 73, 109, 233, 2927, 3061, 409, 3079, 2341, 179, 1213};
		this.hashSeedB = hashB;
		// a and b to compute the hashFunctions needed, every ith index in the hashSeedA and hashSeedB arrays are
		//used to form a linear combination to get a hashfunction of the form ((ax + b) %p) %size
		/*long[] hashSeedA = { 421, 149, 151, 59032440799460394L,
		      1380096083914250750L,
		      9216393848249138261L,
		      1829347879307711444L,
		      9218705108064111365L};

		this.hashSeedA = hashSeedA;
		
		long[] hashSeedB = {73L, 109L, 87L,
			  832108633134565846L,
		      9207888196126356626L,
		      1106582827276932161L,
		      7850759173320174309L,
		      8297516128533878091L};
		this.hashSeedB = hashSeedB;*/
	}

	public int getSize(){
		return size;
	}

	public int getNumberOfHashFunctions(){
		return numberOfHashFunctions;
	}

	public long[][] getMatrix(){
		return hashMatrix;
	}

	public int getTotalNumberofPackets(){
		return totalNumberOfPackets;
	}

	private int hash(long word, int hashFunctionIndex){
		//System.out.println(word.substring(25));
		//System.out.println(hashSeedA[hashFunctionIndex]*word + hashSeedB[hashFunctionIndex]);
		return (int) ((hashSeedA[hashFunctionIndex]*word + hashSeedB[hashFunctionIndex]) % p) % size;
	}

	// update the sketch to reflect that a packet with the id has been received
	// asume updateCount is called on a packet only once
	public void updateCount(long flowid){
		//String flowid = p.fivetuple();

		/* mangle the ip
		long mangledIP = (ip * mangler) % totalNumberOfKeys;

		// divide the key into q words, assuming 32 bit IP prefixes, extract the 4 words 
		// q: should i be splitting the mangled key or the original key?
		int word1 = (ip >> 24) & 0xFF;
		int word2 = (ip >> 16) & 0xFF;
		int word3 = (ip >> 8) & 0xFF;
		int word4 = ip & 0xFF;

		totalNumberOfPackets++;*/
		//long flowid = p.getSrcIp();
		//String flowid = p.fivetuple();
		totalNumberOfPackets++;

		// hash the ip and update the appropriate counters
		for (int i = 0; i < numberOfHashFunctions; i++){
			// hash word by word numberofHashFunctions times independently
			int hashbucket = hash(flowid, i);
			//System.out.println(hashbucket + " " + flowid);
			hashMatrix[i][hashbucket]++; 
		}
	}

	// update the sketch to reflect that a packet with the id has been received
	// asume updateCount is called on a packet only once
	// return an estimate for the flowid associated with the packet p
	public void updateCountInSketch(Packet p){
		long flowid = p.getSrcIp();
		//String flowid = p.fivetuple();

		totalNumberOfPackets++;

		// hash the ip and update the appropriate counters
		for (int i = 0; i < numberOfHashFunctions; i++){
			// hash word by word numberofHashFunctions times independently
			int hashbucket = hash(flowid, i);
			hashMatrix[i][hashbucket]++; 
		}
	}


	// subtract the sketch represented by otherTable from this table and store the result in this table
	public void subtract(Sketch otherTable) throws Exception{
		if (otherTable.getSize() != size || otherTable.getNumberOfHashFunctions() != numberOfHashFunctions)
			throw new Exception("incompatible sizes");

		for (int i = 0; i < numberOfHashFunctions; i++)
			for (int j = 0; j < size; j++){
				this.hashMatrix[i][j] -= otherTable.getMatrix()[i][j];
			}

		this.totalNumberOfPackets -= otherTable.totalNumberOfPackets;
	}

	// query an estimate for the loss of this flow identified by its flow id
	// using the count-min approach
	public long estimateCount(long flowid){
		long min = hashMatrix[0][hash(flowid, 0)];
		for (int i = 1; i < numberOfHashFunctions; i++){
			int hashbucket = hash(flowid, i);
			if (hashMatrix[i][hashbucket] < min)
				min = hashMatrix[i][hashbucket];
		}
		return min;
	}

	// reset the sketch by setting all counters to 0
	public void reset(){
		for (int i = 0; i < numberOfHashFunctions; i++){
			for (int j = 0; j < size; j++)
				hashMatrix[i][j] = 0;
		}
	}

	// get the occupancy - number of occupied slots in the cm sketch as a fraction of total slots
	public double getOccupancy(){
		double occupiedSlots = 0;
		for (int i = 0; i < numberOfHashFunctions; i++){
			for (int j = 0; j < size; j++)
				if (hashMatrix[i][j] != 0)
					occupiedSlots++;
		}
		return occupiedSlots/(numberOfHashFunctions*size);
	}
} 