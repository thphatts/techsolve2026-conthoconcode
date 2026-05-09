package hackathon.fridgeai.repository;

import hackathon.fridgeai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tự động sinh SQL tìm sản phẩm theo tên chính xác
    Optional<Product> findByName(String name);
}