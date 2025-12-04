package ca.bcit.infosys.liangk.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS application configuration. All REST endpoints are served under the
 * base path "/api".
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
