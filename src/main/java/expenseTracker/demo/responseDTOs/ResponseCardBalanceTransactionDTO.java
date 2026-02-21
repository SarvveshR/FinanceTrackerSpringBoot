package expenseTracker.demo.responseDTOs;

public class ResponseCardBalanceTransactionDTO {
    private long balance;
    private long creditsUsed;


    private ResponseTransactionDTO responseTransactionDTO;


    public ResponseTransactionDTO getResponseTransactionDTO() {
        return responseTransactionDTO;
    }

    public void setResponseTransactionDTO(ResponseTransactionDTO requestTransactionDTO) {
        this.responseTransactionDTO = requestTransactionDTO;
    }

    public long getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(long creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }


}
