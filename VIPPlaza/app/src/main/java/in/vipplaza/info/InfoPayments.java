package in.vipplaza.info;

/**
 * Created by manish on 21-07-2015.
 */
public class InfoPayments {

    public String id;
    public String title;
    public String value;
    public String html;


    public InfoPayments()
    {

    }


    public InfoPayments(InfoPayments obj)
    {
        this.id=obj.id;
        this.title=obj.title;
        this.value=obj.value;
        this.html=obj.html;


    }
}
