package expenseTracker.demo.repository;

import expenseTracker.demo.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {
    @Query(
            value ="SELECT * FROM transaction_entity WHERE card_id= :cardId " ,
            nativeQuery = true
    )
    public List<TransactionEntity> getAllTransaction(@Param("cardId") int cardId);

    @Query(
            value=" SELECT * FROM transaction_entity WHERE id=:transactionId",
            nativeQuery = true
    )
    public  TransactionEntity getTransactionAmount(@Param("transactionId") int transactionId);

@Modifying
    @Query(
            nativeQuery = true,
            value = "DELETE FROM transaction_entity WHERE card_id=:cardId"
    )
    public int deleteTransactionbyCardId(@Param("cardId") int cardId);


}
