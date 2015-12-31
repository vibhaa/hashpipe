/*Data Structure used as a building block for the hash table
 that tracks the lossy packets*/

public class FlowWithCount{
		long count;
		int flowid;

		public FlowWithCount(int flowid ,long count){
			this.count = count;
			this.flowid = flowid;
		}

		public static void reset(FlowWithCount[] buckets){
			for (int i = 0; i < buckets.length; i++){
				buckets[i].flowid = 0;
				buckets[i].count = 0;
			}
		}
	}