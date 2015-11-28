import java.util.*;

public class Packet{
	private String srcip;
	private String dstip;
	private String srcPort;
	private String dstPort;
	private String protocol;

	public Packet(String srcip, String dstip, String srcPort, String dstPort, String protocol){
		this.srcip = new String(srcip);
		this.dstip = new String(dstip);
		this.srcPort = new String(srcPort);
		this.dstPort = new String(dstPort);
		this.protocol = new String(protocol);
	}

	public String getSrcIp(){
		return srcip;
	}

	public String getDstIp(){
		return dstip;
	}

	public String fivetuple(){
		return srcip + dstip + srcPort + dstPort + protocol;
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