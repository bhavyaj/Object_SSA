
public class TestInterprocwithstatics {

	public  static Sample s1 = new Sample();
	public  static Sample s2 = new Sample();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		s1.next=s2;
		s2.next=s1;
		test1(s1);
		test2(s1,s2);
	}
	
	public static void test1(Sample a){
		Sample b= new Sample();
		Sample c =a;
		Sample d =test2(b,c);
	}

	public static Sample test2(Sample x, Sample y){
		Sample p = x.next;
		y.next = x;
		Sample q = y.next;
		return q;
	}
}
