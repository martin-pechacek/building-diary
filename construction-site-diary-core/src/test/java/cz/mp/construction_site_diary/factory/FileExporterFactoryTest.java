package cz.mp.construction_site_diary.factory;

import cz.mp.construction_site_diary.enums.ExportFormat;
import cz.mp.construction_site_diary.strategy.FileExporterStrategy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FileExporterFactoryTest {

    @Nested
    class Get {

        @Test
        void shouldReturnMatchingStrategy() {
            FileExporterStrategy csvStrategy = mock(FileExporterStrategy.class);
            FileExporterFactory factory = new FileExporterFactory(Map.of("CSV", csvStrategy));

            assertThat(factory.get(ExportFormat.CSV)).isSameAs(csvStrategy);
        }

        @Test
        void shouldThrowForNullFormat() {
            FileExporterFactory factory = new FileExporterFactory(Map.of());

            assertThatThrownBy(() -> factory.get(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unsupported file type");
        }

        @Test
        void shouldThrowWhenFormatNotRegistered() {
            FileExporterFactory factory = new FileExporterFactory(Map.of());

            assertThatThrownBy(() -> factory.get(ExportFormat.PDF))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unsupported file type");
        }
    }
}
