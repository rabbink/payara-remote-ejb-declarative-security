package com.mycompany.poc;

import java.lang.management.ManagementFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ForkedJVMMessageReceiver extends ReceiverAdapter {

    private final Logger logger = LoggerFactory.getLogger(RemoteEJBTest.class);

    final String clusterName = "ForkedJVMs";

    final Lock lock = new ReentrantLock();
    final Condition notReady  = lock.newCondition();

    volatile boolean proceed;

    static String getProcessId(final String fallback) {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return fallback;
        }

        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            // ignore
        }
        return fallback;
    }

    @Override
    public void receive(Message msg) {
        logger.info(msg.getSrc() + ": " + msg.getObject());

        lock.lock();
        try {
            proceed = true;
            notReady.signal();
        } finally {
            lock.unlock();
        }
    }

}
