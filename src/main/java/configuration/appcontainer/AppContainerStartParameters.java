package configuration.appcontainer;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;

import java.util.HashMap;
import java.util.Map;

public enum AppContainerStartParameters {
    EMPTY,
    DEFAULT_STUDIO_PARAMS,
    /**
     * DEFAULT_STUDIO_PARAMS + `security.allow-bypass-protected-branches=true`.
     * Use for tests that need eligible Managers to merge into protected branches without
     * triggering the application-restarting PATCH on /web/admin/settings/authentication.
     */
    STUDIO_BYPASS_ENABLED_PARAMS,
    /**
     * Like STUDIO_BYPASS_ENABLED_PARAMS but the protected matcher is `*EPBDS-15818*`, so BOTH
     * the dev branch (EPBDS-15818_dev) and the release branch (release-EPBDS-15818) are
     * protected. Used by EPBDS-15960 H.6 to assert the "both branches are protected" copy.
     * `master` stays unprotected so the admin REST setup is not blocked.
     */
    STUDIO_BYPASS_BOTH_PROTECTED_PARAMS,
    /**
     * Protected {@code release-**} with the bypass setting left OFF (default). Used by
     * EPBDS-15960 H.8 to assert that an eligible Manager gets the same blocked path as a
     * contributor (no bypass warning, no confirm modal) when the global setting is disabled.
     */
    STUDIO_PROTECTED_NO_BYPASS_PARAMS,
    /**
     * Studio in OIDC (oauth2) mode wired to the ephemeral Keycloak from
     * {@code KeycloakInfrastructureService} (issuer {@code http://keycloak:8080/realms/openlstudio}),
     * with the protected-branch bypass enabled. Used by the PLAYWRIGHT_DOCKER SSO test that
     * verifies a group-derived Manager role (EPBDS-15960 Z.6). Username maps from
     * {@code preferred_username}; the {@code groups} claim becomes the user's group authorities.
     */
    STUDIO_OIDC_BYPASS_PARAMS,
    /**
     * Studio in OIDC (oauth2) mode against the ephemeral Keycloak ({@code KeycloakInfrastructureService}),
     * without the bypass/protected-branch keys. Used by the PLAYWRIGHT_DOCKER test for the Admin
     * 'Users' view with an OAuth2 external user management system (IPBQA-32789).
     */
    STUDIO_OIDC_PARAMS,
    /**
     * Studio in SAML mode against the ephemeral Keycloak SAML client {@code webstudio}
     * (IdP metadata at {@code /realms/openlstudio/protocol/saml/descriptor}). Used by the
     * PLAYWRIGHT_DOCKER test for the Admin 'Users' view with a SAML external user management
     * system (IPBQA-32788). NameID = username; email/first/last come from SAML attribute mappers.
     */
    STUDIO_SAML_PARAMS,
    /**
     * Studio in Active Directory (LDAP) mode against the ephemeral Samba AD DC
     * ({@code SambaAdInfrastructureService}, {@code ldap://samba:389}, domain {@code openl.local}).
     * Form-based login; the AD user {@code studioadmin} is the Studio admin. Used by the
     * PLAYWRIGHT_DOCKER AD auth test (login / logout / user switch).
     */
    STUDIO_AD_PARAMS,
    /**
     * Studio in single-user mode ({@code user.mode=single}): the app auto-authenticates every request
     * as {@code security.single.username} with no login form. Used by the single-mode auth UI test.
     */
    STUDIO_SINGLE_PARAMS,
    SINGLE_USER_STUDIO_PARAMS,
    DEPLOY_STUDIO_PARAMS,
    STUDIO_GIT,
    SERVICE_PARAMS,
    SERVICE_FILE_PARAMS,
    STUDIO_CENTRAL_GROUP_1_PARAMS,
    STUDIO_CENTRAL_GROUP_2_PARAMS;

