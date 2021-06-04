package mobi.oneway.sd.core.loader.managers;

import android.content.ContentProvider;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import mobi.oneway.sd.core.loader.infos.ContainerProviderInfo;
import mobi.oneway.sd.core.loader.infos.PluginParts;
import mobi.oneway.sd.core.loader.infos.PluginProviderInfo;
import mobi.oneway.sd.core.runtime.UriConverter.UriParseDelegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public  class PluginContentProviderManager implements UriParseDelegate {
    private final HashMap providerMap = new HashMap();
    private final HashMap providerAuthorityMap = new HashMap();
    private final HashMap pluginProviderInfoMap = new HashMap();
    private static final String CONTENT_PREFIX = "content://";
    private static final String SHADOW_BUNDLE_KEY = "shadow_cp_bundle_key";

    
    public Uri parse( String uriString) {
        Uri var10;
        if (uriString.startsWith(CONTENT_PREFIX)) {
            int var4 = CONTENT_PREFIX.length();
            String var10000 = uriString.substring(var4);
            String uriContent = var10000;
            int index = uriContent.indexOf("/");
            if (index != -1) {
                byte var6 = 0;
                if (uriContent == null) {
                    throw new ClassCastException("null cannot be cast to non-null type java.lang.String");
                }

                var10000 = uriContent.substring(var6, index);
            } else {
                var10000 = uriContent;
            }

            String originalAuthority = var10000;
            String containerAuthority = this.getContainerProviderAuthority(originalAuthority);
            if (containerAuthority != null) {
                var10 = Uri.parse(CONTENT_PREFIX + containerAuthority + '/' + uriContent);
                return var10;
            }
        }

        var10 = Uri.parse(uriString);
        return var10;
    }

    
    public Uri parseCall( String uriString,  Bundle extra) {
        Uri pluginUri = this.parse(uriString);
        extra.putString(SHADOW_BUNDLE_KEY, pluginUri.toString());
        return pluginUri;
    }

    public final void addContentProviderInfo( String partKey,  PluginProviderInfo pluginProviderInfo,  ContainerProviderInfo containerProviderInfo) {
        Map var4 = (Map)this.providerMap;
        String var5 = pluginProviderInfo.getAuthority();
        boolean var6 = false;
        if (var4 == null) {
            throw new NullPointerException("null cannot be cast to non-null type kotlin.collections.Map<K, *>");
        } else if (var4.containsKey(var5)) {
            throw new RuntimeException("重复添加 ContentProvider");
        } else {
            Map var10000 = (Map)this.providerAuthorityMap;
            String var10001 = pluginProviderInfo.getAuthority();
            var10000.put(var10001, containerProviderInfo.getAuthority());
            HashSet pluginProviderInfos = (HashSet)null;
            if (this.pluginProviderInfoMap.containsKey(partKey)) {
                pluginProviderInfos = (HashSet)this.pluginProviderInfoMap.get(partKey);
            } else {
                pluginProviderInfos = new HashSet();
            }

            if (pluginProviderInfos != null) {
                pluginProviderInfos.add(pluginProviderInfo);
            }

            this.pluginProviderInfoMap.put(partKey, pluginProviderInfos);
        }
    }

    public final void createContentProviderAndCallOnCreate( Context mContext,  String partKey,  PluginParts pluginParts) {
        HashSet var10000 = (HashSet)this.pluginProviderInfoMap.get(partKey);
        if (var10000 != null) {
            Iterable $this$forEach$iv = (Iterable)var10000;
            Iterator var6 = $this$forEach$iv.iterator();

            while(var6.hasNext()) {
                Object element$iv = var6.next();
                PluginProviderInfo it = (PluginProviderInfo)element$iv;
                try {
                    ContentProvider contentProvider = pluginParts.getAppComponentFactory().instantiateProvider((ClassLoader)pluginParts.getClassLoader(), it.getClassName());
                    if (contentProvider != null) {
                        contentProvider.attachInfo(mContext, it.getProviderInfo());
                    }

                    Map var12 = (Map)this.providerMap;
                    String var10001 = it.getAuthority();
                    var12.put(var10001, contentProvider);
                } catch (Exception var11) {
                    throw new RuntimeException("partKey==" + partKey + " className==" + it.getClassName() + " providerInfo==" + it.getProviderInfo(), var11);
                }
            }
        }

    }

    public final ContentProvider getPluginContentProvider( String pluginAuthority) {
        return (ContentProvider)this.providerMap.get(pluginAuthority);
    }

    public final String getContainerProviderAuthority( String pluginAuthority) {
        return (String)this.providerAuthorityMap.get(pluginAuthority);
    }

    
    public final Set getAllContentProvider() {
        boolean var2 = false;
        HashSet contentProviders = new HashSet();
        Set var10000 = this.providerMap.keySet();
        Iterable $this$forEach$iv = (Iterable)var10000;
        Iterator var4 = $this$forEach$iv.iterator();

        while(var4.hasNext()) {
            Object element$iv = var4.next();
            String it = (String)element$iv;
            Object var10001 = this.providerMap.get(it);
            contentProviders.add(var10001);
        }

        return (Set)contentProviders;
    }

    
    public final Uri convert2PluginUri( Uri uri) {
        String containerAuthority = uri.getAuthority();
        Collection var10000 = this.providerAuthorityMap.values();
        if (!var10000.contains(containerAuthority)) {
            throw new IllegalArgumentException("不能识别的uri Authority:" + containerAuthority);
        } else {
            String var4 = uri.toString();
            String uriString = var4;
            Uri var5 = Uri.parse(uriString.replace(containerAuthority+"/",""));
            return var5;
        }
    }

    
    public final Uri convert2PluginUri( Bundle extra) {
        String uriString = extra.getString(SHADOW_BUNDLE_KEY);
        extra.remove(SHADOW_BUNDLE_KEY);
        Uri var10001 = Uri.parse(uriString);
        return this.convert2PluginUri(var10001);
    }
}
