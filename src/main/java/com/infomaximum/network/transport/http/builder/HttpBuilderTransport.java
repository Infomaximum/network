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

    public static final ConfigUploadFiles DEFAULT_CONFIG_UPLOAD_FILES = new ConfigUploadFiles.Builder().build();

    private Set<BuilderHttpConnector> builderConnectors;

    private Class classWebMvcConfig;
    private ErrorHandler errorHandler;//jetty 12 migration to Request.Processor errorProcessor

    private Set<BuilderFilter> filters;
    private Set<HttpChannel.Listener> httpChannelListeners;

    private ConfigUploadFiles configUploadFiles;
    
    public HttpBuilderTransport(Class classWebMvcConfig) {
        this.classWebMvcConfig = classWebMvcConfig;
        this.configUploadFiles = DEFAULT_CONFIG_UPLOAD_FILES;
    }

    public HttpBuilderTransport addConnector(BuilderHttpConnector builderConnector){
        if (builderConnectors==null) {
            builderConnectors = new HashSet<>();
        }
        builderConnectors.add(builderConnector);
        return this;
    }

    public HttpBuilderTransport withErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public HttpBuilderTransport addFilter(BuilderFilter filterItem){
        if (filters==null) {
            filters = new HashSet<>();
        }
        filters.add(filterItem);
        return this;
    }

    public HttpBuilderTransport withConfigUploadFiles(ConfigUploadFiles config) {
        this.configUploadFiles = config;
        return this;
    }

    public HttpBuilderTransport addListener(HttpChannel.Listener listener){
        if (httpChannelListeners==null) {
            httpChannelListeners = new HashSet<>();
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
    public ErrorHandler getErrorHandler() {//jetty 12 migration to: public  Request.Processor getErrorProcessor()
        return errorHandler;
    }
    public Set<BuilderFilter> getFilters() {
        return filters;
    }

    public ConfigUploadFiles getConfigUploadFiles() {
        return configUploadFiles;
    }

    public Set<HttpChannel.Listener> getHttpChannelListeners() {
        return httpChannelListeners;
    }
}
