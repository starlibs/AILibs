package util.basic;

import java.util.HashSet;
import java.util.Set;

public abstract class MathExt {
    public static long binomial(int n, int k) {
        if (k>n-k)
            k=n-k;
 
        long b=1;
        for (int i=1, m=n; i<=k; i++, m--)
            b=b*m/i;
        return b;
    }
    
    public static Set<Integer> getIntegersFromTo(int from, int to) {
    	Set<Integer> set = new HashSet<>();
    	for (int i = from; i <= to; i++)
    		set.add(i);
    	return set;
    }
    
    public static double doubleFactorial(short k) {
    	if (k <= 0)
    		return 1;
    	return k * doubleFactorial((short)(k - 2));
    }
    
    public static float round(double d, int precision) {
    	return (float)(Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision));
    }
}
