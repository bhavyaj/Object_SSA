import com.yash.Manager;
import com.yash.Employee;

public class ManagerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Construct a manager object
		Manager boss = new Manager("Carl",80000,1987,12,15);
		boss.setBonus(10000);
		Employee[] staff = new Employee[3];
		staff[0]=boss;
		staff[1]= new Employee("harry",50000.0,1989,10,1);
		staff[2]= new Employee("Tommy",40000.0,1990,3,15);
		Manager m1 = (Manager) staff[0];
		//Manager m2 = (Manager) staff[1];
		m1.setBonus(6000);
		System.out.println(m1.toString());
		//Printing info for all employees
		for(Employee e : staff){
			System.out.println(e.getname()+e.getSal()+e.getHireDate());
			System.out.println(e.toString());
		}
		
	}

}
