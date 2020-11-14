package org.felixrilling.musicbrainzenricher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "musicbrainz.enricher")
public class ApplicationConfigurationProperties {

    private String host;

    private String name;
    private String version;
    private String contact;
    private Client client;

    private String username;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private static class Client {
        private String mb;
        private String rfc1945;

        public String getMb() {
            return mb;
        }

        public void setMb(String mb) {
            this.mb = mb;
        }

        public String getRfc1945() {
            return rfc1945;
        }

        public void setRfc1945(String rfc1945) {
            this.rfc1945 = rfc1945;
        }
    }
}