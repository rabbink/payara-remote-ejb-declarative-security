In Payara 4.1.2.174 calling a @PermitAll annotated business method in an EJB having mixed declarative security annotations (@PermitAll for one method and @RolesAllowed for another), 
via its remote interface. Results in an java.rmi.AccessException see: https://github.com/payara/Payara/issues/2223

This 'simple' project reproduces this behaviour. Different JVM processes are required, 
one running the ear deployment and one performing the actual remote bean call. 
Run this from the command line, as your IDE might use its own TestRunner ignoring the maven-surefire-plugin forking settings.

In the project root execute `mvn package` to let maven create all artifacts and run the tests in module `payara-test`.

Test class [RemoteEJBTest](RemoteEJBTest.java) running in 'JVM1' will wait until it gets signalled by test class [DeploymentTest](DeploymentTest.java) running in 'JVM2' when it finishes the ear deployment. I only tested this on Windows 10 / jdk-8u152 x64.
