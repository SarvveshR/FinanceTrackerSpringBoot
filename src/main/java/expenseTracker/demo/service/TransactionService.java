package expenseTracker.demo.service;

import expenseTracker.demo.entities.TransactionEntity;
import expenseTracker.demo.exception.ApiException;
import expenseTracker.demo.exception.ErrorCode;
import expenseTracker.demo.repository.CardRepository;
import expenseTracker.demo.repository.TransactionRepository;
import expenseTracker.demo.requestDtos.RequestCategoriesDTO;
import expenseTracker.demo.requestDtos.RequestTransactionDTO;
import expenseTracker.demo.responseDTOs.ResponseCardBalanceTransactionDTO;
import expenseTracker.demo.responseDTOs.ResponseTransactionDTO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    CategoryService categoryService;

    @Autowired
    CardRepository cardRepository;


    // Stops editing/deleting a transaction through a card it doesn't belong to
    public void requireTransactionOnCard(int transactionId, int cardId){
        boolean onCard = transactionRepository.findById(transactionId)
                .map(t -> t.getCardId() == cardId)
                .orElse(false);
        if(!onCard){
            log.debug("transactionId={} not found on cardId={}", transactionId, cardId);
            throw new ApiException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
    }

    // Rejects a bad request before anything is written
    private void validate(RequestTransactionDTO requestTransactionDTO){
        RequestCategoriesDTO type = requestTransactionDTO.getType();
        if(type == null || type.getId() == null || type.getId().isBlank()){
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Pick a category");
        }
        if(requestTransactionDTO.getAmount() <= 0){
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Amount must be more than 0");
        }
        if(requestTransactionDTO.isExpense() == requestTransactionDTO.isIncome()){
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "A transaction must be either income or expense");
        }
        if(categoryService.getCategoryById(type.getId()).isEmpty()){
            throw new ApiException(ErrorCode.CATEGORY_NOT_FOUND, "Unknown category: " + type.getId());
        }
    }

    // @Transactional: if any step throws, the saved transaction and the balance change are both undone
    @Transactional
    public ResponseCardBalanceTransactionDTO addTransaction(RequestTransactionDTO requestTransactionDTO, int cardId){
        validate(requestTransactionDTO);
ResponseCardBalanceTransactionDTO responseCardBalanceTransactionDTO = new ResponseCardBalanceTransactionDTO();

        // Check the card's limits before saving anything
        long newCreditsUsed = 0;
        if(cardRepository.isCredit(cardId)){
            newCreditsUsed = cardRepository.getCardCreditsUsed(cardId)
                    + creditEffect(requestTransactionDTO.getAmount(), requestTransactionDTO.isExpense());
            checkCreditsUsed(newCreditsUsed, cardRepository.getCreditLimit(cardId));
        }
        else if(cardRepository.isDebit(cardId) && requestTransactionDTO.isExpense()
                && cardRepository.getCardBalance(cardId) < requestTransactionDTO.getAmount()){
            throw new ApiException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        TransactionEntity transactionEntity=new TransactionEntity();
        transactionEntity.setCategoryId(requestTransactionDTO.getType().getId());
        transactionEntity.setDate(requestTransactionDTO.getDate());
        transactionEntity.setCardId(cardId);
        transactionEntity.setTime(requestTransactionDTO.getTime());
        transactionEntity.setDescription(requestTransactionDTO.getDescription());
        transactionEntity.setAmount(requestTransactionDTO.getAmount());
        transactionEntity.setExpense(requestTransactionDTO.isExpense());
        transactionEntity.setIncome(requestTransactionDTO.isIncome());
        TransactionEntity savedTransaction=transactionRepository.save(transactionEntity);
        log.debug("addTransaction saved transactionId={} on cardId={}", savedTransaction.getId(), cardId);
        ResponseTransactionDTO transactionDTO= getAddedTransaction(savedTransaction);
        responseCardBalanceTransactionDTO.setResponseTransactionDTO(transactionDTO);
        if(cardRepository.isCredit(cardId)){
            log.debug("addTransaction credit card {}: creditsUsed {} -> {}", cardId,
                    cardRepository.getCardCreditsUsed(cardId), newCreditsUsed);
            updateCardCreditsUsed(cardId,newCreditsUsed);
            responseCardBalanceTransactionDTO.setCreditsUsed(cardRepository.getCardCreditsUsed(cardId));
        }
        else if(cardRepository.isDebit(cardId)){
            updateCardBalance(cardId, transactionDTO);
            responseCardBalanceTransactionDTO.setBalance(cardRepository.getCardBalance(cardId));
        }



         return responseCardBalanceTransactionDTO;

    }

    public List<ResponseTransactionDTO> getTransactionsByCardId(int cardId){
        List<TransactionEntity> transactionEntities= transactionRepository.getAllTransaction(cardId);
        log.debug("getTransactionsByCardId cardId={} found {} transactions", cardId, transactionEntities.size());
        List<ResponseTransactionDTO> responseTransactionDTOs=new ArrayList<>();
        for(TransactionEntity transactionEntity :transactionEntities){
            ResponseTransactionDTO responseTransactionDTO=new ResponseTransactionDTO();
            responseTransactionDTO.setId(transactionEntity.getId());
            // One bad row shouldn't break loading every card, so an unknown category is shown as "Unknown"
            responseTransactionDTO.setType(categoryService.getCategoryById(transactionEntity.getCategoryId()).orElseGet(() -> {
                log.warn("transactionId={} has unknown category {}", transactionEntity.getId(), transactionEntity.getCategoryId());
                RequestCategoriesDTO unknown = new RequestCategoriesDTO();
                unknown.setId(transactionEntity.getCategoryId());
                unknown.setName("Unknown");
                return unknown;
            }));

            responseTransactionDTO.setDate(transactionEntity.getDate());
            responseTransactionDTO.setTime(transactionEntity.getTime());
            responseTransactionDTO.setDescription(transactionEntity.getDescription());
            responseTransactionDTO.setAmount(transactionEntity.getAmount());
            responseTransactionDTO.setExpense(transactionEntity.isExpense());
            responseTransactionDTO.setIncome(transactionEntity.isIncome());
            responseTransactionDTOs.add(responseTransactionDTO);

        }
        return responseTransactionDTOs;

    }

    public ResponseTransactionDTO getAddedTransaction(TransactionEntity transactionEntity){
        ResponseTransactionDTO responseTransactionDTO=new ResponseTransactionDTO();

        responseTransactionDTO.setId(transactionEntity.getId());
        responseTransactionDTO.setType(categoryService.getCategoryById(transactionEntity.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND)));
        responseTransactionDTO.setDate(transactionEntity.getDate());
        responseTransactionDTO.setTime(transactionEntity.getTime());
        responseTransactionDTO.setDescription(transactionEntity.getDescription());
        responseTransactionDTO.setAmount(transactionEntity.getAmount());
        responseTransactionDTO.setExpense(transactionEntity.isExpense());
        responseTransactionDTO.setIncome(transactionEntity.isIncome());
        return responseTransactionDTO;
    }

    @Transactional
    public ResponseCardBalanceTransactionDTO editTransaction(RequestTransactionDTO requestTransactionDTO,int transactionId, int cardId){
        validate(requestTransactionDTO);
        TransactionEntity transactionEntity= transactionRepository.getById(transactionId);
        ResponseCardBalanceTransactionDTO responseCardBalanceTransactionDTO= new ResponseCardBalanceTransactionDTO();

        transactionEntity.setCategoryId(requestTransactionDTO.getType().getId());
        transactionEntity.setDate(requestTransactionDTO.getDate());
        transactionEntity.setTime(requestTransactionDTO.getTime());
        transactionEntity.setDescription(requestTransactionDTO.getDescription());

        if(cardRepository.isDebit(cardId)){
            if(updateCardBalanceTransactionEdit(cardId,requestTransactionDTO,transactionEntity,transactionId)){
                transactionEntity.setAmount(requestTransactionDTO.getAmount());
            }
            TransactionEntity  savedEntity= transactionRepository.save(transactionEntity);
            ResponseTransactionDTO responseTransactionDTO=getAddedTransaction(savedEntity);
            responseCardBalanceTransactionDTO.setResponseTransactionDTO(responseTransactionDTO);
            responseCardBalanceTransactionDTO.setBalance(cardRepository.getCardBalance(cardId));

            return responseCardBalanceTransactionDTO;

        }
        else  if(cardRepository.isCredit(cardId)){
            if(updateCardCreditsTransactionEdit(cardId,requestTransactionDTO,transactionEntity,transactionId)){
                transactionEntity.setAmount(requestTransactionDTO.getAmount());
            }
            TransactionEntity  savedEntity= transactionRepository.save(transactionEntity);
            ResponseTransactionDTO responseTransactionDTO=getAddedTransaction(savedEntity);
            responseCardBalanceTransactionDTO.setResponseTransactionDTO(responseTransactionDTO);
            responseCardBalanceTransactionDTO.setCreditsUsed(cardRepository.getCardCreditsUsed(cardId));


        }

        return responseCardBalanceTransactionDTO;





    }


    // Returns true if the amount changed; false if it's the same. Throws if the balance can't cover the change.
    public boolean updateCardBalanceTransactionEdit(int cardId,RequestTransactionDTO requestTransactionDTO,TransactionEntity  currentTransactionEntity,int transactionid){
        long cardBalance = cardRepository.getCardBalance(cardId);
        int difference= currentTransactionEntity.getAmount() -requestTransactionDTO.getAmount();


            if(difference<0){
                if(requestTransactionDTO.isIncome()) {

                        long newCardBalance = cardBalance + Math.abs(difference) ;// difference is+-ve so abs
                        cardRepository.updateCardBalance(cardId, newCardBalance);
                        return true;
                }
                else if(requestTransactionDTO.isExpense()){
                    if(cardBalance>= Math.abs(difference)){
                        long newCardBalance= cardBalance-Math.abs(difference);
                        cardRepository.updateCardBalance(cardId, newCardBalance);
                        return true;
                    }
                    log.debug("editTransaction {}: balance {} can't cover {} more", transactionid, cardBalance, Math.abs(difference));
                    throw new ApiException(ErrorCode.INSUFFICIENT_BALANCE);

                }

            }
            else if(difference>0){
                if(requestTransactionDTO.isIncome()){
                    if (cardBalance >= difference) {
                        long newCardBalance=cardBalance-difference;// diff is +ve
                        cardRepository.updateCardBalance(cardId,newCardBalance);
                        return true;
                    }
                    log.debug("editTransaction {}: balance {} too low to reduce income by {}", transactionid, cardBalance, difference);
                    throw new ApiException(ErrorCode.INSUFFICIENT_BALANCE,
                            "Lowering this income would make the balance negative");

                }
                else if(requestTransactionDTO.isExpense()){
                   long newCardBalance = cardBalance+difference;
                    cardRepository.updateCardBalance(cardId, newCardBalance);
                    return true;

                }

            }



        return false;
    }



    // Returns true if the amount changed; false if it's the same.
    // Throws if the new amount would pass the credit limit, or make a payment bigger than what was used.
    public boolean updateCardCreditsTransactionEdit(int cardId,RequestTransactionDTO requestTransactionDTO,TransactionEntity  currentTransactionEntity,int transactionid){
        if(currentTransactionEntity.getAmount() == requestTransactionDTO.getAmount()){
            return false;
        }
        boolean isExpense = currentTransactionEntity.isExpense();
        long creditsUsed = cardRepository.getCardCreditsUsed(cardId);
        // Undo the old amount's effect, apply the new one
        long newCreditsUsed = creditsUsed
                - creditEffect(currentTransactionEntity.getAmount(), isExpense)
                + creditEffect(requestTransactionDTO.getAmount(), isExpense);
        log.debug("editTransaction {}: creditsUsed {} -> {}", transactionid, creditsUsed, newCreditsUsed);
        checkCreditsUsed(newCreditsUsed, cardRepository.getCreditLimit(cardId));
        cardRepository.updateCardCreditsUsed(cardId, newCreditsUsed);
        return true;
    }

    // On a credit card an expense adds to "credits used" and an income is a payment that takes it down
    private static long creditEffect(int amount, boolean isExpense){
        return isExpense ? amount : -amount;
    }

    private static void checkCreditsUsed(long newCreditsUsed, long creditLimit){
        if(newCreditsUsed > creditLimit){
            throw new ApiException(ErrorCode.CREDIT_LIMIT_EXCEEDED);
        }
        if(newCreditsUsed < 0){
            throw new ApiException(ErrorCode.PAYMENT_EXCEEDS_USED);
        }
    }






    public void  updateCardBalance(int cardId, ResponseTransactionDTO responseTransactionDTO){
        long cardBalance = cardRepository.getCardBalance(cardId);
        long oldBalance = cardBalance;
        if(responseTransactionDTO.isIncome()){
            cardBalance= cardBalance+responseTransactionDTO.getAmount();
        }
        else if(responseTransactionDTO.isExpense()){
            cardBalance= cardBalance-responseTransactionDTO.getAmount();

        }
        log.debug("updateCardBalance debit card {}: balance {} -> {}", cardId, oldBalance, cardBalance);

          cardRepository.updateCardBalance(cardId,cardBalance);

    }

    public void updateCardCreditsUsed(int cardId, long newCreditsUsed){
        cardRepository.updateCardCreditsUsed(cardId, newCreditsUsed);

    }



    @Transactional
    public ResponseCardBalanceTransactionDTO deleteTransaction( int transactionId, int cardId) {
        TransactionEntity transactionEntity= transactionRepository.getReferenceById(transactionId);
        ResponseCardBalanceTransactionDTO responseCardBalanceTransactionDTO= new ResponseCardBalanceTransactionDTO();
        long cardBalance= cardRepository.getCardBalance(cardId);


        if (cardRepository.isDebit(cardId)) {
            if(transactionEntity.isExpense() ){
                long newCardBalance= cardBalance+transactionEntity.getAmount();
                cardRepository.updateCardBalance(cardId, newCardBalance);
                responseCardBalanceTransactionDTO.setBalance(cardRepository.getCardBalance(cardId));
                transactionRepository.deleteById(transactionId);

            }
            else if(transactionEntity.isIncome() && cardBalance>= transactionEntity.getAmount()){
                cardRepository.updateCardBalance(cardId, cardBalance-transactionEntity.getAmount());
                responseCardBalanceTransactionDTO.setBalance(cardRepository.getCardBalance(cardId));
                transactionRepository.deleteById(transactionId);

            }
            else{
                // Removing this income would take the balance below zero
                log.debug("deleteTransaction {} refused: balance {} is less than income amount {}",
                        transactionId, cardBalance, transactionEntity.getAmount());
                throw new ApiException(ErrorCode.INSUFFICIENT_BALANCE,
                        "Deleting this income would make the balance negative");

            }
            log.debug("deleteTransaction {} done on debit card {}", transactionId, cardId);
            return responseCardBalanceTransactionDTO;

        }

        // Deleting an expense frees up credit; deleting a payment puts the amount back on the card
        long cardCreditsUsed= cardRepository.getCardCreditsUsed(cardId);
        long newCreditsUsed= cardCreditsUsed - creditEffect(transactionEntity.getAmount(), transactionEntity.isExpense());
        log.debug("deleteTransaction {} on credit card {}: creditsUsed {} -> {}",
                transactionId, cardId, cardCreditsUsed, newCreditsUsed);
        if(newCreditsUsed > cardRepository.getCreditLimit(cardId)){
            throw new ApiException(ErrorCode.CREDIT_LIMIT_EXCEEDED,
                    "Deleting this payment would put the card over its credit limit");
        }
        cardRepository.updateCardCreditsUsed(cardId, Math.max(newCreditsUsed, 0));
        responseCardBalanceTransactionDTO.setCreditsUsed(cardRepository.getCardCreditsUsed(cardId));
        transactionRepository.deleteById(transactionId);
        return  responseCardBalanceTransactionDTO;




    }
    @Transactional
    public boolean deleteTransactionByCardId(int cardId){
        int noOfEntity= transactionRepository.getAllTransaction(cardId).size();
        int rowsDeleted= transactionRepository.deleteTransactionbyCardId(cardId);
        log.debug("deleteTransactionByCardId cardId={} deleted {} of {}", cardId, rowsDeleted, noOfEntity);
        if(rowsDeleted==noOfEntity){
            return true;
        }
        else{
            return false;
        }
    }







}
