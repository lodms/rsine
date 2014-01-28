package at.punkt.lod2.util;

import eu.lod2.rsine.dissemination.notifier.INotifier;

import java.util.Collection;

public class CountingNotifier implements INotifier {

    private int notificationCount = 0;

    @Override
    public synchronized  void notify(Collection<String> messages) {
        notificationCount++;
    }

    public synchronized  int getNotificationCount() {
        return notificationCount;
    }

    /*
    public int waitForNotification() {
        return waitForNotification(1, Long.MAX_VALUE);
    }

    public int waitForNotificationMaxTime(long maxMillis) {
        return waitForNotification(1, maxMillis);
    }

    // waits for notification until either expectedCount is reached or maxMillis have passed
    private int waitForNotification(int expectedCount, long maxMillis) {
        long start = System.currentTimeMillis();
        while (notificationCount < expectedCount) {
            try {
                Thread.sleep(200);
                if ((System.currentTimeMillis() - start) > maxMillis) {
                    break;
                }
            }
            catch (InterruptedException e) {
            }
            Thread.yield();
        }
        int count = notificationCount;
        notificationCount = 0;
        return count;
    }
    */

}
