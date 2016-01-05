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

	public static String convertLongToAddress(long ip){
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
		//return ipStr;
	}

	public static ArrayList<Packet> parsePacketData(String filename){
		ArrayList<Packet> packetStream = new ArrayList<Packet>();
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
				/*if (linenumber++ == 0)
					continue;*/

				fields = line.split(",");

				String srcipString = fields[3];
				long srcip = convertAddressToLong(srcipString);

				String dstipString = fields[4];
				long dstip = convertAddressToLong(dstipString);

				String srcPort = fields[5];
				String dstPort = fields[6];;
				String protocol = fields[7];

				// field[11] contains the number of such packets, so create that many packets
				for (int i = 0; i < Integer.parseInt(fields[11]); i++){
					Packet p = new Packet(srcip, dstip, srcPort, dstPort, protocol);
					packetStream.add(p);
				}
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