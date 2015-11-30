import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/* parser to parse flow data from a csv file
into a packetStream which is represented by a HashSet */

public class FlowDataParser{
	public static long convertAddressToLong(String address) {
		byte[] bytes = null;
		try{
			bytes = InetAddress.getByName(address).getAddress();
		}
		catch (Exception e){
			e.printStackTrace();
			return 0;		
		}

		long val = 0;
		for (int i = 0; i < bytes.length; i++) {
			val <<= 8;
			val |= bytes[i] & 0xff;
		}
		return val;
	}

	public static HashSet<Packet> parsePacketData(String filename){
		HashSet<Packet> packetStream = new HashSet<Packet>();
		File file = new File(filename);
		try
		{
			Scanner scanner = new Scanner(file);
			String line;
			int linenumber = 0;
			String[] fields = new String[24];
			while (scanner.hasNextLine())
			{
				line = scanner.nextLine();
				if (linenumber++ == 0)
					continue;

				fields = line.split(",");

				String srcipString = fields[10];
				long srcip = convertAddressToLong(srcipString);

				String dstipString = fields[11];
				long dstip = convertAddressToLong(dstipString);

				String srcPort = fields[15];
				String dstPort = fields[16];;
				String protocol = fields[17];
				Packet p = new Packet(srcip, dstip, srcPort, dstPort, protocol);
				packetStream.add(p);
			}
			scanner.close();
			return packetStream;
		}
		catch (FileNotFoundException e)
		{
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
			return null;
		}
	}
}