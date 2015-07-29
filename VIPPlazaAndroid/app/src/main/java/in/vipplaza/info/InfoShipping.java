package in.vipplaza.info;

/**
 * Created by manish on 21-07-2015.
 */
public class InfoShipping {


    public String code;
    public String method;
    public String method_title;
    public String price;



    public InfoShipping()
    {

    }


    public InfoShipping(InfoShipping obj)
    {
        this.code=obj.code;
        this.method=obj.method;
        this.method_title=obj.method_title;
        this.price=obj.price;

    }
}
