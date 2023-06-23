package com.infomaximum.network.transport.http.builder;

import com.infomaximum.network.builder.BuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.filter.BuilderFilter;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kris on 29.08.16.
 */
public class HttpBuilderTransport extends BuilderTransport {

    public static final ConfigUploadFiles DEFAULT_CONFIG_UPLOAD_FILES = new ConfigUploadFiles.Builder().build();

    private Set<BuilderHttpConnector> builderConnectors;

    private final ServletHolder servletHolder;

    private ErrorHandler errorHandler;//jetty 12 migration to Request.Processor errorProcessor

    private Set<String> compressResponseMimeTypes;
    private Set<BuilderFilter> filters;
    private String cors;
    private Set<HttpChannel.Listener> httpChannelListeners;

    public HttpBuilderTransport(Class classWebMvcConfig) {
        this(classWebMvcConfig, DEFAULT_CONFIG_UPLOAD_FILES);
    }
    
    public HttpBuilderTransport(Class classWebMvcConfig, ConfigUploadFiles configUploadFiles) {
        this(createServletHolder(classWebMvcConfig, configUploadFiles));
    }

    public HttpBuilderTransport(ServletHolder servletHolder) {
        this.servletHolder = servletHolder;

        addCompressResponseMimeType("text/html");
        addCompressResponseMimeType("application/x-font-ttf");
        addCompressResponseMimeType("text/css");
        addCompressResponseMimeType("application/javascript");
        addCompressResponseMimeType("application/json");
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

    public HttpBuilderTransport addCompressResponseMimeType(String mimeType){
        if (compressResponseMimeTypes == null) {
            compressResponseMimeTypes = new HashSet<>();
        }
        compressResponseMimeTypes.add(mimeType);
        return this;
    }

    public HttpBuilderTransport addFilter(BuilderFilter filterItem){
        if (filters==null) {
            filters = new HashSet<>();
        }
        filters.add(filterItem);
        return this;
    }

    /**
     * Set support enable/disable Cross-Origin Resource Sharing (CORS)
     * @param value
     * @return
     */
    public HttpBuilderTransport setCORS(String value){
        this.cors = value;
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
    public ServletHolder getServletHolder() {
        return servletHolder;
    }
    public ErrorHandler getErrorHandler() {//jetty 12 migration to: public  Request.Processor getErrorProcessor()
        return errorHandler;
    }

    public Set<String> getCompressResponseMimeTypes() {
        return compressResponseMimeTypes;
    }

    public Set<BuilderFilter> getFilters() {
        return filters;
    }

    public String getCORS() {
        return cors;
    }

    public Set<HttpChannel.Listener> getHttpChannelListeners() {
        return httpChannelListeners;
    }

    private static ServletHolder createServletHolder(Class classWebMvcConfig, ConfigUploadFiles config){
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(classWebMvcConfig);
        ServletHolder servletHolder = new ServletHolder("default", new DispatcherServlet(applicationContext));

        if (config != null) {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                    config.getLocation().toAbsolutePath().toString(),
                    config.getMaxFileSize(),
                    config.getMaxRequestSize(),
                    config.getFileSizeThreshold()
            );
            servletHolder.getRegistration().setMultipartConfig(multipartConfigElement);
        }

        return servletHolder;
    }

}
