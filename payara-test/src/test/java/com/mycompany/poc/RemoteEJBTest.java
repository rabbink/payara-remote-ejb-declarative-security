package com.mycompany.poc;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jgroups.JChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteEJBTest extends ForkedJVMMessageReceiver {

    private final Logger logger = LoggerFactory.getLogger(RemoteEJBTest.class);

    private JChannel channel;

    /**
     * This will execute the actual remote bean call. This needs to be done in another (forked) JVM.
     * JGroups is used for inter-process communication, ie. synchronizing this test (JMV1) and the deployment done by Arquillian (JVM2).
     *
     * @throws Exception
     */
    @Test
    public void callRemoteEJB() throws Exception {
        logger.info(" #### Running in JVM with processId: {}", getProcessId("UNKNOWN"));

        Context ctx = null;
        try {
            ctx = new InitialContext(getProperties());
            HelloServiceRemote bean = (HelloServiceRemote) ctx.lookup("java:global/payara-ear-1.0-SNAPSHOT/payara-ejb-1.0-SNAPSHOT/HelloServiceBean!com.mycompany.poc.HelloServiceRemote");
            System.out.println(bean.sayHello());
        } finally {
            if (ctx != null)
                ctx.close();
        }

    }

    @Before
    public void startAndWait() throws Exception {
        logger.info(" #### Running in JVM with processId: {}", getProcessId("UNKNOWN"));

        channel = new JChannel();
        channel.connect(clusterName);
        channel.setReceiver(this);

        lock.lock();
        try {
            while (!proceed) {
                try {
                    // Wait for ARQ JVM to do actual ear deployment
                    logger.info(" #### JVM waiting for ARQ JVM");
                    notReady.await();
                } catch (InterruptedException e) {
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @After
    public void cleanup() throws Exception {
        // Trigger remote (ARQ) JVM to continue, to finish its test run and tear down the deployment.
        // Just consider the second member to be that 'other' JVM.
        try {
            channel.send(channel.getView().getMembers().get(1), "continue");
        } finally {
            channel.close();
        }
    }

    private Properties getProperties() {
        Properties properties = new Properties();

//        properties.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");

//        properties.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
//        properties.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");

        properties.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        properties.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

//        properties.setProperty("com.sun.corba.ee.transport.ORBWaitForResponseTimeout", "5000");
//        properties.setProperty("com.sun.corba.ee.transport.ORBTCPConnectTimeouts", "100:500:100:500");
//        properties.setProperty("com.sun.corba.ee.transport.ORBTCPTimeouts", "500:2000:50:1000");

        return properties;
    }

}