package mobi.oneway.sd.core.loader.infos;

import android.os.Parcelable;


public abstract class PluginComponentInfo implements Parcelable {
    private final String className;

    public final String getClassName() {
        return this.className;
    }

    public PluginComponentInfo(String className) {
        this.className = className;
    }
}
