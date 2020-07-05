package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author xiaoxi666
 * @date 2020-07-06 10:11
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Applicationloader {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Applicationloader.class);
        application.run(args);
    }
}
