public class FlowHashTable{
	private int size;							// K or the max number of buckets in a hash table
	private int numberOfHashFunctions; 		 	// H
	private int totalNumberOfKeys				// N or the total number of distinct keys
	private int mangler; 						// random odd number between 0 and n - 1
	private int totalNumberOfPackets; 			// total number of packets for which the sketch data has been currently collected
	
	// main data structure that maintains the sketch
	// i,jth index contains the number of packets that hashed to the the jth bucket when hashed with the ith hash function 
	private int[][]	hashMatrix; 

	public FlowHashTable(int size, int numberOfHashFunctions, int totalNumberOfKeys){
		this.size = size;
		this.numberOfHashFunctions = numberOfHashFunctions;
		this.totalNumberOfKeys = totalNumberOfKeys;
		this.mangler = (int) Math.random()*(totalNumberOfKeys - 1);
		hashMatrix = new int[numberOfHashFunctions][size];
		this.totalNumberOfPackets = 0;
	}

	public int getSize(){
		return size;
	}

	public int getNumberOfHashFunctions(){
		return numberOfHashFunctions;
	}

	public int[][] getMatrix(){
		return hashMatrix;
	}

	private long hash(int word, int hashFunctionIndex){
		return ((a[hashFunctionIndex]*word + b[hashFunctionIndex]) % p) % size;
	}

	// update the sketch to reflect that a packet with the id has been received
	// asume updateCount is called on a packet only once
	public void updateCount(Packet p){
		long ip = p.srcip;

		// mangle the ip
		long mangledIP = (ip * mangler) % totalNumberOfKeys;

		// divide the key into q words, assuming 32 bit IP prefixes, extract the 4 words 
		// q: should i be splitting the mangled key or the original key?
		int word1 = (ip >> 24) & 0xFF;
		int word2 = (ip >> 16) & 0xFF;
		int word3 = (ip >> 8) & 0xFF;
		int word4 = ip & 0xFF;

		totalNumberOfPackets++;

		// hash the mangled ip
		for (int i = 0; i < numberOfHashFunctions; i++){
			// hash word by word numberofHashFunctions times independently
			long hashbucket = (hash(word1, i) << 24) + (hash(word2, i) << 16) + (hash(word3, i) << 8) + hash(word4, i);
			hashMatrix[i][hashbucket]++; 
		}
	}

	// subtract the sketch represented by otherTable from this table and store the result in this table
	public void subtract(FlowHashTable otherTable){
		if (otherTable.getSize() != size || otherTable.getNumberOfHashFunctions() != numberOfHashFunctions)
			throw new Exception("incompatible sizes");

		for (int i = 0; i < numberOfHashFunctions; i++)
			for (int j = 0; j < size; j++){
				this.hashMatrix[i][j] -= otherTable.getMatrix()[i][j];
			}

		this.totalNumberOfPackets -= otherTable.totalNumberOfPackets;
	}

} 