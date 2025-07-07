package com.example.moyeorak.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @ManyToOne(optional = true)
    @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "fk_notice_author"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User author;

    @ManyToOne(optional = false)
    @JoinColumn(name = "region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notice_region"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Region region;
}
