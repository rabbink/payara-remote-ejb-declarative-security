package com.mycompany.poc;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

@Stateless
@DeclareRoles("user")
public class HelloServiceBean implements HelloServiceLocal, HelloServiceRemote {

    @Resource
    private SessionContext sc;

    @PermitAll
    @Override
    public String sayHello() {
        return "Hello " + sc.getCallerPrincipal().getName() + "!";
    }

    @RolesAllowed("user")
    @Override
    public String secureSayHello() {
        return "Hello " + sc.getCallerPrincipal().getName() + "!";
    }

}
