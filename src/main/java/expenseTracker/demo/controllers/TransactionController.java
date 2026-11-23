package expenseTracker.demo.controllers;


import expenseTracker.demo.requestDtos.RequestTransactionDTO;
import expenseTracker.demo.responseDTOs.ResponseCardBalanceTransactionDTO;
import expenseTracker.demo.security.CurrentUser;
import expenseTracker.demo.service.CardService;
import expenseTracker.demo.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    TransactionService transactionService;
    @Autowired
    CardService cardService;

    @PostMapping("/add/{cardId}")
    public ResponseCardBalanceTransactionDTO addTransaction(@RequestBody RequestTransactionDTO requestTransactionDTO, @PathVariable("cardId") int cardId, @AuthenticationPrincipal CurrentUser user) {
        log.debug("POST /transaction/add/{} userId={} amount={} category={} expense={} income={}", cardId, user.userId(),
                requestTransactionDTO.getAmount(), requestTransactionDTO.getType().getId(),
                requestTransactionDTO.isExpense(), requestTransactionDTO.isIncome());
        cardService.requireOwnedCard(cardId, user.userId());
        return transactionService.addTransaction(requestTransactionDTO, cardId);

    }

    @PutMapping("/edit/{cardId}/{transactionId}")
    public ResponseEntity<ResponseCardBalanceTransactionDTO> editTransaction(@RequestBody RequestTransactionDTO requestTransactionDTO, @PathVariable("cardId") int cardId, @PathVariable("transactionId")  int transactionId, @AuthenticationPrincipal CurrentUser user){
        log.debug("PUT /transaction/edit/{}/{} userId={} amount={}", cardId, transactionId, user.userId(), requestTransactionDTO.getAmount());
        cardService.requireOwnedCard(cardId, user.userId());
        transactionService.requireTransactionOnCard(transactionId, cardId);
        ResponseCardBalanceTransactionDTO responseCardBalanceTransactionDTO = transactionService.editTransaction(requestTransactionDTO, transactionId,cardId);
        return ResponseEntity.ok().body(responseCardBalanceTransactionDTO);


    }
    @DeleteMapping("/delete/{cardId}/{transactionId}")
  public ResponseEntity<ResponseCardBalanceTransactionDTO> deleteTransaction( @PathVariable("transactionId")  int transactionId, @PathVariable("cardId")  int cardId, @AuthenticationPrincipal CurrentUser user){
        log.debug("DELETE /transaction/delete/{}/{} userId={}", cardId, transactionId, user.userId());
        cardService.requireOwnedCard(cardId, user.userId());
        transactionService.requireTransactionOnCard(transactionId, cardId);
        return ResponseEntity.ok().body(transactionService.deleteTransaction(transactionId,cardId));

    }





}