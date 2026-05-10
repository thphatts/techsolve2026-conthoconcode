package hackathon.fridgeai.repository;

import hackathon.fridgeai.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepositoy extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByName(String name);
}