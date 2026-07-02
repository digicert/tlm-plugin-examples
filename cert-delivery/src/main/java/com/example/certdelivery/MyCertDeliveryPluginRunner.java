package com.example.certdelivery;

import com.digicert.tlm.SdkContext;
import com.digicert.tlm.SdkRuntime;

import java.io.IOException;

public class MyCertDeliveryPluginRunner {

    public static void main(String[] args) throws IOException {
        // 1. Initialize the SDK runtime with the package name (in lowercase).
        // This sets up the environment for the plugin to run.
        SdkRuntime runtime = new SdkRuntime(MyCertDeliveryPluginRunner.class.getPackageName().toLowerCase());

        // 2. Create a new context object for the plugin execution.
        // Add data to the context here if your plugin needs customized parameters.
        SdkContext context = new SdkContext();

        // 3. Execute the plugin named "MyCertDeliveryPlugin" using the runtime and
        // context.
        // This will run the plugin's logic as defined in your implementation.

        runtime.execute("MyCertDeliveryPlugin", context);
    }
}
