package expenseTracker.demo.controllers;


import expenseTracker.demo.requestDtos.RequestCardDTO;
import expenseTracker.demo.responseDTOs.ResponseCardDTO;
import expenseTracker.demo.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/card")
public class CardController {

    @Autowired
     CardService cardService;



    @PostMapping("/add")// for adding card
    public void  addCard(@RequestBody RequestCardDTO card){
        System.out.println(card);
        cardService.addCard(card);
    }

    @GetMapping("/get")
    public List<ResponseCardDTO> getCards(){
        return cardService.getCards();
    }






}
