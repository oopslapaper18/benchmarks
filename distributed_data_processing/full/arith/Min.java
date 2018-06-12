package original.arith;

import java.util.List;

public class Min {	
	public static void main(String[] args) {
		minList(null);
	}
	
	public static int minList(List<Integer> data) {
		int min = Integer.MAX_VALUE;
		for(int i=0; i<data.size(); i++) {
			int var = data.get(i);
			min = Math.min(var,min);
		}
		return min;
	}
}