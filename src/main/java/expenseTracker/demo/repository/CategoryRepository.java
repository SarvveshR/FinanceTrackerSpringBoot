package expenseTracker.demo.repository;

import expenseTracker.demo.entities.CategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoriesEntity,String> {
}
