package tech.vtsign.userservice.model.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item{
    @JsonProperty("itemid")
    public String id;
    @JsonProperty("itemname")
    public String name;
    @JsonProperty("itemquantity")
    public int quantity;
    @JsonProperty("itemprice")
    public int price;
    public int getAmount(){
        return quantity * price;
    }
}
