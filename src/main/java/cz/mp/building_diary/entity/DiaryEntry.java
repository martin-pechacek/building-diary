package cz.mp.building_diary.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "diary_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate date;

    private String summary;

    @Column(nullable = false)
    private String weatherCondition;

    @Column(nullable = false)
    private Double temperature;

    @OneToMany(mappedBy = "diaryEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkforceEntry> workforceEntries = new ArrayList<>();

    @OneToMany(mappedBy = "diaryEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialUsage> materialUsages = new ArrayList<>();

    @OneToMany(mappedBy = "diaryEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    public void addWorkforceEntry(WorkforceEntry entry) {
        workforceEntries.add(entry);
        entry.setDiaryEntry(this);
    }

    public void addMaterialUsage(MaterialUsage usage) {
        materialUsages.add(usage);
        usage.setDiaryEntry(this);
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
        photo.setDiaryEntry(this);
    }
}