package mobi.oneway.sd.core.loader.infos;

import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ManifestInfo {

    private final List<Receiver> receivers;

    public ManifestInfo() {
        this.receivers = new ArrayList();
    }

    public final List<Receiver> getReceivers() {
        return this.receivers;
    }

    public static final class Receiver {
        private final String name;
        private List<ReceiverIntentInfo> intents;

        public Receiver(String name) {
            this.name = name;
            this.intents = new ArrayList();
        }

        public final String getName() {
            return this.name;
        }

        public final List<ReceiverIntentInfo> getIntents() {
            return this.intents;
        }

        public final List<String> actions() {
            List<String> actions = new ArrayList();
            for (ReceiverIntentInfo intentInfo : intents) {
                Iterator<String> iterator = intentInfo.actionsIterator();
                while (iterator.hasNext()) {
                    actions.add(iterator.next());
                }
            }
            return actions;
        }
    }

    public static final class ReceiverIntentInfo
            extends IntentFilter {
    }
}
