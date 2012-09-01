
public class TestInterProc {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Sample a = new Sample();
		Sample b = new Sample();
		test2(a,a);
		test();
		
		test1(a,b);
		test1(b,a);
	}
		
	public static void test(){
		Sample a = new Sample();
		Sample b = new Sample();
		Sample.check(1);
		Sample c = test1(a, b);
		
		Sample d = test1(b,a);
		test1(b.next,b.next);
/*		Sample.check(1);
		Sample c = test2(a,b);
		c.next=a;
		b.x = test3(c).x;
		test2(b,c);
		
	}*/
	
	}
	public static Sample test1(Sample x, Sample y){
		x.next = y;
		return x;
	}

	public static void test2(Sample x, Sample y){
		x.next = y;
		y.next =x;
	}
	/*public static Sample test3(Sample x){
		return x;
	}*/
	

}
