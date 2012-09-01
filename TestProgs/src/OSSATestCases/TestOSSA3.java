package OSSATestCases;


/**
 * putphi on abstract objects
 * @author yash
 *
 */
public class TestOSSA3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();
	}
	
	public static void test(){
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c;
		int p1=a.x;
		if(p1>4){
			c=a;
			c.x =1;
		}
		else{
			c=b;
			b.x=2;
		}
		c.x = 3;
		a.x = 4;
		Sample d = new Sample();
		int p2 = b.x;
		if(p2>5){
			c.x = 12;
		}
		else{
			a.x = 13;
			c = d;
		}
		c.next = a;
		d.next = b;
	}

}
