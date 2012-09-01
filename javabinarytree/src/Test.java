
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int a=1;
		BinarySearchTree t1 = new BinarySearchTree(new int []{4,5,1,7,6,3,0,9,5,7,1,2});
		t1.bfstraverse();
		System.out.println(t1.getdepth());
	}
	

}
