package cz.mp.building_diary.mapper;

import cz.mp.building_diary.dto.MaterialUsageDto;
import cz.mp.building_diary.entity.MaterialUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialUsageMapperTest {

    private static final UUID USAGE_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private MaterialUsageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(MaterialUsageMapper.class);
    }

    @Nested
    class ToDto {

        @Test
        void shouldMapEntityToDto() {
            MaterialUsage entity = new MaterialUsage();
            entity.setId(USAGE_ID);
            entity.setMaterialName("Cement");
            entity.setQuantity(BigDecimal.valueOf(50.5));
            entity.setUnit("kg");

            MaterialUsageDto dto = mapper.toDto(entity);

            assertThat(dto.id()).isEqualTo(USAGE_ID);
            assertThat(dto.materialName()).isEqualTo("Cement");
            assertThat(dto.quantity()).isEqualByComparingTo(BigDecimal.valueOf(50.5));
            assertThat(dto.unit()).isEqualTo("kg");
        }

        @Test
        void shouldReturnNullWhenEntityIsNull() {
            assertThat(mapper.toDto(null)).isNull();
        }
    }

    @Nested
    class ToDtoList {

        @Test
        void shouldMapEntityListToDtoList() {
            MaterialUsage entity1 = new MaterialUsage();
            entity1.setId(USAGE_ID);
            entity1.setMaterialName("Cement");
            entity1.setQuantity(BigDecimal.valueOf(50));
            entity1.setUnit("kg");

            MaterialUsage entity2 = new MaterialUsage();
            entity2.setId(UUID.randomUUID());
            entity2.setMaterialName("Sand");
            entity2.setQuantity(BigDecimal.valueOf(100));
            entity2.setUnit("kg");

            List<MaterialUsageDto> dtos = mapper.toDtoList(List.of(entity1, entity2));

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).materialName()).isEqualTo("Cement");
            assertThat(dtos.get(1).materialName()).isEqualTo("Sand");
        }

        @Test
        void shouldReturnNullWhenListIsNull() {
            assertThat(mapper.toDtoList(null)).isNull();
        }

        @Test
        void shouldReturnEmptyListWhenListIsEmpty() {
            assertThat(mapper.toDtoList(List.of())).isEmpty();
        }
    }

    @Nested
    class ToEntity {

        @Test
        void shouldMapDtoToEntity() {
            MaterialUsageDto dto = new MaterialUsageDto(
                    USAGE_ID, "Cement", BigDecimal.valueOf(50.5), "kg"
            );

            MaterialUsage entity = mapper.toEntity(dto);

            assertThat(entity.getMaterialName()).isEqualTo("Cement");
            assertThat(entity.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(50.5));
            assertThat(entity.getUnit()).isEqualTo("kg");
        }

        @Test
        void shouldReturnNullWhenDtoIsNull() {
            assertThat(mapper.toEntity(null)).isNull();
        }
    }

    @Nested
    class ToEntityList {

        @Test
        void shouldMapDtoListToEntityList() {
            MaterialUsageDto dto1 = new MaterialUsageDto(null, "Cement", BigDecimal.valueOf(50), "kg");
            MaterialUsageDto dto2 = new MaterialUsageDto(null, "Sand", BigDecimal.valueOf(100), "kg");

            List<MaterialUsage> entities = mapper.toEntityList(List.of(dto1, dto2));

            assertThat(entities).hasSize(2);
            assertThat(entities.get(0).getMaterialName()).isEqualTo("Cement");
            assertThat(entities.get(1).getMaterialName()).isEqualTo("Sand");
        }

        @Test
        void shouldReturnNullWhenListIsNull() {
            assertThat(mapper.toEntityList(null)).isNull();
        }

        @Test
        void shouldReturnEmptyListWhenListIsEmpty() {
            assertThat(mapper.toEntityList(List.of())).isEmpty();
        }
    }
}