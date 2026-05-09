package hackathon.fridgeai.entity;

import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fridges", indexes = @Index(name = "idx_fridges_owner", columnList = "owner_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Fridge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String location;

    // --- Relationships ---

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FridgeMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FridgeItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bill> bills = new ArrayList<>();
}