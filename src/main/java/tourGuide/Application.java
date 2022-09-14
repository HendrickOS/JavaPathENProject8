package tourGuide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);

        SpringApplication.run(Application.class, args);

//        MultiThreading multiThreading = new MultiThreading();
//        multiThreading.start();
//        multiThreading.run();

//        MultiThreading multiThreading2 = new MultiThreading();
//        multiThreading2.start();
//        multiThreading2.run();

        /* On peut également faire une boucle */
//        for (int i=0; i<5; i++){
//            MultiThreading multiThreading = new MultiThreading();
//            multiThreading.start();
//        }

    }

}
