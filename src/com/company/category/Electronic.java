package com.company.category;

import com.company.category.Category;

import java.time.LocalDateTime;
import java.util.UUID;

public class Electronic extends Category {
    public Electronic(UUID id, String names) {
        super(id, names);
    }
    @Override
    public LocalDateTime findDeliveryDueDate() {
        LocalDateTime localDateTime = LocalDateTime.now();// today
        return localDateTime.plusDays(4);
    }
    public String generateCategoryCode() {
        return "EL-" + getId().toString().substring(0,8);

}


}
