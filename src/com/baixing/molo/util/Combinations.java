package com.baixing.molo.util;

import java.util.ArrayList;

public class Combinations {

    // print all subsets of the characters in s
    public void comb1(ArrayList s) { comb1(new ArrayList(), s); }
    public ArrayList resultSet = new ArrayList();
    // print all subsets of the remaining elements, with given prefix 
    private void comb1(ArrayList prefix, ArrayList s) {
        if (s.size() > 0) {
        	ArrayList al = new ArrayList(prefix);
        	al.add(s.get(0));
            resultSet.add(al);
        	ArrayList ss = new ArrayList();
        	for(int i = 1;i<s.size();i++){
        		ss.add(s.get(i));
        	}
            comb1(al, ss);
            comb1(prefix,ss);
        }
    }  

    // alternate implementation
    public void comb2(ArrayList s) { comb2(new ArrayList(), s); }
    private void comb2(ArrayList prefix, ArrayList s) {
    	if(prefix.size()>0) resultSet.add(prefix);
        for (int i = 0; i < s.size(); i++){
        	ArrayList al = new ArrayList(prefix);
        	al.add(s.get(i));
        	ArrayList sub = new ArrayList();
        	for(int j = i+1;j<s.size();j++){
        		sub.add(s.get(j));
        	}
            comb2(al, sub);
        }
    }  

    public ArrayList getCombs(ArrayList s){
    	comb2(new ArrayList(), s); 
    	return resultSet;
    }
    
    public ArrayList getCombs(Object[] s){
    	ArrayList ss = new ArrayList();
    	for(Object o:s){
    		ss.add(o);
    	}
    	comb2(new ArrayList(), ss); 
    	return resultSet;
    }

    public static void main(String[] args) {
       Combinations c = new Combinations();
       // using first implementation
       Integer[] ls = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12,13};
       ArrayList r = c.getCombs(ls);
       System.out.println(r.size());
    }

}