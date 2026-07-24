package com.nicolashernandez.financetracker.service;
import com.nicolashernandez.financetracker.model.Category;
import com.nicolashernandez.financetracker.repository.CategoryRepository;
import com.nicolashernandez.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategory(Long id) {
        Optional<Category> result = categoryRepository.findById(id);

        if(result.isPresent()){
            return result.get();
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Category Not Found"
        );
    }

    public Category createCategory(Category category) {
        category.setId(null);//Must be Null if creating a new one
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = getCategory(id);
        boolean categoryIsInUse =
                transactionRepository.existsByCategory_Id(id);
        boolean typeIsChanging =
                existingCategory.getType() != updatedCategory.getType();

        if (categoryIsInUse && typeIsChanging) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category type cannot be changed because it is used by transactions"
            );
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setType(updatedCategory.getType());
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        Category existingCategory = getCategory(id);
        boolean isCategoryInUse = transactionRepository.existsByCategory_Id(id);

        if(isCategoryInUse){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category cannot be deleted because it is used by transactions"
            );

        }

        categoryRepository.delete(existingCategory);
    }
}
