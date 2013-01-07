
public class TestProgReachability {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();
	}

	public static void test(){
		Sample a = new Sample();
		Sample b = new Sample();
		//a=b;
		Sample.check(1);
//		b.x=3;//any random use of object.
		b=a;
		a.x=3;
	}
}
