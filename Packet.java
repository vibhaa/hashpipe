public class Packet{
	public long srcip;
	public long dstip;
	private long srcPort;
	private long dstPort;
	private String protocol;

	public Packet(long srcip, long dstip, long srcip, long srcPort, long dstPort, String protocol){
		this.srcip = srcip;
		this.dstip = dstip;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.protocol = new String(protocol);
	}
}