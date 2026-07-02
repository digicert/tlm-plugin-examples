
/**
 * Configuration class for the DigiCert TLM Automation Plugin.
 * <p>
 * This class holds the configuration parameters required to connect and authenticate
 * with the managed system. It is typically populated from user input or configuration files.
 * <ul>
 *   <li><b>userName</b>: The username for authentication.</li>
 *   <li><b>password</b>: The password for authentication.</li>
 *   <li><b>managementIp</b>: The IP address of the management interface.</li>
 *   <li><b>managementPort</b>: The port number of the management interface.</li>
 * </ul>
 * <b>Usage:</b> This class is used by the plugin to retrieve connection details and credentials.<br>
 * <b>Customization:</b> Users can customize this class to add, remove, or modify configuration parameters as needed for their specific plugin requirements.<br>
 * <b>Important:</b> Any changes made to this class (such as adding, removing, or renaming fields) must also be reflected in the <code>configuration.json</code> file, specifically in the <code>config_settings</code> and <code>core_settings</code> sections, to ensure proper mapping and plugin functionality.
 */
package com.example.automation.extended.configuration;

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
    private String password;

    /**
     * The IP address of the management interface.
     */
    private String managementIp;

    /**
     * The port number of the management interface.
     */
    private String managementPort;
}
