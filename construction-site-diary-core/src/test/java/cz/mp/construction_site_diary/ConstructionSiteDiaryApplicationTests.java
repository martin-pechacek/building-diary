package cz.mp.construction_site_diary;

import cz.mp.construction_site_diary.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(TestSecurityConfig.class)
class ConstructionSiteDiaryApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should load successfully");
    }

    @Test
    void applicationHasRequiredBeans() {
        assertNotNull(applicationContext.getBean(ConstructionSiteDiaryApplication.class),
                "Main application bean should be present");
    }

}
