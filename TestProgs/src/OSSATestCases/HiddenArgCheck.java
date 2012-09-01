package OSSATestCases;

public class HiddenArgCheck {

	public static Sample s1 = new Sample(); 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Sample a = new Sample();
		Sample b = new Sample();
		a.next = b;
		test(a);
	}
	
	public static void test(Sample a) {
		Sample b = a.next;
		b.x=4;
	
	}
	
}
