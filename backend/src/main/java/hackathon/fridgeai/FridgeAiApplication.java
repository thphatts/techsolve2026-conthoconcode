package hackathon.fridgeai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // Import cái này

@SpringBootApplication
@EnableScheduling // BẬT TÍNH NĂNG CHẠY NGẦM LÊN !!!
public class FridgeAiApplication {
	public static void main(String[] args) {
		SpringApplication.run(FridgeAiApplication.class, args);
	}
}