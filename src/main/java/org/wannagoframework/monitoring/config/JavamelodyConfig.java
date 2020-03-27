package org.wannagoframework.monitoring.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.wannagoframework.commons.utils.HasLogger;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/19/19
 */
@Configuration
public class JavamelodyConfig implements HasLogger {

  private final Environment env;

  public JavamelodyConfig(Environment env) {
    this.env = env;
  }

  @PostConstruct
  public boolean init() {
    String loggerPrefix = getLoggerPrefix("init");

    String applicationName =
        env.getProperty("spring.application.name");

    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }
    String serverPort = env.getProperty("management.server.port");
    String contextPath = env.getProperty("server.servlet.context-path");
    if (StringUtils.isBlank(contextPath)) {
      contextPath = "/";
    }
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      logger().warn(
          loggerPrefix + "The host name could not be determined, using `localhost` as fallback");
    }

    String localUrl = String.format("%s://user:none@%s:%s%sactuator", protocol,
        hostAddress,
        serverPort,
        contextPath);

    String collectUrl = env.getProperty("COLLECT_URL");
    logger().debug(loggerPrefix + "Application name = " + applicationName);
    logger().debug(loggerPrefix + "Collect URL = " + collectUrl);
    logger().debug(loggerPrefix + "Local URL = " + localUrl);

    if (collectUrl != null && localUrl != null) {
      try {
        URL collectServerUrl = new URL(collectUrl);
        URL applicationNodeUrl = new URL(localUrl);
        net.bull.javamelody.MonitoringFilter
            .registerApplicationNodeInCollectServer(applicationName, collectServerUrl,
                applicationNodeUrl);
      } catch (MalformedURLException e) {
        logger().error(loggerPrefix + "Malformed URL : " + e.getMessage(), e);
      }
    } else {
      logger().warn(loggerPrefix
          + "COLLECT_URL or LOCAL_URL missing, no automatic registering to collect server");
    }
    return true;
  }

  @PreDestroy
  public void destroy() {
    String loggerPrefix = getLoggerPrefix("destroy");
    try {
      net.bull.javamelody.MonitoringFilter.unregisterApplicationNodeInCollectServer();
    } catch (IOException e) {
      logger().error(
          loggerPrefix + "Error while un-registering from collect server : " + e.getMessage(), e);
    }
  }

}
