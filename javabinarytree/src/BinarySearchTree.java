
public class BinarySearchTree {
	private Node root;
	public BinarySearchTree(){}
	
	public BinarySearchTree(int[] elements){
		
		for(int i=0;i<elements.length;i++){
			//System.out.println("Array constructor called"+i);
			insert(elements[i]);
			
		}
	}
	public boolean insert(int i){
		if (root==null){
			root = new Node(i);
		}
		else{
			Node parent=null;
			Node current = root;
			while(current!=null){
				if(i<current.value){
					parent=current;
					current=current.left;
				}else if(i>current.value){
					parent=current;
					current=current.right;
				}
				else
					return false;
			}
			if(i<parent.value)
				parent.left=new Node(i);
			else
				parent.right=new Node(i);
			//System.out.println("value inserted"+i);
		}
		
		return true;
	}//insert(int)
	
	public int getdepth(){
		return depth(root);
	}
	
	public int depth(Node root){
		if(root==null)
			return 0;
		else
			return 1+Math.max(depth(root.left), depth(root.right));
	}
	
	public boolean treesearch(int i, Node root){
		if(root==null)
			return false;
		if(i==root.value)
			return true;
		else 
			return treesearch(i,root.left)||treesearch(i,root.right);
	}
	public void bfstraverse(){
		bfs(root);
	}
	private void bfs(Node root){
		if(root==null)
			return;
		System.out.println(root.value+" ");
		bfs(root.left);
		bfs(root.right);
	}
}
