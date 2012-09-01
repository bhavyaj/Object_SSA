package com.yash;

import java.util.*;

public class Employee extends Person{
	//Fields
	private final String name;
	private final Date hireDate;
	private Double sal;
	private static int empno = 0;
	public Employee(String name,Double sal,int yr,int month,int date){
		super(name);
		this.name=name;
		this.sal=sal;
		GregorianCalendar calendar = new GregorianCalendar(yr, month-1, date);
		hireDate = calendar.getTime();
		empno++;
	}
	
	public String toString(){
		return getClass().getName()+"[name="+name+"salary="+sal+"hireday="+hireDate+"]";
	}
	
	public String getname(){
		return name;
	}
	
	public double getSal(){
		return sal;
	}
	
	public String getHireDate(){
		return hireDate.toString();
	}
	
	public static final int getEmpno(){
		return empno;
	}
	
	public static void swap(Employee a, Employee b){
		Employee temp=a;
		a=b;
		b=temp;
	}
	
	public void raiseSalary(double byPercent){
		double raise = sal * byPercent /100;
		sal+= raise;
	}
	public boolean equals(Object otherObject){
		if(this==otherObject)
			return true;
		if (otherObject==null)
			return false;
		if(getClass()!=otherObject.getClass())
			return false;
		Employee other =(Employee) otherObject;
		
		return name.equals(other.name)&&sal==other.sal&&hireDate.equals(other.hireDate);
		
	}
	public int hashCode(){
		return 7*name.hashCode()+11*new Double(sal).hashCode()+13*hireDate.hashCode();
	}
	
}
