package org.littleshoot.proxy.impl;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import sun.misc.BASE64Encoder;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Queue;

/**
 * created by yuanyi0510 at 16:18 2019-07-11
 **/
public class VipChainedProxyManager implements ChainedProxyManager {
    private static String proStr;

    public VipChainedProxyManager(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
        lookupChainedProxies(httpRequest,chainedProxies);
    }

    @Override
    public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
        chainedProxies.add(new ChainedProxyAdapter(){
            String[] usernameAndHostSplit = proStr.split("@");
            String hostPortStr = usernameAndHostSplit[1];
            String host = hostPortStr.split(":")[0];
            String port = hostPortStr.split(":")[1];
            @Override
            public InetSocketAddress getChainedProxyAddress() {
                return new InetSocketAddress(host, Integer.parseInt(port));
            }

            @Override
            public void filterRequest(HttpObject httpObject) {
                if (httpObject instanceof DefaultHttpRequest) {
                    DefaultHttpRequest defaultHttpRequest = (DefaultHttpRequest) httpObject;
                    HttpHeaders httpHeaders = defaultHttpRequest.headers();
                    httpHeaders.add(HttpHeaders.Names.PROXY_AUTHORIZATION, "BASIC "+getBase64(usernameAndHostSplit[0]));
                    try {
                        Field fields = DefaultHttpRequest.class.getSuperclass().getDeclaredField("headers");
                        fields.setAccessible(true);
                        fields.set(httpObject, httpHeaders);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
    }

    private static String getBase64(String str){
        BASE64Encoder encoder = new BASE64Encoder();
        String data = encoder.encode(str.getBytes());
        System.out.println("BASE64加密：" + data);
        return data;
    }
}
