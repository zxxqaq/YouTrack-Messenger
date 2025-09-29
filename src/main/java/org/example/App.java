package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        // load .env
        Dotenv dotenv = Dotenv.load();
        String ytBase = dotenv.get("YT_BASE_URL");
        String ytToken = dotenv.get("YT_TOKEN");
        String tgToken = dotenv.get("TG_BOT_TOKEN");
        String tgGroupChatId = dotenv.get("TG_GROUP_CHAT_ID");
        String tgPmChatId = dotenv.get("TG_PM_CHAT_ID");

        if (ytBase == null || ytToken == null) {
            System.err.println("[Config] Missing YT_BASE_URL or YT_TOKEN in .env");
            return;
        }
        if (tgToken == null || tgGroupChatId == null || tgPmChatId == null) {
            System.err.println("[Config] Missing TG_BOT_TOKEN or TG_GROUP_CHAT_ID or TG_PM_CHAT_ID in .env ");
        }

        // initialize the clients
        YouTrackClient yt = new YouTrackClient(ytBase, ytToken);
//        TelegramClient tg = (tgToken != null && tgGroupChatId != null)
//                ? new TelegramClient(tgToken, tgGroupChatId)
//                : null;

        // timed task executor
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "yt-poller");
            t.setDaemon(true);
            return t;
        });

        Runnable pollTask = () -> {
            try {
                List<YouTrackClient.Notification> list = yt.fetchNotifications(20);
                //TODO: filter the already sent notifications
//                List<YouTrackClient.Notification> newOnes = yt.filterUnsent(list);

//                if (newOnes.isEmpty()) {
//                    System.out.println("[Poll] No new notifications.");
//                    return;
//                }

                for (YouTrackClient.Notification n : list) {
                    String msg = Formatter.toTelegramMarkdown(n);
//                    if (tg != null) {
//                        tg.sendMarkdown(msg);
//                        System.out.println("[Sent TG] " + n.id);
//                    } else {
//                        System.out.println("[DryRun] " + msg);
//                    }
                }
            } catch (Exception e) {
                System.err.println("[Poll error] " + e.getMessage());
            }
        };

        // immediate run
        pollTask.run();
        // then every 10 minutes
        ses.scheduleAtFixedRate(pollTask, 10, 10, TimeUnit.MINUTES);

        //shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ses.shutdown();
            try {
                if (!ses.awaitTermination(5, TimeUnit.SECONDS)) {
                    ses.shutdownNow();
                }
            } catch (InterruptedException ignored) {}
            System.out.println("[Shutdown] Scheduler stopped.");
        }));

        // keep main thread alive
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }
}
