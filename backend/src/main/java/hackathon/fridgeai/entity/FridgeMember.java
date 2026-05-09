package hackathon.fridgeai.entity;

import hackathon.fridgeai.enums.FridgeMemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "fridge_members", uniqueConstraints = @UniqueConstraint(name = "uq_fridge_user", columnNames = {
                "fridge_id", "user_id" }), indexes = {
                                @Index(name = "idx_fridge_members_fridge", columnList = "fridge_id"),
                                @Index(name = "idx_fridge_members_user", columnList = "user_id")
                })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FridgeMember extends BaseEntity {

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "fridge_id", nullable = false)
        private Fridge fridge;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        @Builder.Default
        private FridgeMemberRole role = FridgeMemberRole.MEMBER;

        @Column(name = "joined_at", nullable = false)
        @Builder.Default
        private Instant joinedAt = Instant.now();
}
