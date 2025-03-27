package com.infomaximum.network.transport.http.builder.filter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import java.util.EnumSet;

/**
 * Created by kris on 15.06.17.
 */
public class BuilderFilter {

    public final Class<? extends Filter> filterClass;
    public final String pathSpec;
    public final EnumSet<DispatcherType> dispatches;
    public Filter filter;

    public BuilderFilter(Class<? extends Filter> filterClass, String pathSpec) {
        this(filterClass, pathSpec, EnumSet.of(DispatcherType.REQUEST));
    }

    public BuilderFilter(Filter filter, String pathSpec) {
        this(filter.getClass(), pathSpec, EnumSet.of(DispatcherType.REQUEST));
        this.filter = filter;
    }

    public BuilderFilter(Class<? extends Filter> filterClass, String pathSpec, EnumSet<DispatcherType> dispatches) {
        this.filterClass = filterClass;
        this.pathSpec = pathSpec;
        this.dispatches = dispatches;
    }

    public boolean isInstance() {
        return filter != null;
    }
}
