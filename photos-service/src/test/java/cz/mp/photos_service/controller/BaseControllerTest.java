package cz.mp.photos_service.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseControllerTest {

    private static class ConcreteController extends BaseController {}

    @Test
    void shouldDefineBasePathConstant() {
        assertThat(BaseController.BASE_PATH).isEqualTo("/api/v1");
    }

    @Test
    void shouldBeAccessibleFromSubclass() {
        ConcreteController controller = new ConcreteController();
        assertThat(controller.BASE_PATH).isEqualTo("/api/v1");
    }
}
