package cz.mp.construction_site_diary.mapper;

import cz.mp.construction_site_diary.dto.DiaryEntryDto;
import cz.mp.construction_site_diary.dto.MaterialUsageDto;
import cz.mp.construction_site_diary.dto.WorkforceEntryDto;
import cz.mp.construction_site_diary.entity.DiaryEntry;
import cz.mp.construction_site_diary.entity.MaterialUsage;
import cz.mp.construction_site_diary.entity.Project;
import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.entity.WorkforceEntry;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DiaryEntryMapperImpl.class, WorkforceEntryMapperImpl.class, MaterialUsageMapperImpl.class})
class DiaryEntryMapperTest {

    private static final UUID ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
    private static final LocalDate ENTRY_DATE = LocalDate.of(2024, 1, 15);

    @Autowired
    private DiaryEntryMapper mapper;

    @Autowired
    private WorkforceEntryMapper workforceMapper;

    @Autowired
    private MaterialUsageMapper materialMapper;

    @Nested
    class ToDto {

        @Test
        void shouldMapEntityToDto() {
            User user = new User();
            user.setId(USER_ID);

            Project project = new Project();
            project.setId(PROJECT_ID);
            project.setCreatedBy(user);

            DiaryEntry entity = new DiaryEntry();
            entity.setId(ENTRY_ID);
            entity.setProject(project);
            entity.setDate(ENTRY_DATE);
            entity.setSummary("Daily summary");
            entity.setWeatherCondition("Sunny");
            entity.setTemperature(22.5f);

            DiaryEntryDto dto = mapper.toDto(entity);

            assertThat(dto.id()).isEqualTo(ENTRY_ID);
            assertThat(dto.projectId()).isEqualTo(PROJECT_ID);
            assertThat(dto.createdById()).isEqualTo(USER_ID);
            assertThat(dto.date()).isEqualTo(ENTRY_DATE);
            assertThat(dto.summary()).isEqualTo("Daily summary");
            assertThat(dto.weatherCondition()).isEqualTo("Sunny");
            assertThat(dto.temperature()).isEqualTo(22.5);
        }

        @Test
        void shouldMapEntityWithWorkforceEntries() {
            User user = new User();
            user.setId(USER_ID);

            Project project = new Project();
            project.setId(PROJECT_ID);
            project.setCreatedBy(user);

            DiaryEntry entity = new DiaryEntry();
            entity.setId(ENTRY_ID);
            entity.setProject(project);
            entity.setDate(ENTRY_DATE);

            WorkforceEntry workforceEntry = new WorkforceEntry();
            workforceEntry.setId(UUID.randomUUID());
            workforceEntry.setRole("Mason");
            workforceEntry.setFirstname("John");
            workforceEntry.setLastname("Doe");
            workforceEntry.setWorkingHours(BigDecimal.valueOf(8));
            entity.addWorkforceEntry(workforceEntry);

            DiaryEntryDto dto = mapper.toDto(entity);

            assertThat(dto.workforceEntries()).hasSize(1);
            assertThat(dto.workforceEntries().get(0).role()).isEqualTo("Mason");
        }

        @Test
        void shouldMapEntityWithMaterialUsages() {
            User user = new User();
            user.setId(USER_ID);

            Project project = new Project();
            project.setId(PROJECT_ID);
            project.setCreatedBy(user);

            DiaryEntry entity = new DiaryEntry();
            entity.setId(ENTRY_ID);
            entity.setProject(project);
            entity.setDate(ENTRY_DATE);

            MaterialUsage materialUsage = new MaterialUsage();
            materialUsage.setId(UUID.randomUUID());
            materialUsage.setMaterialName("Cement");
            materialUsage.setQuantity(BigDecimal.valueOf(50));
            materialUsage.setUnit("kg");
            entity.addMaterialUsage(materialUsage);

            DiaryEntryDto dto = mapper.toDto(entity);

            assertThat(dto.materialUsages()).hasSize(1);
            assertThat(dto.materialUsages().get(0).materialName()).isEqualTo("Cement");
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
            User user = new User();
            user.setId(USER_ID);

            Project project = new Project();
            project.setId(PROJECT_ID);
            project.setCreatedBy(user);

            DiaryEntry entity1 = new DiaryEntry();
            entity1.setId(ENTRY_ID);
            entity1.setProject(project);
            entity1.setDate(ENTRY_DATE);
            entity1.setSummary("Summary 1");

            DiaryEntry entity2 = new DiaryEntry();
            entity2.setId(UUID.randomUUID());
            entity2.setProject(project);
            entity2.setDate(ENTRY_DATE.plusDays(1));
            entity2.setSummary("Summary 2");

            List<DiaryEntryDto> dtos = mapper.toDtoList(List.of(entity1, entity2));

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).summary()).isEqualTo("Summary 1");
            assertThat(dtos.get(1).summary()).isEqualTo("Summary 2");
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
            DiaryEntryDto dto = new DiaryEntryDto(
                    ENTRY_ID, PROJECT_ID, ENTRY_DATE, "Daily summary",
                    "Sunny", 22.5, null, null, USER_ID, null, null
            );

