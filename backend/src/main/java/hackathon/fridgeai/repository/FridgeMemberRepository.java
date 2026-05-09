package hackathon.fridgeai.repository;

import hackathon.fridgeai.entity.FridgeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {

    boolean existsByFridgeIdAndUserId(Long fridgeId, Long userId);

}
