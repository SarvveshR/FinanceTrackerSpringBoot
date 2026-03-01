package expenseTracker.demo.service;


import expenseTracker.demo.entities.CardEntity;
import expenseTracker.demo.repository.CardRepository;
import expenseTracker.demo.requestDtos.RequestCardDTO;
import expenseTracker.demo.responseDTOs.ResponseCardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardService {
    @Autowired
    CardRepository cardRepository;
    @Autowired
    TransactionService transactionService;

     public void addCard(RequestCardDTO card){
         CardEntity cardEntity =new CardEntity();
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
         cardRepository.save(cardEntity);


     }
     public List<ResponseCardDTO> getCards(){
         List<CardEntity> entityCards = new ArrayList<>(cardRepository.findAll());
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

     public ResponseCardDTO editCard(RequestCardDTO card,int cardId){
         CardEntity cardEntity= cardRepository.getReferenceById(cardId);
         cardEntity.setCardHolder(card.getCardHolder());
         cardEntity.setCardNetwork(card.getCardNetwork());
         cardEntity.setCardNo(card.getCardNo());
         cardEntity.setValidFrom(card.getValidFrom());
         cardEntity.setValidTo(card.getValidTo());
         cardEntity.setCreditLimit(card.getCreditLimit());
         CardEntity saveEntity= cardRepository.save(cardEntity);
         return getCard(saveEntity);

     }


     public boolean deleteCard(int cardId){

             boolean delTrans=transactionService.deleteTransactionByCardId(cardId);
             cardRepository.deleteById(cardId);

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
