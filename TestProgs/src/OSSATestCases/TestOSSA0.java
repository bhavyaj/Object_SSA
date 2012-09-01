package OSSATestCases;

public class TestOSSA0 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		Sample x = new Sample();
		Sample y = new Sample();
		Sample z;
		int a =x.x;
		
		if(a>5){
			z = x;
		}
		else
			z=y;
		Sample t;
		if(a<4)
			t = x;
		else 
			t =y;
		
		if(t.x==z.x)
			t=z;
		
	
		
	}

}
