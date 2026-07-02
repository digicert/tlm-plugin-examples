/**
 * Entry point for the DigiCert TLM Automation Plugin.
 * <p>
 * <b>Purpose:</b> This class contains the main method to launch your automation plugin using the DigiCert TLM SDK.<br>
 * <b>How it works:</b>
 * <ol>
 *   <li>Initializes the plugin runtime environment.</li>
 *   <li>Creates a context for passing data to the plugin.</li>
 *   <li>Executes the plugin by name with the provided context.</li>
 * </ol>
 * <b>Who should read this?</b> Anyone learning how to start or test a DigiCert TLM automation plugin, especially beginners.<br>
 * <b>Usage Example:</b>
 * <pre>
 *   java com.example.automation.MyAutomationPluginRunner
 * </pre>
 * <p>
 * For more details, see the DigiCert TLM SDK documentation or consult your team lead.
 */

package com.example.automation;

import java.io.IOException;

import com.digicert.tlm.SdkContext;
import com.digicert.tlm.SdkRuntime;

public class MyAutomationPluginRunner {

    /**
     * Main method to start the Automation Plugin.
     * <p>
     * This is where the program begins execution.
     *
     * @param args Command-line arguments (not used)
     * @throws IOException if an I/O error occurs during plugin execution
     */
    public static void main(String[] args) throws IOException {
        // 1. Initialize the SDK runtime with the package name (in lowercase).
        // This sets up the environment for the plugin to run.
        SdkRuntime runtime = new SdkRuntime(MyAutomationPluginRunner.class.getPackageName().toLowerCase());

        // 2. Create a new context object for the plugin execution.
        // Add data to the context here if your plugin needs customized parameters.
        SdkContext context = new SdkContext();

        // 3. Execute the plugin named "MyAutomationPlugin" using the runtime and
        // context.
        // This will run the plugin's logic as defined in your implementation.
        runtime.execute("MyAutomationPlugin", context);
    }
}
