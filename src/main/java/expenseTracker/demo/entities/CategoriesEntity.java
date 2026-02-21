package expenseTracker.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
@Entity
public class CategoriesEntity {


    @Id
    String id;
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