    private static final String STUDIO_CENTRAL_BASE = "https://dev2eisgengit02.exigengroup.com/gitlab/genesis/";

    // Self-signed SP keypair (CN=webstudio) for the SAML relying party in STUDIO_SAML_PARAMS.
    private static final String SAML_LOCAL_CERTIFICATE = "MIIE+zCCAuOgAwIBAgIGAYkGsTnzMA0GCSqGSIb3DQEBCwUAMBQxEjAQBgNVBAMMCXdlYnN0dWRpbzAeFw0yMzA2MjcxMDI3MDVaFw0zMzA2MjYxMDI3MDVaMBQxEjAQBgNVBAMMCXdlYnN0dWRpbzCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAISZnphnpon/GmCOWNQmife+N23/qendOULVcwK+H2wtmcAw2Jmc/MH3Kh/ZDAHZ4AtfxqTza6HTzK1a6isZUSMicC4BFOYH7mprNhCg4U59FNaoF+PwkYbl6M8SglHuk5m5Gp2d/A34tqyGmM+IQ4WaWY8HNCCa3uJxmhit/uLa+TR0+a5KW3N5+YRT9A8j1pyPpncAGDx30u+JCPZXqVNN0EwXndWJ0pBiSnnoKfF1mhJSdYrCS+eq5eFPaY+FDqFDWw/OtrIqWiwEZhXv3O3BNQktuVby4Way37RVOGuHsOCBEbF0O0lxmVUsJvVSLcijENBaqXDluMMmgYc8Cw3eK/JKUro62qPj3vlh6A4Bs666jAhkB2Fqz53gFlD/Zn7j5FqQdPK+E/tL9ocoXy471QrgqzT3wTs8P/RgOlBMR26Y7f4FD7F+o1/klIyHt8BsuhgIAGFqeZVBsxl3u5+VpFXH/uI/pl2wZOCz31CjYbj+vHHxWJvf1G5Brd2CjkxymPPkoRlbdkjh0AXxGBnB/RuVARwmpym2Leq40zjVwuSXZJGOBxLOfw3rFOcm+8mFTv8mvxEcNwNPYDiSP7V4Fp3sPJsv2Zj8ouc1NqZ+IS5FBimu2U6djTp2JyYR0eo0xl2H8tZEBuy1uzjcNVDC9D3OEaUp0n3Rc/ICcTXdAgMBAAGjUzBRMB0GA1UdDgQWBBRlTJMBUgkKSXyKmWohnXP+4FAURjAfBgNVHSMEGDAWgBRlTJMBUgkKSXyKmWohnXP+4FAURjAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4ICAQAptRTpPwO4UdjNYXXt3C4DgsfKiNwFeoz8VroLIE3bk/PjRRZxWViv4H9wFBG3yMUXSkVj8a7brDSCaKgLoA30wp6LdtJPnNOLOTCdZ93URTIouTEChAZmTcnUHza1wZa8jWTRkw8PvpkkX1DGAGV6nn9PJdenGIYMvGIRF6q9wLrUDQtchNAErSV30H5t8L9xdcmqdVb7vPl4zbMywrVIiQ3d16CwbnRhr5GHKCtW/eBcNc1oTvBJnbNnH3BKTTeLNu96w/xzf6QjT3eZMI98WR5wpW64kYwI0qOxOJTzedpEJB7xqiKK7SOY/iMh/cBNJpOn5LAtdkUotkDJQZxDULaZ76XiRRFx50KNSRmdEqGYoEPIGhAvylmNZoepSH3C1Klwi58/plL5YaIbTB0xbhaHl55OCekXRbk0OWT7/ch76tDwxY9KS4u4Oa3mDiHaLdk/FfOy4cJobMvg9G9jyn18hZm8POuFuo+IM9d0JqUSRNfkPj1YUD1BcdQKu2Q85jPu3vG2eG/4nei1AaEo2qadb7g06mh1Olhsvyz14vOUesw20Ek0flFFjKc9xaz5A+zOFd2pA8DvzTGatDzfa+dLROn+VUhVUC61vOjaI1Cvo/GXyllGXVAiIk6AV8kzp7zAlbVwHG4EuLK68rVQ/t9eTEoH9N9/BFl4cpLl9Q==";
    private static final String SAML_LOCAL_KEY = "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";

