package configuration.appcontainer;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;

import java.util.HashMap;
import java.util.Map;

public enum AppContainerStartParameters {
    EMPTY,
    DEFAULT_STUDIO_PARAMS,
    SAML_STUDIO_PARAMS,
    OAUTH_STUDIO_PARAMS,
    SERVICE_PARAMS;

    public Map<String, String> getParameterMap() {
        Map<String, String> config = new HashMap<>();
        switch (this) {
            case EMPTY:
                config.put("JAVA_OPTS", "-Xms32m -XX:MaxRAMPercentage=50.0");
                break;
            case SAML_STUDIO_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("security.saml.local-certificate", "MIIE+zCCAuOgAwIBAgIGAYkGsTnzMA0GCSqGSIb3DQEBCwUAMBQxEjAQBgNVBAMMCXdlYnN0dWRpbzAeFw0yMzA2MjcxMDI3MDVaFw0zMzA2MjYxMDI3MDVaMBQxEjAQBgNVBAMMCXdlYnN0dWRpbzCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAISZnphnpon/GmCOWNQmife+N23/qendOULVcwK+H2wtmcAw2Jmc/MH3Kh/ZDAHZ4AtfxqTza6HTzK1a6isZUSMicC4BFOYH7mprNhCg4U59FNaoF+PwkYbl6M8SglHuk5m5Gp2d/A34tqyGmM+IQ4WaWY8HNCCa3uJxmhit/uLa+TR0+a5KW3N5+YRT9A8j1pyPpncAGDx30u+JCPZXqVNN0EwXndWJ0pBiSnnoKfF1mhJSdYrCS+eq5eFPaY+FDqFDWw/OtrIqWiwEZhXv3O3BNQktuVby4Way37RVOGuHsOCBEbF0O0lxmVUsJvVSLcijENBaqXDluMMmgYc8Cw3eK/JKUro62qPj3vlh6A4Bs666jAhkB2Fqz53gFlD/Zn7j5FqQdPK+E/tL9ocoXy471QrgqzT3wTs8P/RgOlBMR26Y7f4FD7F+o1/klIyHt8BsuhgIAGFqeZVBsxl3u5+VpFXH/uI/pl2wZOCz31CjYbj+vHHxWJvf1G5Brd2CjkxymPPkoRlbdkjh0AXxGBnB/RuVARwmpym2Leq40zjVwuSXZJGOBxLOfw3rFOcm+8mFTv8mvxEcNwNPYDiSP7V4Fp3sPJsv2Zj8ouc1NqZ+IS5FBimu2U6djTp2JyYR0eo0xl2H8tZEBuy1uzjcNVDC9D3OEaUp0n3Rc/ICcTXdAgMBAAGjUzBRMB0GA1UdDgQWBBRlTJMBUgkKSXyKmWohnXP+4FAURjAfBgNVHSMEGDAWgBRlTJMBUgkKSXyKmWohnXP+4FAURjAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4ICAQAptRTpPwO4UdjNYXXt3C4DgsfKiNwFeoz8VroLIE3bk/PjRRZxWViv4H9wFBG3yMUXSkVj8a7brDSCaKgLoA30wp6LdtJPnNOLOTCdZ93URTIouTEChAZmTcnUHza1wZa8jWTRkw8PvpkkX1DGAGV6nn9PJdenGIYMvGIRF6q9wLrUDQtchNAErSV30H5t8L9xdcmqdVb7vPl4zbMywrVIiQ3d16CwbnRhr5GHKCtW/eBcNc1oTvBJnbNnH3BKTTeLNu96w/xzf6QjT3eZMI98WR5wpW64kYwI0qOxOJTzedpEJB7xqiKK7SOY/iMh/cBNJpOn5LAtdkUotkDJQZxDULaZ76XiRRFx50KNSRmdEqGYoEPIGhAvylmNZoepSH3C1Klwi58/plL5YaIbTB0xbhaHl55OCekXRbk0OWT7/ch76tDwxY9KS4u4Oa3mDiHaLdk/FfOy4cJobMvg9G9jyn18hZm8POuFuo+IM9d0JqUSRNfkPj1YUD1BcdQKu2Q85jPu3vG2eG/4nei1AaEo2qadb7g06mh1Olhsvyz14vOUesw20Ek0flFFjKc9xaz5A+zOFd2pA8DvzTGatDzfa+dLROn+VUhVUC61vOjaI1Cvo/GXyllGXVAiIk6AV8kzp7zAlbVwHG4EuLK68rVQ/t9eTEoH9N9/BFl4cpLl9Q==");
                config.put("security.saml.local-key", "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");
                break;
            case OAUTH_STUDIO_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                config.put("user.mode", "oauth2");
                config.put("security.oauth2.client-id", "Oauth2TestClientName");
                config.put("security.oauth2.issuer-uri", "http://192.168.210.152:8088/auth/realms/test_realm");
                config.put("security.oauth2.client-secret", "650db240-09ed-4136-a610-e6926d2648d3");
                config.put("security.administrators", "openl_ac");
                config.put("security.oauth2.scope", "openid,profile");
                config.put("security.default-group", "Authenticated");
                break;
            case SERVICE_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("ruleservice.deployer.enabled", "true");
                config.put("production-repository.factory", "repo-git");
                config.put("production-repository.uri", ProjectConfiguration.getProperty(PropertyNameSpace.GIT_URL_RULESERVICE));
                config.put("production-repository.local-repository-path", "/opt/openl/local");
                config.put("production-repository.listener-timer-period", "10");
                config.put("production-repository.branch", "main");
                config.put("production-repository.tag-prefix", "Rules_");
                config.put("production-repository.login", ProjectConfiguration.getProperty(PropertyNameSpace.GIT_LOGIN_RULESERVICE));
                config.put("production-repository.password", ProjectConfiguration.getProperty(PropertyNameSpace.GIT_TOKEN_RULESERVICE));
            case DEFAULT_STUDIO_PARAMS:
            default:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                config.put("user.mode", "multi");
                config.put("security.administrators", "admin");
                config.put("repository.production1.$ref", "repo-jdbc");
                config.put("production-repository-configs", "production1");
                config.put("repository.production1.base.path.$ref", "repo-default.production.base.path");
                config.put("repository.production1.name", "Deployment");
                config.put("repository.production1.comment-template.$ref", "repo-default.production.comment-template");
                config.put("repository.production1.comment-template.use-custom-comments", "false");
                break;
        }
        return config;
    }
}
