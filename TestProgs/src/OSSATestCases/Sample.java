package OSSATestCases;

public class Sample {

	public Sample next;
	public int x=4;
	/**
	 * @param args
	 */
	public static  int a(){
		int a =10;
		int b=5;
		int c;
		
		Sample x = new Sample();
		Sample y = new Sample();
		
		if(a>b){
			c=6;
			a=4;
		}
		else{ 
			c=7;
			a=4;
			b=2;
		}
		for(int i=7;i>1;i--){
			a=a-2;}
		a=c+b-a;
		int abc[]=new int[5];
		abc[0]=5;
		b=abc[0];
		//b=a;
		return b;//abc;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		a();
		System.out.println("main function of class Sample of Testprog called");
		
		
	}
	/**
	 * dummy function to create invokespecial instructions in ir named check() for reachability tests of objects
	 * @param i object number whose reachability is to be checked
	 */
	public static void check(int i){
		//
	}

}
