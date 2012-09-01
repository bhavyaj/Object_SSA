
public class TestLinkedList {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		Sample head = new Sample();
		Sample tail = head;
		for(int i=1;i<10;i++){
			tail.next = new Sample();
			tail = tail.next;
//			tail = new Sample();
		}
	}

}
