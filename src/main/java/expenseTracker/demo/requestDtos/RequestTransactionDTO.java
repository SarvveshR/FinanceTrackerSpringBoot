package expenseTracker.demo.requestDtos;

public class RequestTransactionDTO {
    private RequestCategoriesDTO type;
    private String date;
    private String time;
    private String description;
    private int amount;
    private boolean expense;
    private boolean income;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isIncome() {
        return income;
    }

    public void setIncome(boolean income) {
        this.income = income;
    }



    public RequestCategoriesDTO getType(){
        return this.type;

    }
    public void  setType(RequestCategoriesDTO type){
        this.type=type;
    }

    public String getDate(){
        return this.date;

    }
    public void setDate(String date){
        this.date=date;
    }
    public boolean isExpense(){
        return this.expense;
    }
    public void setExpense(boolean expense){
        this.expense=expense;
    }

}
