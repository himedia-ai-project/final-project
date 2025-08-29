package com.gigigenie.domain.product.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.gigigenie.domain.chat.util.VectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Integer id;

    @JsonBackReference
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 255)
    @NotNull
    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Size(max = 255)
    @Column(name = "model_image")
    private String modelImage;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "feature_summary", columnDefinition = "TEXT")
    private String featureSummary;

    @Type(VectorType.class)
    @Column(name = "feature_embedding", columnDefinition = "vector")
    private List<Float> featureEmbedding;

}