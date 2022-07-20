/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bsds.chapter4.rmi;
import java.rmi.*;  
/**
 *
 * @author igortn
 */
public class MyBankClient {
    

    public static void main(String args[]){  
    try{  
       MyBank bankServer=(MyBank)Naming.lookup("rmi://localhost:1099/MyBankServer");  
       System.out.println("connecting to server");
       System.out.println(bankServer.balance("00169990"));  
    }catch(Exception e){
      e.printStackTrace();
    }  
    }  
}  
    
