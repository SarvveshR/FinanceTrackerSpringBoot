package expenseTracker.demo.service;

import expenseTracker.demo.entities.TransactionEntity;
import expenseTracker.demo.repository.CardRepository;
import expenseTracker.demo.repository.TransactionRepository;
import expenseTracker.demo.requestDtos.RequestTransactionDTO;
import expenseTracker.demo.responseDTOs.ResponseCardBalanceTransactionDTO;
import expenseTracker.demo.responseDTOs.ResponseTransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.LifecycleProcessor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    CategoryService categoryService;

    @Autowired
    CardRepository cardRepository;


    public ResponseCardBalanceTransactionDTO addTransaction(RequestTransactionDTO requestTransactionDTO, int cardId){
ResponseCardBalanceTransactionDTO responseCardBalanceTransactionDTO = new ResponseCardBalanceTransactionDTO();


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
        ResponseTransactionDTO transactionDTO= getAddedTransaction(savedTransaction);

        responseCardBalanceTransactionDTO.setResponseTransactionDTO(transactionDTO);
        if(cardRepository.isCredit(cardId)){
            updateCardCreditsUsed(cardId,transactionDTO.getAmount());
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
        List<ResponseTransactionDTO> responseTransactionDTOs=new ArrayList<>();
        for(TransactionEntity transactionEntity :transactionEntities){
            ResponseTransactionDTO responseTransactionDTO=new ResponseTransactionDTO();
            responseTransactionDTO.setId(transactionEntity.getId());
            responseTransactionDTO.setType(categoryService.getCategoryById(transactionEntity.getCategoryId()).orElseThrow(() ->
                    new RuntimeException("Category not found: " + transactionEntity.getCategoryId())
            ));

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
        responseTransactionDTO.setType(categoryService.getCategoryById(transactionEntity.getCategoryId()).get());
        responseTransactionDTO.setDate(transactionEntity.getDate());
        responseTransactionDTO.setTime(transactionEntity.getTime());
        responseTransactionDTO.setDescription(transactionEntity.getDescription());
        responseTransactionDTO.setAmount(transactionEntity.getAmount());
        responseTransactionDTO.setExpense(transactionEntity.isExpense());
        responseTransactionDTO.setIncome(transactionEntity.isIncome());
        return responseTransactionDTO;
    }

    public ResponseCardBalanceTransactionDTO editTransaction(RequestTransactionDTO requestTransactionDTO,int transactionId, int cardId){
        TransactionEntity transactionEntity= transactionRepository.getById(transactionId);
        ResponseCardBalanceTransactionDTO responseCardBalanceTransactionDTO= new ResponseCardBalanceTransactionDTO();


        if(cardRepository.isDebit(cardId)){
            if(updateCardBalanceTransactionEdit(cardId,requestTransactionDTO,transactionEntity,transactionId)){
                transactionEntity.setCategoryId(requestTransactionDTO.getType().getId());
                transactionEntity.setDate(requestTransactionDTO.getDate());
                transactionEntity.setTime(requestTransactionDTO.getTime());
                transactionEntity.setDescription(requestTransactionDTO.getDescription());
                transactionEntity.setAmount(requestTransactionDTO.getAmount());
                TransactionEntity savedEntity= transactionRepository.save(transactionEntity);
                ResponseTransactionDTO responseTransactionDTO=getAddedTransaction(savedEntity);
                responseCardBalanceTransactionDTO.setResponseTransactionDTO(responseTransactionDTO);
                responseCardBalanceTransactionDTO.setBalance(cardRepository.getCardBalance(cardId));
                return responseCardBalanceTransactionDTO;
            }
            else{
                return responseCardBalanceTransactionDTO;

            }
        }






        return responseCardBalanceTransactionDTO;





    }

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
                    else{
                        return false;
                    }


                }

            }
            else if(difference>0){
                if(requestTransactionDTO.isIncome()){
                    if (cardBalance >= difference) {
                        long newCardBalance=cardBalance-difference;// diff is +ve
                        cardRepository.updateCardBalance(cardId,newCardBalance);
                        return true;
                    }
                    else{
                        return false;

                    }

                }
                else if(requestTransactionDTO.isExpense()){
                   long newCardBalance = cardBalance+difference;
                    cardRepository.updateCardBalance(cardId, newCardBalance);
                    return true;

                }

            }



        return false;
    }

    public void  updateCardBalance(int cardId, ResponseTransactionDTO responseTransactionDTO){
        long cardBalance = cardRepository.getCardBalance(cardId);
        if(responseTransactionDTO.isIncome()){
            cardBalance= cardBalance+responseTransactionDTO.getAmount();
        }
        else if(responseTransactionDTO.isExpense()){
            cardBalance= cardBalance-responseTransactionDTO.getAmount();

        }


          cardRepository.updateCardBalance(cardId,cardBalance);

    }

    public void updateCardCreditsUsed(int cardId, int newTransaAmount){
        long cardCreditsUsed = cardRepository.getCardCreditsUsed(cardId);

        cardCreditsUsed= cardCreditsUsed-newTransaAmount;

         cardRepository.updateCardCreditsUsed(cardId,cardCreditsUsed);

    }








}
