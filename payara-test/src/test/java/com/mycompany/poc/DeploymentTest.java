package com.mycompany.poc;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class DeploymentTest extends ForkedJVMMessageReceiver {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentTest.class);

    /**
     * This will trigger deployment by Arquillian and the actual test performed in another JVM. Different (forked) JVMs are required.
     * JGroups is used for inter-process communication, ie. synchronizing this test (JMV2) and the other (JVM1).
     *
     * @throws Exception
     */
    @org.junit.Test
    public void run() throws Exception {
        logger.info(" ##### Running in (ARQ) JVM with processId: {}", getProcessId("UNKNOWN"));

        JChannel channel = new JChannel();
        channel.connect(clusterName);
        channel.setReceiver(this);

        // Trigger remote JVM to continue, and run the actual test.
        // Just consider the first member to be that 'other' JVM
        channel.send(new Message(channel.getView().getMembers().get(0), "continue"));

        try {
            while (!proceed) {
                lock.lock();
                try {
                    logger.info(" ##### (ARQ) JVM with processId: {} waiting for remote JVM", getProcessId("UNKNOWN"));
                    notReady.await();
                } catch (InterruptedException e) {
                }
            }
        } finally {
            lock.unlock();

            channel.close();
        }
    }

    // Client mode as this test is expected to run in this JVM
    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {
        logger.info(" #### Creating deployment in (ARQ) JVM with processId: {}", getProcessId("UNKNOWN"));
        return ShrinkWrap.createFromZipFile(
                EnterpriseArchive.class, new File("../payara-ear/target/payara-ear-1.0-SNAPSHOT.ear"));
    }

}
