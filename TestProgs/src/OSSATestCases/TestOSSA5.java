package OSSATestCases;

public class TestOSSA5 {

	/**
	 * @param args
	 */
	public static Sample s1 = new Sample(); 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test() {
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c = new Sample();
		Sample d = a;
		a.next = b;
		int p = 1;
		if(p>1) {
			d=func(a);
			b = s1;
		}		
		d.next=c;
		
	}
	
	public static Sample func(Sample a) {
		return a.next;
		
	}

}
