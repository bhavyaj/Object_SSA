
public class prabhuprog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		bar();
	}
	
	public static void bar(){
		Sample n = new Sample();
		Sample m =  new Sample();
		Sample o = new Sample();
		n.next = m;
		Sample p = helper(n);
		Sample q = n;
		n.next = o;
		n.next = m ;
		int x =n.x;
		if(x>4){
			q.next = o;
			q = m;
		}		
		x=q.x;
	}
	
	public static Sample helper(Sample x){
		Sample r =  new Sample();
		Sample ret = x.next;
		ret.next = r;
		return ret;
	}

}
