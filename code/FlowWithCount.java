public class FlowWithCount{
		int count;
		int flowid;

		public FlowWithCount(int flowid ,int count){
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