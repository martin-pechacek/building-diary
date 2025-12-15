package cz.mp.building_diary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BuildingDiaryApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertNotNull(applicationContext, "Application context should load successfully");
	}

	@Test
	void applicationHasRequiredBeans() {
		assertNotNull(applicationContext.getBean(BuildingDiaryApplication.class),
				"Main application bean should be present");
	}

}
