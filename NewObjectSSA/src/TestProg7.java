
public class TestProg7 {

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
		Sample t = ref3;
		int p = t.x;
		if(p>5){
			t = ref1;
			ref1.x=3;
		}
		else if(p>3){
			ref1.x=2;
		}
		else
			t = ref2;
		t.x = 100;
	}
}
