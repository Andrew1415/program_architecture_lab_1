package com.example.bookstockapi.shared;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DataDirectoryInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String url = event.getEnvironment().getProperty("spring.datasource.url", "");
        String prefix = "jdbc:sqlite:";
        if (!url.startsWith(prefix)) {
            return;
        }

        String databasePath = url.substring(prefix.length());
        if (databasePath.startsWith("./")) {
            databasePath = databasePath.substring(2);
        }

        File parent = new File(databasePath).getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
