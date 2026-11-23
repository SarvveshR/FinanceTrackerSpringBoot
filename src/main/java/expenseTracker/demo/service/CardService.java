package expenseTracker.demo.service;


import expenseTracker.demo.entities.CardEntity;
import expenseTracker.demo.exception.ApiException;
import expenseTracker.demo.exception.ErrorCode;
import expenseTracker.demo.repository.CardRepository;
import expenseTracker.demo.requestDtos.RequestCardDTO;
import expenseTracker.demo.responseDTOs.ResponseCardDTO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardService {
    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    @Autowired
    CardRepository cardRepository;
    @Autowired
    TransactionService transactionService;

     // Throws 404 unless the card exists and belongs to this user
     public void requireOwnedCard(int cardId, long userId){
         if(!cardRepository.existsByCardIdAndUserId(cardId, userId)){
             log.debug("cardId={} not found for userId={}", cardId, userId);
             throw new ApiException(ErrorCode.CARD_NOT_FOUND);
         }
     }

     private void validate(RequestCardDTO card){
         if(card.isDebit() == card.isCredit()){
             throw new ApiException(ErrorCode.VALIDATION_FAILED, "A card must be either debit or credit");
         }
         if(card.getCardNetwork() == null){
             throw new ApiException(ErrorCode.VALIDATION_FAILED, "Pick a card network");
         }
         if(card.getCardNo() == null || card.getCardNo().isBlank()){
             throw new ApiException(ErrorCode.VALIDATION_FAILED, "Card number is required");
         }
         if(card.getCardHolder() == null || card.getCardHolder().isBlank()){
             throw new ApiException(ErrorCode.VALIDATION_FAILED, "Card holder name is required");
         }
         if(card.getBalance() < 0 || card.getCreditLimit() < 0 || card.getCreditsUsed() < 0){
             throw new ApiException(ErrorCode.VALIDATION_FAILED, "Amounts can't be negative");
         }
     }

     public void addCard(RequestCardDTO card, long userId){
         validate(card);
         CardEntity cardEntity =new CardEntity();
         cardEntity.setUserId(userId);
         cardEntity.setCardNetwork(card.getCardNetwork());
         cardEntity.setCardHolder(card.getCardHolder());
         cardEntity.setCardNo(card.getCardNo());
         cardEntity.setValidFrom(card.getValidFrom());
         cardEntity.setValidTo(card.getValidTo());
         cardEntity.setDebit(card.isDebit());
         cardEntity.setCredit(card.isCredit());
         cardEntity.setBalance(card.getBalance());
         cardEntity.setCreditLimit(card.getCreditLimit());
         cardEntity.setCreditsUsed(card.getCreditsUsed());
         CardEntity saved = cardRepository.save(cardEntity);
         log.debug("addCard saved cardId={} for userId={}", saved.getCardId(), userId);


     }
     public List<ResponseCardDTO> getCards(long userId){
         List<CardEntity> entityCards = new ArrayList<>(cardRepository.findByUserId(userId));
         log.debug("getCards found {} cards for userId={}", entityCards.size(), userId);
         List<ResponseCardDTO> cards=new ArrayList<>();
         for(CardEntity entityCard:entityCards){
             ResponseCardDTO card=new ResponseCardDTO();
             card.setCardId((entityCard.getCardId()));
             card.setCardNetwork(entityCard.getCardNetwork());
             card.setCardHolder(entityCard.getCardHolder());
             card.setCardNo(entityCard.getCardNo());
             card.setValidFrom(entityCard.getValidFrom());
             card.setValidTo(entityCard.getValidTo());
             card.setDebit(entityCard.isDebit());
             card.setCredit(entityCard.isCredit());
             card.setBalance(entityCard.getBalance());
             card.setCreditLimit(entityCard.getCreditLimit());
             card.setCreditsUsed(entityCard.getCreditsUsed());
             card.setTransaction(transactionService.getTransactionsByCardId(card.getCardId()));
             cards.add(card);


         }
         return cards;

     }
     public ResponseCardDTO getCard(CardEntity cardEntity){

         ResponseCardDTO responseCardDTO= new ResponseCardDTO();
         responseCardDTO.setCardId(cardEntity.getCardId());
         responseCardDTO.setCardHolder(cardEntity.getCardHolder());
         responseCardDTO.setCardNetwork(cardEntity.getCardNetwork());
         responseCardDTO.setCardNo(cardEntity.getCardNo());
         responseCardDTO.setValidFrom(cardEntity.getValidFrom());
         responseCardDTO.setValidTo(cardEntity.getValidTo());
         responseCardDTO.setDebit(cardEntity.isDebit());
         responseCardDTO.setCredit(cardEntity.isCredit());
         responseCardDTO.setBalance(cardEntity.getBalance());
         responseCardDTO.setCreditsUsed(cardEntity.getCreditsUsed());
         responseCardDTO.setCreditLimit(cardEntity.getCreditLimit());
         return responseCardDTO;

     }

     public ResponseCardDTO editCard(RequestCardDTO card,int cardId, long userId){
         requireOwnedCard(cardId, userId);
         CardEntity cardEntity= cardRepository.getReferenceById(cardId);
         if(card.getCardNo() == null || card.getCardNo().isBlank()
                 || card.getCardHolder() == null || card.getCardHolder().isBlank()){
             throw new ApiException(ErrorCode.VALIDATION_FAILED, "Card number and holder name are required");
         }
         if(cardEntity.isCredit() && card.getCreditLimit() < cardEntity.getCreditsUsed()){
             throw new ApiException(ErrorCode.CREDIT_LIMIT_BELOW_USED);
         }
         cardEntity.setCardHolder(card.getCardHolder());
         cardEntity.setCardNetwork(card.getCardNetwork());
         cardEntity.setCardNo(card.getCardNo());
         cardEntity.setValidFrom(card.getValidFrom());
         cardEntity.setValidTo(card.getValidTo());
         cardEntity.setCreditLimit(card.getCreditLimit());
         CardEntity saveEntity= cardRepository.save(cardEntity);
         log.debug("editCard updated cardId={}", cardId);
         return getCard(saveEntity);

     }


     // Card and its transactions go together or not at all
     @Transactional
     public boolean deleteCard(int cardId, long userId){
             requireOwnedCard(cardId, userId);

             boolean delTrans=transactionService.deleteTransactionByCardId(cardId);
             cardRepository.deleteById(cardId);
             log.debug("deleteCard cardId={} deleted, transactions cleared={}", cardId, delTrans);

             return delTrans;
     }

     public long getCardBalance(int cardId){
         return cardRepository.getCardBalance(cardId);
     }
     public long getCardCreditsUsed(int cardId){
         return cardRepository.getCardCreditsUsed(cardId);

     }

     public void updateCardBalance(int cardId, long balance) {

          cardRepository.updateCardBalance(cardId,balance);
     }

     public void updateCardCreditsUsed(int cardId, long CreditsUsed){
          cardRepository.updateCardCreditsUsed(cardId,CreditsUsed);

     }

    public boolean isCredit(int cardId){
        return cardRepository.isCredit(cardId);
    }

    public boolean isDebit(int cardId){
         return cardRepository.isDebit(cardId);

    }





}
