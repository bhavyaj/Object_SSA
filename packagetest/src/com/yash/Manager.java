package com.yash;

public class Manager extends Employee{
	private double bonus;
	public Manager(String name,double sal,int year, int month, int day){
		super(name, sal, year,month,day);
		bonus = 0;
	}
	
	public void setBonus(double bonus){
		this.bonus=bonus;
	}
	
	public double getSal(){
		return super.getSal()+bonus;
	}
	
	public boolean equals(Object otherObject){
		if(!super.equals(otherObject))
			return false;
		Manager other = (Manager) otherObject;
		return bonus==other.bonus;
	}
	
	public int hashCode(){
		return super.hashCode()+17*new Double(bonus).hashCode();
	}

}

final class Exec extends Manager{
	
	public Exec(String name, double sal, int year, int month, int day){
		super(name,sal,year,month,day);
		
	}
	
}