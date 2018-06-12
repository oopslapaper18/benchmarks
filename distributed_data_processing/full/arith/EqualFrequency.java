package original.arith;

import java.util.List;

public class EqualFrequency {	
	public static void main(String[] args) {
		equalFrequency(null);
	}
	
	public static boolean equalFrequency(List<Integer> data) {
		int first = 0;
		int second = 0;
		for(int i=0; i<data.size(); i++) {
			int var = data.get(i);
			if(var == 100){
				first++;
			}
			if(var == 110){
				second++;
			}
		}
		return first == second;
	}
}