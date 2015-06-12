package com.mch.xoServer;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mch.xoData.defaults.Defaults;
import com.mch.xoServer.controller.ServerController;
import com.mch.xoServer.controller.impl.ServerControllerImpl;
import com.mch.xoServer.view.ServerViewImpl;


public class Main {
  
  private static ClassPathXmlApplicationContext context;
//  private static AnnotationConfigApplicationContext context2;
  
  public static void main(String[] args) {
//    context2 = new AnnotationConfigApplicationContext(ComponentScanningAppConfig.class);
    context = new ClassPathXmlApplicationContext("META-INF/application-Context.xml");
    // Inject that controller into the view... good
    ServerController controller = context.getBean(ServerControllerImpl.class);
    new ServerViewImpl(Defaults.DEFAULT_PORT, controller);
  }
}
