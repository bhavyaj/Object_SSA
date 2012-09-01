package OSSATestCases;

public class StaticTest {
	
	public static Sample a = new Sample();
	public static Sample b = new Sample();
	public static Sample c ;
	public static StaticTest e = new StaticTest();
	public Sample x = new Sample();
	public StaticTest(){
		c = new Sample();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test(a);
	}
	
	public static Sample test(Sample y) {
		
		Sample x = new Sample();
//		Sample y = test(a,x);
		if(5>4) {
		a.next = b;
		b.next = a.next;}
		a.x =2;
		x = test(b);
		e.x.x = 100;
		return a;
	}

}
