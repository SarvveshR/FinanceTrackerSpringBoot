package expenseTracker.demo.service;

import expenseTracker.demo.entities.CategoriesEntity;
import expenseTracker.demo.repository.CategoryRepository;
import expenseTracker.demo.requestDtos.RequestCategoriesDTO;
import expenseTracker.demo.responseDTOs.ResponseCardDTO;
import expenseTracker.demo.responseDTOs.ResponseCategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;


    public Optional<RequestCategoriesDTO> getCategoryById(String id){
        CategoriesEntity categoriesEntity;
        return categoryRepository.findById(id).map(entity->{
            RequestCategoriesDTO requestCategoriesDTO=new RequestCategoriesDTO();
            requestCategoriesDTO.setId(entity.getId());
            requestCategoriesDTO.setName(entity.getName());
            return requestCategoriesDTO;
        });




    }
}
