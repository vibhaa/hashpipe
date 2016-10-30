import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class AnalyzeData{
	private static class ExpSetting{
		public int k;
		public int d;
		public int m;
		//private SummaryStructureType type;

		public ExpSetting(int k, int D, int m){
			this.k = k;
			this.d = D;
			this.m = m;
			//this.type = type;
		}

		@Override
    	public boolean equals(Object o) {
        	if (this == o) return true;
        	if (!(o instanceof ExpSetting)) return false;
        	ExpSetting that = (ExpSetting) o;
        	return k == that.k && d == that.d && m == that.m;
   		}

		@Override
		public int hashCode() {
			String s = Integer.toString(k);
			s += Integer.toString(d);
			s += Integer.toString(m);
		    return s.hashCode();
		}
	}

	public static double getMean(ArrayList<Double> data){
		double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/data.size();
	}

	public static double getStdDev(ArrayList<Double> data)
    {
        double mean = getMean(data);
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        double var = temp/data.size();
        return Math.sqrt(var);
    }

	public static void main(String[] args){
		HashMap<ExpSetting, ArrayList<Double>> expToFalseNegs = new HashMap<ExpSetting, ArrayList<Double>>();
		HashMap<ExpSetting, ArrayList<Double>> expToWeightedFalseNegs = new HashMap<ExpSetting, ArrayList<Double>>();
		
		File file = new File(args[0]);
		try {

			Scanner scanner = new Scanner(file);
			String line;
			while (scanner.hasNextLine())
			{
				line = scanner.nextLine();
				String[] fields = line.split(",");

				int offset = 0;
				if (args[1].equals("SH"))
					offset = 1;

				int m = Integer.parseInt(fields[0]);
				int k = Integer.parseInt(fields[1 + offset]);
				int d = Integer.parseInt(fields[2 + offset]);
				double falseNeg = Double.parseDouble(fields[4 + offset]);
				double weightedFalseNeg = 1 - Double.parseDouble(fields[7 + offset]);

				ExpSetting newexp = new ExpSetting(k, d, m);

				ArrayList<Double> curList1  = null;
				ArrayList<Double> curList2 = null;

				if (expToFalseNegs.containsKey(newexp)){
					curList1 = expToFalseNegs.get(newexp);
					curList1.add(falseNeg);
				}
				else {
					curList1 = new ArrayList<Double>();
					curList1.add(falseNeg);
				}

				if (expToWeightedFalseNegs.containsKey(newexp)){
					curList2 = expToWeightedFalseNegs.get(newexp);
					curList2.add(falseNeg);
				}
				else {
					curList2 = new ArrayList<Double>();
					curList2.add(falseNeg);
				}

				expToFalseNegs.put(newexp, curList1);
				expToWeightedFalseNegs.put(newexp, curList2);
			}
			scanner.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.format("Exception occurred trying to read '%s'.", args[0]);
			e.printStackTrace();
			return;
		}

		System.out.println("tablesize" +"," + "K, D," + "Mean False Negs" + "," + "StdDev False Negs" + "," + "Mean Weighted FN," + "STDDev Weighted FN");
		ArrayList<Double> curList1  = null;
		ArrayList<Double> curList2 = null;
		for (ExpSetting e : expToFalseNegs.keySet()){
			curList1 = expToFalseNegs.get(e);
			curList2 = expToWeightedFalseNegs.get(e);

			System.out.println(e.m +"," + e.k + "," + e.d + "," + getMean(curList1) + "," + getStdDev(curList1) + "," + getMean(curList2) + "," + getStdDev(curList2));
		}
	}
}