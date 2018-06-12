package original.arith;

import java.util.List;

public class Delta {	
	public static void main(String[] args) {
		deltaList(null);
	}
	
	public static int deltaList(List<Integer> data) {
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		for(int i=0; i<data.size(); i++) {
			int var = data.get(i);
			max = Math.max(var,max);
			min = Math.min(var,min);
		}
		return max-min;
	}
}