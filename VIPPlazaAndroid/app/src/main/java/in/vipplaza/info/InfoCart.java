package in.vipplaza.info;

/**
 * Created by manish on 15-07-2015.
 */
public class InfoCart {


    public String id;
    public String name;
    public String sku;
    public String ssku;
    public String img;
    public String qty;
    public String price;
    public String totalqty;
    public String price_incl_tax;
    public String size;
    public String entity_id;
    public String mainprice;





    public InfoCart()
    {

    }


    public InfoCart(InfoCart obj)

    {
        this.id=obj.id;
        this.name=obj.name;
        this.sku=obj.sku;
        this.ssku=obj.ssku;
        this.img=obj.img;
        this.qty=obj.qty;
        this.price=obj.price;
        this.totalqty=obj.totalqty;
        this.price_incl_tax=obj.price_incl_tax;
        this.size=obj.size;
        this.mainprice=obj.mainprice;
        this.entity_id=obj.entity_id;


    }
}
