
public class TestProg4 {

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
		Sample ref3 = new Sample();
		int t=ref1.x;
		if(t<5){
			ref1.next = ref2;
		}
		else if(t>5&&t<7){
			ref1.next = ref3;
		}
		else;
		ref1.next.x = 100;
	}
	

}
