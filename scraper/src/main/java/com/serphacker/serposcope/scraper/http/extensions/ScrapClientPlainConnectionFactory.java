/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serphacker.serposcope.scraper.http.extensions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

public class ScrapClientPlainConnectionFactory implements ConnectionSocketFactory {
    
    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("proxy.socks");
        
        if (socksaddr != null) {
            return new Socket(new Proxy(Proxy.Type.SOCKS, socksaddr));
        } else {
            return new Socket();
        }
    }

    @Override
    public Socket connectSocket(
        final int connectTimeout,
        final Socket socket,
        final HttpHost host,
        final InetSocketAddress remoteAddress,
        final InetSocketAddress localAddress,
        final HttpContext context) throws IOException, ConnectTimeoutException {

        Socket sock;
        if (socket != null) {
            sock = socket;
        } else {
            sock = createSocket(context);
        }
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            sock.connect(remoteAddress, connectTimeout);
        } catch (SocketTimeoutException ex) {
            throw new ConnectTimeoutException(ex, host, remoteAddress.getAddress());
        }
        return sock;
    }

}
