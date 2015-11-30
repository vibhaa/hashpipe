import java.util.*;

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
		return Long.toString(srcip); /* + long.toString(dstip) + srcPort + dstPort + protocol;*/
	}

	public static ArrayList<Packet> computeDiff(HashSet<Packet> startPointPackets, HashSet<Packet> endPointPackets){
		ArrayList<Packet> diffPackets = new ArrayList<Packet>();

		for (Packet p: startPointPackets){
			if (!endPointPackets.contains(p))
				diffPackets.add(p);
		}

		return diffPackets;
	}
}