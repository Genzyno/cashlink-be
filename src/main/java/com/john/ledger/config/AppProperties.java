package com.john.ledger.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the application under the 'app' prefix.
 * This class helps resolve "unknown property" warnings in the IDE.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Google google = new Google();
    private final Cors cors = new Cors();
    private final Otp otp = new Otp();
    private final Upload upload = new Upload();
    private final Invite invite = new Invite();
    private String frontendUrl;

    @Getter
    @Setter
    public static class Invite {
        private int expirationDays;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessExpirationSec;
        private long refreshExpirationSec;
    }

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String additionalAudiences;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
        private long maxAge;
    }

    @Getter
    @Setter
    public static class Otp {
        private int length;
        private int expirationMin;
        private int rateLimitPerEmailPerMin;
    }

    @Getter
    @Setter
    public static class Upload {
        private String dir;
    }
}
