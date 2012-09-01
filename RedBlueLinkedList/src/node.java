
public class node {

	String type;
	node next;
	//Constructor
	node(String t){
		if(t=="Red"||t=="Blue")			//Add only red or black node
			type =t;
		next=null;
	}
	
	/*void appendToTail(String x){
		node end = new node(x);
		node n =this;
		while(n.next!=null)
			n=n.next;
		n.next=end;
	}
	
	node deletefirst(node head){
		node n =head;
		if(head.next!=null)
			head=head.next;
		return n;
	}
	*/
	/*public static void print(node head){
		if(head!=null){
			System.out.println(head.type);
			print(head.next);
		}
	}*/
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	/*	node combinedList, redList, blueList;
		combinedList=new node("Blue");
		for(int i=2;i<=20;i++){
			if(i%2==0)
				combinedList.appendToTail("Red");
			else 
				combinedList.appendToTail("Blue");
		}
		print(combinedList);
		
		node tmp=combinedList.deletefirst(combinedList);
		System.out.println("The deleted first element is "+tmp.type);
		print(combinedList);
*/
	}

}
