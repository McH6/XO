package com.mch.xoClient;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.mch.xoClient.view.ClientView;


public class Main {
  
  private static ApplicationContext context;
  
  
	public static void main(String[] args) {
	  context = new AnnotationConfigApplicationContext(ComponentScanningAppConfig.class);
	  
	  // run the View
    context.getBean(ClientView.class);
	}
	
}
