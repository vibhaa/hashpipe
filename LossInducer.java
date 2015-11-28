import java.util.*;

public class LossInducer{
	private int numberOfLossyFlows;
	private int granularity;

	public LossInducer(int numberOfLossyFlows, int granularity){
		this.numberOfLossyFlows = numberOfLossyFlows;
		this.granularity = granularity;
	}

	// takes in an original packet stream and a flow that we want to deliberately make lossy
	// and returns a packet stream with all the packets but those on the flow to be lost
	// flow recognized by the srcip
	public static HashSet<Packet> createSingleLossyFlow(HashSet<Packet> packetStream, String flowToBeLost){
		HashSet<Packet> lossyStream = new HashSet<Packet>();
		for (Packet p : packetStream){
			if (!p.getSrcIp().equals(flowToBeLost))
				lossyStream.add(p);
		}

		return lossyStream;
	}
}