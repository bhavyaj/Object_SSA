import com.yash.*;
public class PackTest {
	public static void main(String[] args){
		Employee yash = new Employee("yash", (double)10000,1998,12,6);
		System.out.println(yash.getHireDate());
		System.out.println(yash.getEmpno());
	
		Employee yash2 = new Employee("yash2", (double)90000,1998,12,7);
		System.out.println(yash.getname());
		System.out.println(yash2.getname());
		
		Employee.swap(yash, yash2);
		System.out.println(yash.getname());
		System.out.println(yash2.getname());
		
	}

	
}
