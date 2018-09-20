package com.infomaximum.network.transport.http.builder;

import com.infomaximum.network.builder.BuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.filter.BuilderFilter;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.handler.ErrorHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kris on 29.08.16.
 */
public class HttpBuilderTransport extends BuilderTransport {

    private Set<BuilderHttpConnector> builderConnectors;

    private Class classWebMvcConfig;
    private ErrorHandler errorHandler;

    private String jspPath;
    private Set<BuilderFilter> filters;
    private Set<HttpChannel.Listener> httpChannelListeners;

    public HttpBuilderTransport(Class classWebMvcConfig) {
        this.classWebMvcConfig = classWebMvcConfig;
    }

    public HttpBuilderTransport addConnector(BuilderHttpConnector builderConnector){
        if (builderConnectors==null) {
            builderConnectors = new HashSet<BuilderHttpConnector>();
        }
        builderConnectors.add(builderConnector);
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

    public HttpBuilderTransport addFilter(BuilderFilter filterItem){
        if (filters==null) {
            filters = new HashSet<BuilderFilter>();
        }
        filters.add(filterItem);
        return this;
    }

    public HttpBuilderTransport addListener(HttpChannel.Listener listener){
        if (httpChannelListeners==null) {
            httpChannelListeners = new HashSet<HttpChannel.Listener>();
        }
        httpChannelListeners.add(listener);
        return this;
    }

    public Set<BuilderHttpConnector> getBuilderConnectors() {
        return builderConnectors;
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
    public Set<HttpChannel.Listener> getHttpChannelListeners() {
        return httpChannelListeners;
    }
}
