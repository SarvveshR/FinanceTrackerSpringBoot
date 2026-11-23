package expenseTracker.demo.controllers;


import expenseTracker.demo.repository.CardRepository;
import expenseTracker.demo.requestDtos.RequestCardDTO;
import expenseTracker.demo.responseDTOs.ResponseCardDTO;
import expenseTracker.demo.security.CurrentUser;
import expenseTracker.demo.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/card")
public class CardController {
    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    @Autowired
     CardService cardService;



    @PostMapping("/add")// for adding card
    public void  addCard(@RequestBody RequestCardDTO card, @AuthenticationPrincipal CurrentUser user){
        log.debug("POST /card/add userId={} network={} debit={} credit={}", user.userId(), card.getCardNetwork(), card.isDebit(), card.isCredit());
        cardService.addCard(card, user.userId());
    }

    @GetMapping("/get")
    public List<ResponseCardDTO> getCards(@AuthenticationPrincipal CurrentUser user){
        log.debug("GET /card/get userId={}", user.userId());
        List<ResponseCardDTO> cards = cardService.getCards(user.userId());
        log.debug("GET /card/get returning {} cards", cards.size());
        return cards;
    }

    @PutMapping("/edit/{cardId}")
    public ResponseCardDTO editCard(@RequestBody RequestCardDTO card, @PathVariable("cardId") int cardId, @AuthenticationPrincipal CurrentUser user){
        log.debug("PUT /card/edit/{} userId={}", cardId, user.userId());
        return cardService.editCard(card,cardId, user.userId());
    }
    @DeleteMapping("/delete/{cardId}")
    public boolean deleteCard( @PathVariable("cardId") int cardId, @AuthenticationPrincipal CurrentUser user){
        log.debug("DELETE /card/delete/{} userId={}", cardId, user.userId());
        return cardService.deleteCard(cardId, user.userId()) ;
    }






}