    public Map<String, String> getParameterMap() {
        Map<String, String> config = new HashMap<>();
        switch (this) {
            case EMPTY:
                config.put("JAVA_OPTS", "-Xms32m -XX:MaxRAMPercentage=50.0");
                break;
            case STUDIO_SINGLE_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                config.put("user.mode", "single");
                config.put("security.single.username", "singleuser");
                config.put("security.administrators", "singleuser");
                break;
            case SINGLE_USER_STUDIO_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                break;
            case DEPLOY_STUDIO_PARAMS:
                config.putAll(DEFAULT_STUDIO_PARAMS.getParameterMap());
                // Production repository config is passed via .properties file
                // (not env vars) because WebStudio's $$ref syntax requires it.
                // The .properties file is created dynamically in the test's
                // beforeMethod and copied into the container via additionalContainerFiles.
                // We only need to ensure base env vars (user.mode, security.administrators)
                // are present — those come from DEFAULT_STUDIO_PARAMS.
                break;
            case STUDIO_GIT:
                config.putAll(DEFAULT_STUDIO_PARAMS.getParameterMap());
                config.put("repository.design.login", ProjectConfiguration.getProperty(PropertyNameSpace.GIT_LOGIN));
                config.put("repository.design.password", ProjectConfiguration.getProperty(PropertyNameSpace.GIT_PASSWORD));
                config.put("repository.design.uri", ProjectConfiguration.getProperty(PropertyNameSpace.GIT_URL));
                break;
            case STUDIO_CENTRAL_GROUP_1_PARAMS:
                config.putAll(DEFAULT_STUDIO_PARAMS.getParameterMap());
                config.put("design-repository-configs", "rating,claim");
                config.putAll(studioCentralRepoConfig("rating", "openl-rating"));
                config.putAll(studioCentralRepoConfig("claim", "openl-claim"));
                break;
            case STUDIO_CENTRAL_GROUP_2_PARAMS:
                config.putAll(DEFAULT_STUDIO_PARAMS.getParameterMap());
                config.put("design-repository-configs", "policy,policy_life,financials");
                config.putAll(studioCentralRepoConfig("policy", "openl-policy"));
                config.putAll(studioCentralRepoConfig("policy_life", "openl-policy-life"));
                config.putAll(studioCentralRepoConfig("financials", "openl-financials"));
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
                break;
            case SERVICE_FILE_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("ruleservice.deployer.enabled", "true");
                config.put("production-repository.factory", "repo-file");
                config.put("production-repository.uri", "/opt/openl/shared");
                break;
            case STUDIO_BYPASS_ENABLED_PARAMS:
                config.putAll(DEFAULT_STUDIO_PARAMS.getParameterMap());
                config.put("security.allow-bypass-protected-branches", "true");
                // Protect `release-**` only (matches `release-EPBDS-...`). `master` stays
                // unprotected so admin setup steps (initial upload, branch creation) are not
                // themselves blocked by the bypass guard.
                config.put("repository.design.protected-branches", "release-**");
                break;
            case STUDIO_BYPASS_BOTH_PROTECTED_PARAMS:
                config.putAll(DEFAULT_STUDIO_PARAMS.getParameterMap());
                config.put("security.allow-bypass-protected-branches", "true");
                // `*EPBDS-15818*` matches both EPBDS-15818_dev and release-EPBDS-15818, so the
                // merge crosses two protected branches; `master` stays unprotected for setup.
                config.put("repository.design.protected-branches", "*EPBDS-15818*");
                break;
            case STUDIO_PROTECTED_NO_BYPASS_PARAMS:
                config.putAll(DEFAULT_STUDIO_PARAMS.getParameterMap());
                // Bypass setting intentionally left OFF (default); only the protected matcher is set.
                config.put("repository.design.protected-branches", "release-**");
                break;
            case STUDIO_OIDC_BYPASS_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                config.put("user.mode", "oauth2");
                config.put("security.administrators", "admin");
                config.put("security.oauth2.client-id", "openlstudio");
                config.put("security.oauth2.client-secret", "openlstudiosecret");
                config.put("security.oauth2.issuer-uri", "http://keycloak:8080/realms/openlstudio");
                config.put("security.oauth2.scope", "openid,profile,email");
                config.put("security.oauth2.attribute.username", "preferred_username");
                config.put("security.oauth2.attribute.groups", "groups");
                config.put("security.allow-bypass-protected-branches", "true");
                config.put("repository.design.protected-branches", "release-**");
                break;
            case STUDIO_OIDC_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                config.put("user.mode", "oauth2");
                config.put("security.administrators", "admin");
                config.put("security.oauth2.client-id", "openlstudio");
                config.put("security.oauth2.client-secret", "openlstudiosecret");
                config.put("security.oauth2.issuer-uri", "http://keycloak:8080/realms/openlstudio");
                config.put("security.oauth2.scope", "openid,profile,email");
                config.put("security.oauth2.attribute.username", "preferred_username");
                config.put("security.oauth2.attribute.groups", "groups");
                break;
            case STUDIO_SAML_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                config.put("user.mode", "saml");
                config.put("security.administrators", "admin");
                config.put("security.saml.entity-id", "webstudio");
                config.put("security.saml.saml-server-metadata-url",
                        "http://keycloak:8080/realms/openlstudio/protocol/saml/descriptor");
                config.put("security.saml.local-key", SAML_LOCAL_KEY);
                config.put("security.saml.local-certificate", SAML_LOCAL_CERTIFICATE);
                config.put("security.saml.attribute.first-name", "urn:oid:2.5.4.42");
                config.put("security.saml.attribute.last-name", "urn:oid:2.5.4.4");
                config.put("security.saml.attribute.email", "urn:oid:0.9.2342.19200300.100.1.3");
                // Studio uses the user's display name as the Git commit author (PersonIdent);
                // a SAML user with no display-name attribute would commit with a null name.
                config.put("security.saml.attribute.display-name", "urn:oid:2.16.840.1.113730.3.1.241");
                break;
            case STUDIO_AD_PARAMS:
                config.putAll(EMPTY.getParameterMap());
                config.put("webstudio.configured", "true");
                config.put("user.mode", "ad");
                config.put("security.administrators", "studioadmin");
                config.put("security.ad.domain", "openl.local");
                config.put("security.ad.server-url", "ldap://samba:389");
                break;
            case DEFAULT_STUDIO_PARAMS:
            default:
                config.putAll(EMPTY.getParameterMap());
                config.put("user.mode", "multi");
                config.put("security.administrators", "admin");
                break;
        }
        return config;
    }

    private static Map<String, String> studioCentralRepoConfig(String id, String repoName) {
        Map<String, String> entries = new HashMap<>();
        entries.put("repository." + id + ".name", repoName);
        entries.put("repository." + id + ".$ref", "repo-git");
        entries.put("repository." + id + ".uri", STUDIO_CENTRAL_BASE + repoName + ".git");
        entries.put("repository." + id + ".local-repository-path", "${openl.home}/repositories/" + id);
        entries.put("repository." + id + ".branch", ProjectConfiguration.getProperty(PropertyNameSpace.GITLAB_BRANCH));
        entries.put("repository." + id + ".login", ProjectConfiguration.getProperty(PropertyNameSpace.GITLAB_USER));
        entries.put("repository." + id + ".password", ProjectConfiguration.getProperty(PropertyNameSpace.GITLAB_PASSWORD));
        return entries;
    }
}
