package OSSATestCases;

public class TestOSSA4 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();
		test1();
		test2();
		test3();
		test4(new Sample());
	}

	public static void test(){
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c = new Sample();
		Sample d=null,e=null;
		int p1=a.x;
		int p2=b.x;
		int p3=c.x;
		if(p1>10) {
			if(p2>5) {
				d=a;
				a.x=3;
			}
			else {
				d=b;
			}
			a.x=4;
			d.x=2;
		}
		else {
			if(p3>1) {
				e=b;
			}
			else {
				e=c;
			}
			e.x=20;
		}
		d.next=c;
		e.x=2;
		p1=d.x;
		b.x=a.x;
		
	}
	
	public static void test1(){
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c = new Sample();
		Sample d=null,e=null;
		int p1=a.x;
		int p2=b.x;
		int p3=c.x;
//		if(p1>10) {
			if(p2>5) {
				d=a;
				a.x=3;
			}
			else {
				d=b;
			}
//			a.x=4;
			d.x=2;
//		}
//		a.x=7;
//		d.x=9;
	}
	
	public static void test2(){
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c = new Sample();
		Sample d=null,e=null;
		int p1=a.x;
		int p2=b.x;
		int p3=c.x;
		if(p1>10) {
			if(p2>5) {
				d=a;
				a.x=3;
			}
			else {
				d=b;
				p1=10;
			}
//			a.x=4;
			d.x=2;
		}
		Sample.check(1);
		a.x=p1;
		d.x=9;
	}
	public static void test3() {
		Sample a = new Sample();
		Sample b = new Sample();
		Sample c = new Sample();
		Sample d=null,e=null;
		int p1=a.x;
		int p2=b.x;
		int p3=c.x;
		Sample.check(1);
		if(p1>10) {
			if(p2>5) {
				d=a;
				a.x=3;
			}
			else {
				d=b;
			}
//			a.x=4;
			d.x=2;
		}
		Sample.check(1);
		a.x=7;
		d=test4(b);
		d.x=9;
		Sample.check(1);
		Sample.check(2);
		p1=a.x;
		a.x=2;
	}
	
	
	public static Sample test4(Sample a) {
		Sample b = new Sample();
		b.next= a;
		return a;
	}
}