            DiaryEntry entity = mapper.toEntity(dto, workforceMapper, materialMapper);

            assertThat(entity.getDate()).isEqualTo(ENTRY_DATE);
            assertThat(entity.getSummary()).isEqualTo("Daily summary");
            assertThat(entity.getWeatherCondition()).isEqualTo("Sunny");
            assertThat(entity.getTemperature()).isEqualTo(22.5f);
        }

        @Test
        void shouldMapDtoWithWorkforceEntriesToEntity() {
            WorkforceEntryDto workforceDto = new WorkforceEntryDto(
                    null, "Mason", "John", "Doe", BigDecimal.valueOf(8)
            );
            DiaryEntryDto dto = new DiaryEntryDto(
                    null, null, ENTRY_DATE, "Daily summary",
                    "Sunny", 22.5, List.of(workforceDto), null, null, null, null
            );

            DiaryEntry entity = mapper.toEntity(dto, workforceMapper, materialMapper);

            assertThat(entity.getWorkforceEntries()).hasSize(1);
            assertThat(entity.getWorkforceEntries().get(0).getRole()).isEqualTo("Mason");
            assertThat(entity.getWorkforceEntries().get(0).getDiaryEntry()).isEqualTo(entity);
        }

        @Test
        void shouldMapDtoWithMaterialUsagesToEntity() {
            MaterialUsageDto materialDto = new MaterialUsageDto(
                    null, "Cement", BigDecimal.valueOf(50), "kg"
            );
            DiaryEntryDto dto = new DiaryEntryDto(
                    null, null, ENTRY_DATE, "Daily summary",
                    "Sunny", 22.5, null, List.of(materialDto), null, null, null
            );

            DiaryEntry entity = mapper.toEntity(dto, workforceMapper, materialMapper);

            assertThat(entity.getMaterialUsages()).hasSize(1);
            assertThat(entity.getMaterialUsages().get(0).getMaterialName()).isEqualTo("Cement");
            assertThat(entity.getMaterialUsages().get(0).getDiaryEntry()).isEqualTo(entity);
        }

        @Test
        void shouldMapDtoWithBothCollectionsToEntity() {
            WorkforceEntryDto workforceDto = new WorkforceEntryDto(
                    null, "Mason", "John", "Doe", BigDecimal.valueOf(8)
            );
            MaterialUsageDto materialDto = new MaterialUsageDto(
                    null, "Cement", BigDecimal.valueOf(50), "kg"
            );
            DiaryEntryDto dto = new DiaryEntryDto(
                    null, null, ENTRY_DATE, "Daily summary",
                    "Sunny", 22.5, List.of(workforceDto), List.of(materialDto), null, null, null
            );

            DiaryEntry entity = mapper.toEntity(dto, workforceMapper, materialMapper);

            assertThat(entity.getWorkforceEntries()).hasSize(1);
            assertThat(entity.getMaterialUsages()).hasSize(1);
        }

        @Test
        void shouldReturnNullWhenDtoIsNull() {
            assertThat(mapper.toEntity(null, workforceMapper, materialMapper)).isNull();
        }

        @Test
        void shouldHandleNullCollections() {
            DiaryEntryDto dto = new DiaryEntryDto(
                    null, null, ENTRY_DATE, "Daily summary",
                    null, null, null, null, null, null, null
            );

            DiaryEntry entity = mapper.toEntity(dto, workforceMapper, materialMapper);

            assertThat(entity.getWorkforceEntries()).isEmpty();
            assertThat(entity.getMaterialUsages()).isEmpty();
        }
    }
}