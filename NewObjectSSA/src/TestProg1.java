
public class TestProg1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		Sample ref1 = new Sample();
		int t=1;
		if(t>0){
			ref1.x = 5;
		}
		else{
			ref1.x = 6;
		}
		t=ref1.x;
	}

}
