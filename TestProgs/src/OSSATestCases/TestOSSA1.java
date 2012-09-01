package OSSATestCases;

/**
 * basic object creation, get-field and putfield instructions. Without phis.
 * @author yash
 *
 */
public class TestOSSA1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();
	}

	public static void test0(){
		Sample a = new Sample();
		Sample b = new Sample();
		a.x = 5;
		b.x = a.x;
		b.next = a;
		b.x = b.next.x;
	}
	
	public static void test() {
		Sample a = new Sample();
		Sample b = new Sample();
		int p=4;
		Sample c;
		a.next=b;
		c = a.next;
		c.x=p;
		a.x=c.x;
		p = a.next.x;
		a.next.x = 100;
	}

}
