package org.felixrilling.musicbrainzenricher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Configuration
@ConfigurationProperties(prefix = "musicbrainz.enricher")
public class ApplicationConfigurationProperties {

    @NotBlank
    private String host;


    @NotBlank
    @Pattern(regexp = "^\\w+$")
    private String name;

    @NotBlank
    private String version;

    @NotBlank
    private String contact;


    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}