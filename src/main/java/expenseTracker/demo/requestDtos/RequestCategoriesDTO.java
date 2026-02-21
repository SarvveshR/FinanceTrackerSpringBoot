package expenseTracker.demo.requestDtos;

public class RequestCategoriesDTO {
    private String id;


    private String name;


    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id=id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
