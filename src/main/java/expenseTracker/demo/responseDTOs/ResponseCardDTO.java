package expenseTracker.demo.responseDTOs;

import expenseTracker.demo.Enum.CardNetwork;
import expenseTracker.demo.entities.TransactionEntity;

import java.util.List;

public class ResponseCardDTO {
    private int  cardId;
    private CardNetwork cardNetwork;
    private   String cardHolder;
    private  String cardNo;
    private String validFrom;
    private   String validTo;
    private   boolean debit;
    private   boolean credit;
    private  long balance;
    private   long creditLimit;
    private   long creditsUsed;
    private List<ResponseTransactionDTO> transaction;


    public int getCardId(){
        return this.cardId;
    }
    public void setCardId(int cardId){
        this.cardId=cardId;
    }

    public CardNetwork getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(CardNetwork cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public boolean isDebit() {
        return debit;
    }

    public void setDebit(boolean debit) {
        this.debit = debit;
    }

    public boolean isCredit() {
        return credit;
    }

    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(long creditLimit) {
        this.creditLimit = creditLimit;
    }

    public long getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(long creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public List<ResponseTransactionDTO> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<ResponseTransactionDTO> transactionEntity) {
        this.transaction = transactionEntity;
    }

}
