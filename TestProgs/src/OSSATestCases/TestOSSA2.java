package OSSATestCases;

/**
 * putphi on concrete objects
 * @author yash
 *
 */
public class TestOSSA2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();
	}

	public static void test(){
		
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c = a;
		int p = a.x;
		if(p>4){
//			c = a;
			c.x = 7;
			b.next =a;
		}
		else{
//			c=b;
			a.x = 8;
		}
		/*Sample d =  new Sample();
		c.next = d;
		d.x = b.x;
		a.next = b;*/
		c.next=b;
		b.x=p;
				
	}
}
