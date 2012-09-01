import com.yash.Employee;
import com.yash.Manager;

import java.util.ArrayList;

public class ArrayListTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Employee> staff =new ArrayList<Employee>();
		staff.add(new Employee("harry",50000.0,1989,10,1));
		staff.add(new Employee("Tommy",40000.0,1990,3,15));
		staff.add(new Manager("Carl",80000,1987,12,15));
		for(Employee e :staff){
			e.raiseSalary(5);
		}
		
		for(Employee e :staff){
			System.out.println(e.toString());
		}
		staff.get(2);
		staff.set(1,staff.get(1));
	}

}
