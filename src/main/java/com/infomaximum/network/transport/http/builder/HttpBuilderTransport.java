package com.infomaximum.network.transport.http.builder;

import com.infomaximum.network.builder.BuilderTransport;
import com.infomaximum.network.transport.http.builder.filter.BuilderFilter;
import org.eclipse.jetty.server.handler.ErrorHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kris on 29.08.16.
 */
public class HttpBuilderTransport extends BuilderTransport {

    private InetAddress host;
    private int port;

    private Class classWebMvcConfig;
    private ErrorHandler errorHandler;

    private String jspPath;
    private Set<BuilderFilter> filters;

    public HttpBuilderTransport(int port, Class classWebMvcConfig) {
        this.classWebMvcConfig = classWebMvcConfig;
        this.host = new InetSocketAddress(0).getAddress();
        this.port = port;
    }

    public HttpBuilderTransport withHost(InetAddress host){
        this.host=host;
        return this;
    }

    public HttpBuilderTransport withErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public HttpBuilderTransport withJspPath(String value) {
        this.jspPath=value;
        return this;
    }

    public HttpBuilderTransport withAddFilter(BuilderFilter filterItem){
        if (filters==null) {
            filters = new HashSet<BuilderFilter>();
        }
        filters.add(filterItem);
        return this;
    }

    public InetAddress getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public Class getClassWebMvcConfig() {
        return classWebMvcConfig;
    }
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    public String getJspPath() {
        return jspPath;
    }
    public boolean isSupportJsp(){
        return (jspPath!=null);
    }
    public Set<BuilderFilter> getFilters() {
        return filters;
    }
}
