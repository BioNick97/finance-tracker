package com.nicolashernandez.financetracker.service;

import com.nicolashernandez.financetracker.model.Category;
import com.nicolashernandez.financetracker.repository.CategoryRepository;
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
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setType(updatedCategory.getType());
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        Category existingCategory = getCategory(id);
        categoryRepository.delete(existingCategory);
    }
}
