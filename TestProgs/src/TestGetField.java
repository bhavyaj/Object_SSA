
public class TestGetField {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		Sample ref1 = new Sample();
		Sample ref2 = new Sample();
		ref1.next = ref2;
		Sample t = ref1.next;
//		t.x=6;
//		compare(ref2,t);
		
/*		ref1=ref2;
		check(1);
*/		//ref1.next.x = 5;
	}

}
