import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;


public class trial {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
		Collection items = new ArrayList();
		Collection items2;
		
		items.add("item1");
		items.add("item2");
		items.add("item3");
		items2 = items;
		while(!items2.isEmpty()) {
		for(Iterator<ArrayList<Object>>iter = items.iterator();iter.hasNext();) {
			items2.add("item0");
			Object x = iter.next();
			items2.remove(x);
			iter=items2.iterator();
		}
		
		}
		
		/*items.add("item0");
		Iterator itemIter = items.iterator();
		itemIter.next();
		items.add("item1");*/
		
//		itemIter.next();
		}
		catch (ConcurrentModificationException e) {
		// an exception will always be caught.
			e.printStackTrace();
		}
	}

}
