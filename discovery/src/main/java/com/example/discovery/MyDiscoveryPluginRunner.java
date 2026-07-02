package com.example.discovery;

import com.digicert.tlm.SdkContext;
import com.digicert.tlm.SdkRuntime;

import java.io.IOException;

public class MyDiscoveryPluginRunner
{
    public static void main(String[] args) throws IOException {
        SdkRuntime runtime =
                new SdkRuntime(MyDiscoveryPluginRunner.class.getPackageName().toLowerCase());
        // Create a context and add some data to it
        SdkContext context = new SdkContext();
        runtime.execute("MyDiscoveryPlugin", context);
    }
}
