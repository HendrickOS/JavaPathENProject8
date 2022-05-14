package tourGuide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

        MultiThreading multiThreading = new MultiThreading();
        multiThreading.start();

        MultiThreading multiThreading2 = new MultiThreading();
        multiThreading2.start();

        /* On peut également faire une boucle */
//        for (int i=0; i<5; i++){
//            MultiThreading multiThreading = new MultiThreading();
//            multiThreading.start();
//        }

    }

}
