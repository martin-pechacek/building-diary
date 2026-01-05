package cz.mp.building_diary.mapper;

import cz.mp.building_diary.dto.WorkforceEntryDto;
import cz.mp.building_diary.entity.WorkforceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkforceEntryMapperTest {

    private static final UUID ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private WorkforceEntryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(WorkforceEntryMapper.class);
    }

    @Nested
    class ToDto {

        @Test
        void shouldMapEntityToDto() {
            WorkforceEntry entity = new WorkforceEntry();
            entity.setId(ENTRY_ID);
            entity.setRole("Mason");
            entity.setFirstname("John");
            entity.setLastname("Doe");
            entity.setWorkingHours(BigDecimal.valueOf(8.5));

            WorkforceEntryDto dto = mapper.toDto(entity);

            assertThat(dto.id()).isEqualTo(ENTRY_ID);
            assertThat(dto.role()).isEqualTo("Mason");
            assertThat(dto.firstname()).isEqualTo("John");
            assertThat(dto.lastname()).isEqualTo("Doe");
            assertThat(dto.workingHours()).isEqualByComparingTo(BigDecimal.valueOf(8.5));
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
            WorkforceEntry entity1 = new WorkforceEntry();
            entity1.setId(ENTRY_ID);
            entity1.setRole("Mason");
            entity1.setFirstname("John");
            entity1.setLastname("Doe");
            entity1.setWorkingHours(BigDecimal.valueOf(8));

            WorkforceEntry entity2 = new WorkforceEntry();
            entity2.setId(UUID.randomUUID());
            entity2.setRole("Electrician");
            entity2.setFirstname("Jane");
            entity2.setLastname("Smith");
            entity2.setWorkingHours(BigDecimal.valueOf(6));

            List<WorkforceEntryDto> dtos = mapper.toDtoList(List.of(entity1, entity2));

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).role()).isEqualTo("Mason");
            assertThat(dtos.get(1).role()).isEqualTo("Electrician");
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
            WorkforceEntryDto dto = new WorkforceEntryDto(
                    ENTRY_ID, "Mason", "John", "Doe", BigDecimal.valueOf(8.5)
            );

            WorkforceEntry entity = mapper.toEntity(dto);

            assertThat(entity.getRole()).isEqualTo("Mason");
            assertThat(entity.getFirstname()).isEqualTo("John");
            assertThat(entity.getLastname()).isEqualTo("Doe");
            assertThat(entity.getWorkingHours()).isEqualByComparingTo(BigDecimal.valueOf(8.5));
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
            WorkforceEntryDto dto1 = new WorkforceEntryDto(null, "Mason", "John", "Doe", BigDecimal.valueOf(8));
            WorkforceEntryDto dto2 = new WorkforceEntryDto(null, "Electrician", "Jane", "Smith", BigDecimal.valueOf(6));

            List<WorkforceEntry> entities = mapper.toEntityList(List.of(dto1, dto2));

            assertThat(entities).hasSize(2);
            assertThat(entities.get(0).getRole()).isEqualTo("Mason");
            assertThat(entities.get(1).getRole()).isEqualTo("Electrician");
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