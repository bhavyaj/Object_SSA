package OSSATestCases;

public class HiddenArgCheck {

	public static Sample s1 = new Sample(); 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Sample a = new Sample();
		Sample b = a.next;
		
		test(a);
		b.x=5;
		System.out.println(b.x);
	}
	
	public static void test(Sample a) {
		a.next=new Sample();
	
	}
	
}
