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
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c = new Sample();
		Sample d;
		Sample e;
		
		int p =a.x;
		int q =b.x;
		int r =c.x;
		
		if(p>10){
			if(q>5){
				d=a;
				a.x=3;
			}
			else{
				d=b;
			}
			a.x=4;
			d.x=2;
		}
		
		else{
			if(r>1){
				e=b;
			}
			else
			{
				e=c;
			}
			a.x=5;
			e.x=20;
		}
			
		a.next=c;
		b.x=2;
		p=b.x;
		b.x=a.x;
		return;
	
		
	}

}

