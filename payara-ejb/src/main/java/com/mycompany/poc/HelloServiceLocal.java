package com.mycompany.poc;

import javax.ejb.Local;

@Local
public interface HelloServiceLocal {

    String sayHello();

    String secureSayHello();
}
