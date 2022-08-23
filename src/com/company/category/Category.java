package com.company.category;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Category {

    private UUID id;
    private String names;
    public Category(UUID id, String names) {
        this.id = id;
        this.names = names;
    }

    public abstract LocalDateTime findDeliveryDueDate();

    public String generateCategoryCode(){
        return id.toString().substring(0,8).concat("-").concat(names.substring(0,2));//id change to string
    }
    public UUID getId() {
        return id;
    }
    public String getNames() {
        return names;
    }
}
