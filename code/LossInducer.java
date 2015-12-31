import java.util.*;

/* 	class that facilitates loss creation on a stream of packets
	by choosing flow(s) to be loss and returning all packets but
	those belonging to the flow*/
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
		long ipToBeLost = FlowDataParser.convertAddressToLong(flowToBeLost);
		for (Packet p : packetStream){
			if (p.getSrcIp() == ipToBeLost)
				lossyStream.add(p);
		}

		return lossyStream;
	}
}