package in.vipplaza.info;

/**
 * Created by manish on 02-07-2015.
 */
public class InfoProductListing {

    public String id;
    public String name;
    public String short_description;
    public String long_description;
    public String price;
    public String speprice;
    public String img;
    public String url;
    public String brand;
    public String qty;
    public String minqty;


    public InfoProductListing()
    {

    }


    public InfoProductListing(InfoProductListing obj)

    {
        this.id=obj.id;
        this.name=obj.name;
        this.short_description=obj.short_description;
        this.long_description=obj.long_description;
        this.speprice=obj.speprice;
        this.price=obj.price;
        this.img=obj.img;
        this.url=obj.url;
        this.brand=obj.brand;

        this.qty=obj.qty;
        this.minqty=obj.minqty;

    }
}
