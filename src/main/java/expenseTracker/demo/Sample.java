package expenseTracker.demo;


import expenseTracker.demo.entities.CardEntity;
import expenseTracker.demo.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Sample {
    @Autowired
    CardRepository cardrepo;
    @GetMapping("/status")
    public String status(){
        return "Hello Connection Established!";
    }


}
