package expenseTracker.demo.controllers;


import expenseTracker.demo.requestDtos.RequestTransactionDTO;
import expenseTracker.demo.responseDTOs.ResponseCardBalanceTransactionDTO;
import expenseTracker.demo.responseDTOs.ResponseTransactionDTO;
import expenseTracker.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @PostMapping("/add/{cardId}")
    public ResponseCardBalanceTransactionDTO addTransaction(@RequestBody RequestTransactionDTO requestTransactionDTO, @PathVariable("cardId") int cardId) {

        return transactionService.addTransaction(requestTransactionDTO, cardId);

    }

    @PutMapping("/edit/{cardId}/{transactionId}")
    public ResponseEntity<ResponseCardBalanceTransactionDTO> editTransaction(@RequestBody RequestTransactionDTO requestTransactionDTO, @PathVariable("cardId") int cardId, @PathVariable("transactionId")  int transactionId){
        ResponseCardBalanceTransactionDTO responseCardBalanceTransactionDTO = transactionService.editTransaction(requestTransactionDTO, transactionId,cardId);
        return ResponseEntity.ok().body(responseCardBalanceTransactionDTO);


    }
    @DeleteMapping("/delete/{cardId}/{transactionId}")
  public ResponseEntity<ResponseCardBalanceTransactionDTO> deleteTransaction( @PathVariable("transactionId")  int transactionId, @PathVariable("cardId")  int cardId){
        return ResponseEntity.ok().body(transactionService.deleteTransaction(transactionId,cardId));

    }





}
