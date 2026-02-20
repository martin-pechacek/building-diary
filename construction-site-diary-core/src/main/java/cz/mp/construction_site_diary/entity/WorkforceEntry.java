package cz.mp.construction_site_diary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workforce_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkforceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_entry_id", nullable = false)
    private DiaryEntry diaryEntry;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;
    
    @Column(name = "working_hours")
    private BigDecimal workingHours;
}