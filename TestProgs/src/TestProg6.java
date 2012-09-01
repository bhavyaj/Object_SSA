
public class TestProg6 {

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
			t = ref1;
			p=5;
		}
		else if(p>3)
			t=ref2;
		else if(p>1){
			ref2.x=7;
			t=ref2;
			p=1;
		}
			
		else 
			t=null;
		
//		p=t.x+1;		//commented since get instructinos are not working properly.
		t.x=p;
		t.next = t;
		
	}

}
