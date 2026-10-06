package vn.machtrip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MachtripApplication {

    public static void main(String[] args) {
        // PgJDBC sends the JVM default zone to Postgres at connect time. On Windows the JDK may report the legacy
        // alias "Asia/Saigon", which servers without tzdata-legacy reject ("invalid value for parameter TimeZone").
        // All time columns are timestamptz, so pinning UTC changes no stored value.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(MachtripApplication.class, args);
    }
}
