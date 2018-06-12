package original.arith;

import java.util.List;

public class MaxAbs {	
	public static void main(String[] args) {
		maxAbsList(null);
	}
	
	public static int maxAbsList(List<Integer> data) {
		int maxAbs = Integer.MIN_VALUE;
		for(int i=0; i<data.size(); i++) {
			int var = data.get(i);
			maxAbs = Math.max(Math.abs(var),maxAbs);
		}
		return maxAbs;
	}
}