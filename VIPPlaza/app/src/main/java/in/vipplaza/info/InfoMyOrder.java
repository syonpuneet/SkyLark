package in.vipplaza.info;

/**
 * Created by manish on 23-07-2015.
 */
public class InfoMyOrder {

    public String id;
    public String order_id;
    public String date;
    public String name;
    public String total;
    public String status;




    public InfoMyOrder()
    {

    }


    public InfoMyOrder(InfoMyOrder obj)

    {
        this.id=obj.id;
        this.order_id=obj.order_id;
        this.date=obj.date;
        this.name=obj.name;
        this.total=obj.total;
        this.status=obj.status;




    }
}
