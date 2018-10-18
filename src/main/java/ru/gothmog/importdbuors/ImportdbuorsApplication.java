package ru.gothmog.importdbuors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.gothmog.importdbuors.property.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class ImportdbuorsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImportdbuorsApplication.class, args);
    }
}
