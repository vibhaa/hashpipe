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
	public static ArrayList<Packet> createSingleLossyFlow(ArrayList<Packet> packetStream, long flowToBeLost){
		ArrayList<Packet> lossyStream = new ArrayList<Packet>();
		for (Packet p : packetStream){
			if (p.srcip != flowToBeLost)
				lossyStream.add(p);
		}

		return lossyStream;
	}
}