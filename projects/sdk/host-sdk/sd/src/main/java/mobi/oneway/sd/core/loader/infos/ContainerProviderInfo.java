package mobi.oneway.sd.core.loader.infos;

public class ContainerProviderInfo {
    private String className;
    private String authority;

    public final String getClassName() {
        return this.className;
    }

    public final void setClassName(String var1) {
        this.className = var1;
    }

    public final String getAuthority() {
        return this.authority;
    }

    public final void setAuthority(String var1) {
        this.authority = var1;
    }

    public ContainerProviderInfo(String className, String authority) {
        this.className = className;
        this.authority = authority;
    }
}

