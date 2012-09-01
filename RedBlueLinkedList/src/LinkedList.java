
public class LinkedList {
	node head;
	int size;
	
	LinkedList(node n){
		head=n;
	}
	/**
	 * @param args
	 */
	
	void appendToTail(node x){
		node end = (x);
		node n =this.head;
		if(n==null){
			head=end;
			return;
		}
		while(n.next!=null)
			n=n.next;
		n.next=end;
	}
	
	node deletefirst(){
		node n =head;
		
		if(head!=null)
			head=head.next;
		n.next=null;
		return n;
	}
	
	public  void print(){
		node tmp=head;
		size=0;
		if(tmp==null)
			return;
		while(tmp!=null){
			System.out.println(tmp.type);
			size++;
			tmp=tmp.next;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkedList combinedList, redList, blueList;
		combinedList=new LinkedList(new node("Blue"));
		for(int i=2;i<=20;i++){
			if(i%2==0)
				combinedList.appendToTail(new node("Red"));
			else 
				combinedList.appendToTail(new node("Blue"));
		}
		combinedList.print();
		
		/*node tmp=combinedList.deletefirst();
		System.out.println("The deleted first element is "+tmp.type);
		combinedList.print();
		*/
		redList = new LinkedList(null);
		blueList = new LinkedList(null);
		
		while(combinedList.head!=null){
			node tmp = combinedList.deletefirst();
			if(tmp.type=="Red")
				redList.appendToTail(tmp);
			if(tmp.type=="Blue")
				blueList.appendToTail(tmp);
			
		}
		
		//print only the redlist..thus only the redlist is live
		System.out.println("RedList : ");
		redList.print();
/*		System.out.println("BlueList : ");
		blueList.print();
		System.out.println("CombinedList : ");
		combinedList.print();
*/		
		

	}

}
