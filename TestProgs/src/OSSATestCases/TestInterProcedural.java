package OSSATestCases;

public class TestInterProcedural {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test() {
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c;
		a.next = b;
		int p = a.x;
		if(p>5) {
			c=a;
		}
		else 
			c=b;
		c.x=p;
		test1(a);
		c.next = a;
	}
	
	public static void test1(Sample p) {
		p.next.next = p;
		test2(p.next);
		
	}
	
	public static void test2(Sample l) {
		test2(l.next);
//		stringTest("Test String");
		l.x=1;
	}
	
	public static void stringTest(String x) {
		System.out.println(x);
	}
	
	
	
}
