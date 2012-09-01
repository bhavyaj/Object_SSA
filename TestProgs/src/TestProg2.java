
public class TestProg2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		Sample ref1 = new Sample();
		//Sample ref2 = new Sample();	
		int t=7;
		if(t<5){
			ref1.x =101;
		}
		else if (t>5&&t<7){
			ref1.x=102;
		}
		else if(t>7&&t<9){
			ref1.x=103;
		}
		else{
			ref1.x=104;
		}
		t=ref1.x;
	}

}
