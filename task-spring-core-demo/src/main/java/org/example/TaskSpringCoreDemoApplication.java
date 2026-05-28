package org.example;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.example.config.web.WebConfig;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;


/**
 * Main application entry point for embedded Tomcat runtime.
 */
@Slf4j
public class TaskSpringCoreDemoApplication {

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        Context context = tomcat.addContext("", null);

        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(WebConfig.class);

        webContext.setServletContext(context.getServletContext());
        webContext.refresh();

        DispatcherServlet dispatcherServlet = new DispatcherServlet(webContext);

        // Register SecurityFilter
        DelegatingFilterProxy securityFilter = new DelegatingFilterProxy("AuthenticationFilter", webContext);
        FilterDef securityFilterDef = createFilterDef("securityFilter", securityFilter);
        context.addFilterDef(securityFilterDef);

        FilterMap securityFilterMap = new FilterMap();
        securityFilterMap.setFilterName("securityFilter");
        securityFilterMap.addURLPattern("/*");
        context.addFilterMap(securityFilterMap);

        // Register TraceFilter
        DelegatingFilterProxy traceFilter = new DelegatingFilterProxy("TraceFilter", webContext);
        FilterDef traceFilterDef = createFilterDef("traceFilter", traceFilter);
        context.addFilterDef(traceFilterDef);

        FilterMap txFilterMap = new FilterMap();
        txFilterMap.setFilterName("traceFilter");
        txFilterMap.addURLPattern("/*");
        context.addFilterMap(txFilterMap);

        // Register RestCallLoggingFilter via DelegatingFilterProxy
        DelegatingFilterProxy restFilter = new DelegatingFilterProxy("RestCallLoggingFilter", webContext);
        FilterDef restFilterDef = createFilterDef("restCallLoggingFilter", restFilter);
        context.addFilterDef(restFilterDef);

        FilterMap restFilterMap = new FilterMap();
        restFilterMap.setFilterName("restCallLoggingFilter");
        restFilterMap.addURLPattern("/*");
        restFilterMap.setDispatcher(DispatcherType.REQUEST.name());
        context.addFilterMap(restFilterMap);

        Wrapper servlet =
                Tomcat.addServlet(
                        context,
                        "dispatcher",
                        dispatcherServlet
                );

        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");

        tomcat.start();

        log.debug("Started: http://localhost:8080");

        tomcat.getServer().await();
    }

    private static FilterDef createFilterDef(String filterName, Filter filter) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(filterName);
        filterDef.setFilter(filter);
        return filterDef;
    }
}
