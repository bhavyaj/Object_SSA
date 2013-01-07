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
	}
	
	public static Sample test1(Sample p){
		Sample q = new Sample();
		int x=1;
		int y=1;
		int z=1;
		p.next = q;
		return p;
	}
	public static void test2(){
	}
	
}
