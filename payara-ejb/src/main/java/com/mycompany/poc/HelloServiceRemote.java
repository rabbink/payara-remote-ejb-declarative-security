package com.mycompany.poc;

import javax.ejb.Remote;

@Remote
public interface HelloServiceRemote {

    String sayHello();

    String secureSayHello();
}
