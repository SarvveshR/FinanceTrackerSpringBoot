package expenseTracker.demo.repository;

import expenseTracker.demo.entities.CardEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<CardEntity, Integer> {


    @Query(nativeQuery = true,
            value = "SELECT balance FROM  card_entity  WHERE card_id= :cardId")
    public long getCardBalance(@Param("cardId") int cardId);


    @Query(nativeQuery = true,
    value = "SELECT credit_limit FROM  card_entity  WHERE card_id= :cardId ")
    public long getCreditLimit(@Param("cardId") int cardId);

    @Query(nativeQuery = true,
            value = "SELECT credits_used FROM  card_entity  WHERE card_id= :cardId")
    public long getCardCreditsUsed(@Param("cardId") int cardId);


    @Query(nativeQuery = true,
            value = "SELECT debit FROM  card_entity  WHERE card_id= :cardId")
    public boolean isDebit(@Param("cardId") int cardId);

    @Query(nativeQuery = true,
            value = "SELECT credit FROM  card_entity  WHERE card_id= :cardId")
    public boolean isCredit(@Param("cardId") int cardId);


    @Modifying
    @Transactional
    @Query(nativeQuery = true,
    value = "UPDATE card_entity SET balance =:balance WHERE card_id= :cardId")
    public void updateCardBalance(@Param("cardId") int cardId, @Param("balance") long balance);


    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "UPDATE card_entity SET credits_used =:creditsUsed WHERE card_id= :cardId")
    public void updateCardCreditsUsed(@Param("cardId") int cardId, @Param("creditsUsed") long creditsUsed);






}
