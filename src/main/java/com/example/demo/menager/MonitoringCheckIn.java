package com.example.demo.menager;

import com.example.demo.functions.SnmpSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;

public class MonitoringCheckIn implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final String ipAddress;

    public SnmpMenager menager;
    CheckInTask checkInTask = new CheckInTask();

    public MonitoringCheckIn(SnmpMenager menager) throws UnknownHostException {
        this.menager = menager;
        menager.setCommunity("public");
        menager.setPort("161");
        menager.setVersion(SnmpConstants.version2c);

        String hostAddress = "";
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        int port = 161;
        ipAddress = String.format("%s:%s", hostAddress, port);

        LOGGER.info(String.format("Check in initialization started on address %s", ipAddress));

        this.threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        this.threadPoolTaskScheduler.setThreadNamePrefix("monitoring-check-in");
        this.threadPoolTaskScheduler.initialize();

        LOGGER.info("Check in initialization ended.");

        System.out.println("XXXXXXXX");
        checkInTask.run();
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        scheduleCheckIn();
    }

    public void scheduleCheckIn() {
        Instant triggerTime = Instant.now();
        triggerTime = triggerTime.plus(15, ChronoUnit.SECONDS);
        threadPoolTaskScheduler.schedule(new CheckInTask(), Date.from(triggerTime));
    }

    @PreDestroy
    private void shutdown() {
        threadPoolTaskScheduler.shutdown();
    }

    public class CheckInTask implements Runnable {
        private String ipAddress;

        CheckInTask() {

            this.ipAddress = ipAddress;
        }

        @Override
        public void run() {
            Random random1 = new Random();
            Random random2 = new Random();
            int device = random1.nextInt(3);
            int value = random2.nextInt(100);
            String newIp = "10.0.1."+(device+1);
            menager.setIp(newIp);
            menager.set(value,".1.3.6.1.2.1.4.2.0");
            System.out.println(value+"    "+menager.getIp());
            scheduleCheckIn();
        }
    }
}
