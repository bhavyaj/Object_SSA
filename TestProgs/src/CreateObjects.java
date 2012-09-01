
public class CreateObjects {

	/**
	 * @param args
	 */
	public static void pointstodump(Object x){
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	/*	Sample a = new Sample();
		Sample b = new Sample();
		Sample c = new Sample();
		c= new Sample();
		Sample d = new Sample();
		if(3<5)
			a=b;
	*/	Sample f = new Sample();
		Sample e = f;
		for(int i=1;i<5;i++){
			//Sample e = new Sample();
			f.next = new Sample();
			f = f.next;
		}
		
		e.next.next.next= new Sample();
		Sample e2=e.next;
		//e = new Sample();
		Sample e3 = e.next.next;
		
		//f.next=new Sample();
//		e= null;
		pointstodump(e3);
		
		//make call to fun1() in order to get corresponding nodes in CallGraph
		//Sample g=fun1();
		func();
		
		int t= e3.x;
	}
	
	public static void func(){
		Sample a = new Sample();
		a.next = new Sample();
		Sample c = a;
		int n = a.x;
		if (n < 10){
			c=a.next;
			Sample b = new Sample();
			a.next = b;
			n=5;
		}
		a.x = n;
		a.next.next=c;
		
	}
	
//	public static Sample fun1(){				//Intreprocedural - not handled
	public static void fun1(){
		Sample a = new Sample();
		Sample b = new Sample();
		Sample f = new Sample();
		Sample e = f;
//		for(int i=1;i<5;i++){
			//Sample e = new Sample();
			f.next = new Sample();
			f = f.next;
//		}
		f.x=6;
//		a.x=f.x+e.x;
		e.next.next.next= new Sample();
		Sample e2=e.next;
		//e = new Sample();
		Sample e3 = e.next.next;
//		e2.next = e3.next;
//		return e2;
		
	}
	
//	public static void fun2(Sample a){		//Interprocedural- not handled
	public static void fun2(){
		Sample b = new Sample();
		Sample c = new Sample();
		b.x=1;
		c.next=b;
		int x=10;
		if(x>5){
			b = c;
			c = new Sample();
		}
		b.x=1;
		if(c.next!=null);
		c=b.next;
//		a.x=b.x+c.x;
//		b=a.next;
		return;
	}
	
//	public static void fun3(Sample a){		//Interprocedural analysis - not handled
	public static void fun3(){
		Sample b = new Sample();
		Sample c = new Sample();
		Sample d = c;
		for(int i =1;i<=10;i++){
			d.next = new Sample();
			d = d.next;
		}
		d=c;
		for(int i=1; i<=5;i++){
			d=d.next;
			d.x = 6;
		}
		d.next=null;
		d.next = new Sample();
		
		d.next.next=b;
		
		return;
	}

}


