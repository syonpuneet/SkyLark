package in.vipplaza.info;

/**
 * Created by manish on 24-07-2015.
 */
public class InfoOrderDetails {


    public String product_id;
    public String product_sku;
    public String product_price;
    public String product_name;

    public String status;
    public String category_name;
    public String size;
    public String subtot;

    public String qty;



    public InfoOrderDetails()
    {

    }


    public InfoOrderDetails(InfoOrderDetails obj)

    {
        this.product_id=obj.product_id;
        this.product_sku=obj.product_sku;
        this.product_price=obj.product_price;
        this.product_name=obj.product_name;
        this.status=obj.status;
        this.category_name=obj.category_name;
        this.size=obj.size;
        this.qty=obj.qty;
        this.subtot=obj.subtot;


    }
}
