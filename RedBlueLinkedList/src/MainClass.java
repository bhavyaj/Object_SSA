import java.util.LinkedList;

import javax.swing.text.html.HTMLDocument.Iterator;

//Implementation of red blue linked list
//date-26dec,2011
//author Yash


public class MainClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkedList<String> redList, blueList, combinedList;
		combinedList= new LinkedList<String>();
		//create combinedList
		for(int i=1;i<=20;i++){
			if(i%2==0)
				combinedList.add("Red");
			else
				combinedList.add("Blue");
		}
		
		
		java.util.Iterator<String> it = combinedList.iterator();
		int count=1;
		System.out.println("combinedList contains "+combinedList.size()+"elements");
		while(it.hasNext()){
			System.out.println(it.next()+" "+count++);
		}
		
		//Separate in Red and Bluellist
		redList = new LinkedList<String>();
		blueList = new LinkedList<String>();
		
		/*
		it = combinedList.iterator();
		
		while(it.hasNext()){
			String temp = it.next();
			if(tmp)
		}
		*/
		while(!combinedList.isEmpty()){
			String tmp = combinedList.remove();
//			System.out.println("tmp = "+tmp);
			if(tmp=="Red"){
				redList.add(tmp);
			}
			if(tmp=="Blue"){
				blueList.add(tmp);
			}
		}
		count=1;
		it = redList.iterator();
		System.out.println("redList contains "+redList.size()+"elements");
		while(it.hasNext()){
			System.out.println(it.next()+count++);
		}
		
		
		
	}

}
