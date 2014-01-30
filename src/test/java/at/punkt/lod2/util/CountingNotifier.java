package at.punkt.lod2.util;

import eu.lod2.rsine.dissemination.notifier.INotifier;

import java.util.Collection;

public class CountingNotifier implements INotifier {

    private int notificationCount = 0;

    @Override
    public void notify(Collection<String> messages) {
        notificationCount++;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

}
