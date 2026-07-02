package com.example.certdelivery.extended.configuration;

import lombok.Data;

@Data
public class MyPluginConfiguration {
    /**
     * The username for authenticating with the managed system.
     */
    private String userName;

    /**
     * The password for authenticating with the managed system.
     */
    private byte[] password;

}
