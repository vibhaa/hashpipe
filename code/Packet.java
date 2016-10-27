import java.util.*;

/* abstraction for Packets in a network that helps retrieve 
   the key fields from the packet*/
public class Packet{
	private long srcip;
	private long dstip;
	private String srcPort;
	private String dstPort;
	private String protocol;

	public Packet(long srcip, long dstip, String srcPort, String dstPort, String protocol){
		this.srcip = srcip;
		this.dstip = dstip;
		this.srcPort = new String(srcPort);
		this.dstPort = new String(dstPort);
		this.protocol = new String(protocol);
	}

	public long getSrcIp(){
		return srcip;
	}

	public long getDstIp(){
		return dstip;
	}

	public String fivetuple(){
		return Long.toString(srcip) + Long.toString(dstip) + srcPort + dstPort + protocol;
	}

	public String getFlowId(){
		return Long.toString(srcip);
	}

	public static ArrayList<Packet> computeDiff(ArrayList<Packet> startPointPackets, HashSet<Packet> endPointPackets){
		ArrayList<Packet> diffPackets = new ArrayList<Packet>();

		for (Packet p: startPointPackets){
			if (!endPointPackets.contains(p))
				diffPackets.add(p);
		}

		return diffPackets;
	}
}