/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bsds.chapter3.rmi;

/**
 *
 * @author igortn
 */
	// Simple mybank.com server interface
	public interface MyBank extends java.rmi.Remote{
	    public double balance  (String accNo)
              throws java.rmi.RemoteException ;
	     public boolean  statement(String month)
	       throws java.rmi.RemoteException ;
	    // other operations
	 }

