import java.*;

public class Sketch{
	private int size;							// K or the max number of buckets in a hash table
	private int numberOfHashFunctions; 		 	// H
	private int totalNumberOfKeys;				// N or the total number of distinct keys
	private int mangler; 						// random odd number between 0 and n - 1
	private int totalNumberOfPackets; 			// total number of packets for which the sketch data has been currently collected
	
	// main data structure that maintains the sketch
	// i,jth index contains the number of packets that hashed to the the jth bucket when hashed with the ith hash function 
	private long[][]	hashMatrix; 

	// a and b to compute the hashFunctions needed, every ith index in the a and b arrays are
	//used to form a linear combination to get a hashfunction of the form ((ax + b) %p) %size
	private final int[] a;
	private final int[] b;
	private final long p;

	public Sketch(int size, int numberOfHashFunctions, int totalNumberOfKeys){
		this.size = size;
		this.numberOfHashFunctions = numberOfHashFunctions;
		this.totalNumberOfKeys = totalNumberOfKeys;
		this.mangler = (int) Math.random()*(totalNumberOfKeys - 1);
		hashMatrix = new long[numberOfHashFunctions][size];
		this.totalNumberOfPackets = 0;

		// a and b to compute the hashFunctions needed, every ith index in the a and b arrays are
		//used to form a linear combination to get a hashfunction of the form ((ax + b) %p) %size
		a = new int[numberOfHashFunctions];
		b = new int[numberOfHashFunctions];
		for (int i = 0; i < numberOfHashFunctions; i++){
			a[i] = 2*i + 1;
			b[i] = 2*i;
		}

		p = (int) Math.pow(2,32) - 1;
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

	private int hash(String word, int hashFunctionIndex){
		return (int) ((a[hashFunctionIndex]*word.hashCode() + b[hashFunctionIndex]) % p) % size;
	}

	// update the sketch to reflect that a packet with the id has been received
	// asume updateCount is called on a packet only once
	public void updateCount(Packet p){
		String flowid = p.fivetuple();

		/* mangle the ip
		long mangledIP = (ip * mangler) % totalNumberOfKeys;

		// divide the key into q words, assuming 32 bit IP prefixes, extract the 4 words 
		// q: should i be splitting the mangled key or the original key?
		int word1 = (ip >> 24) & 0xFF;
		int word2 = (ip >> 16) & 0xFF;
		int word3 = (ip >> 8) & 0xFF;
		int word4 = ip & 0xFF;

		totalNumberOfPackets++;*/

		// hash the ip and update the appropriate counters
		for (int i = 0; i < numberOfHashFunctions; i++){
			// hash word by word numberofHashFunctions times independently
			int hashbucket = hash(flowid, i);
			hashMatrix[i][hashbucket]++; 
		}
	}

	// update the sketch to reflect that a packet with the id has been received
	// asume updateCount is called on a packet only once
	// return an estimate for the flowid associated with the packet p
	public void updateCountInMinSketch(Packet p){
		String flowid = p.fivetuple();

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
	public long estimateLossCount(String flowid){
		long min = hashMatrix[1][hash(flowid, 0)];
		for (int i = 1; i < numberOfHashFunctions; i++){
			int hashbucket = hash(flowid, i);
			if (hashMatrix[i][hashbucket] < min)
				min = hashMatrix[i][hashbucket];
		}
		return min;
	}

} 