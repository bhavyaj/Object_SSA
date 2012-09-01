package InterProcedure;



public class InterProc1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}

	public static void test(){
		Sample a = new Sample();
		test1(a);
		Sample.check(1);
		test1(a);
	}
	
	public static void test1(Sample a){
		a.next = new Sample();
		
	}
}
