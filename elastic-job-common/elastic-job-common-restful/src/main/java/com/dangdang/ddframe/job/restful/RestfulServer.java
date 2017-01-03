/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.restful;

import com.google.common.base.Joiner;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * REST API的内嵌服务器.
 *
 * @author zhangliang
 */
@Slf4j
public final class RestfulServer {
    
    private final Server server;
    
    public RestfulServer(final int port) {
        server = new Server(port);
    }
    
    /**
     * 启动内嵌的RESTful服务器.
     * 
     * @param packages RESTful实现类所在包
     * @throws Exception 启动服务器异常
     */
    public void start(final String packages) throws Exception {
        log.info("Elastic Job: Start RESTful server");
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(getServletHolder(packages), "/*");
        server.start();
    }
    
    /**
     * 启动内嵌的RESTful、Webapp服务器.
     * 
     * @param packages RESTful实现类所在包
     * @param webappRootPath Webapp资源根路径
     * @throws Exception
     */
    public void start(final String packages, final String webappRootPath) throws Exception {
        log.info("Elastic Job: Start RESTful server");
        ServletContextHandler restApiContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        restApiContext.setContextPath("/");
        server.setHandler(restApiContext);
        restApiContext.addServlet(getServletHolder(packages), "/*");
        WebAppContext webappContext = new WebAppContext();
        webappContext.setDescriptor(webappRootPath + "/WEB-INF/web.xml");
        webappContext.setResourceBase(webappRootPath);
        webappContext.setContextPath("/console");
        webappContext.setParentLoaderPriority(true);
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] {restApiContext, webappContext});
        server.setHandler(contexts);
        server.start();
    }
    
    private ServletHolder getServletHolder(final String packages) {
        ServletHolder result = new ServletHolder(ServletContainer.class);
        result.setInitParameter(PackagesResourceConfig.PROPERTY_PACKAGES, Joiner.on(",").join(RestfulServer.class.getPackage().getName(), packages));
        result.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", PackagesResourceConfig.class.getName());
        result.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.scan.providers", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.use.builtin.providers", Boolean.FALSE.toString());
        return result;
    }
    
    /**
     * 安静停止内嵌的RESTful服务器.
     * 
     */
    public void stop() {
        log.info("Elastic Job: Stop RESTful server");
        try {
            server.stop();
            // CHECKSTYLE:OFF
        } catch (final Exception e) {
            // CHECKSTYLE:ON
            log.error("Elastic Job: Stop RESTful server error", e);
        }
    }
}
