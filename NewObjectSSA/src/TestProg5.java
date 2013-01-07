
public class TestProg5 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		Sample ref1 = new Sample();
		int t = ref1.x;
		if(t<5){
			ref1.x = 100;
			ref1.x =200;
		}
		else if(t>5&&t<7){
			ref1.x = 150;
		}
		else;
		t=ref1.x;
	
	}
	
}
