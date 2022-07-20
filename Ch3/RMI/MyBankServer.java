/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bsds.chapter4.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author igortn
 */
public class MyBankServer 
        extends UnicastRemoteObject 
        implements MyBank  {
    
    MyBankServer() throws RemoteException {
        super();
    }
   
    @Override
     public double balance  (String accNo)
              throws java.rmi.RemoteException {
         return  33.3;
     }
     @Override
     public boolean  statement(String month)
	       throws java.rmi.RemoteException {
         return true;   
     }
        public static void main(String args[]){  
        try{  
          MyBankServer server=new MyBankServer();  
          // create a registry in this JVM on default port
          Registry registry = LocateRegistry.createRegistry(1099);
          registry.bind("MyBankServer", server);
          System.out.println("server ready");
        }catch(Exception e){System.out.println(e);}  
    }  
    
}
