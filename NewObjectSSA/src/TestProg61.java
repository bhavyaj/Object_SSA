
/**
 * Java Class to test the object phi insertions in place of wala-IR phis.
 * @author yash
 *
 */

public class TestProg61 {

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
		Sample t;
		int p =ref1.x;
		if(p>5){
			t=ref1;			
		}
		else 
			t=ref2;
		p=t.x;
	}
}
