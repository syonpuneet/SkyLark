package in.vipplaza.info;

/**
 * Created by manish on 15-07-2015.
 */
public class InfoAddress {


    public String id="";
    public String firstname;
    public String lastname;
    public String street;
    public String region;
    public String fax;

    public String city;
    public String postcode;
    public String telephone;
    public String address_type;

    public InfoAddress()
    {

    }


    public InfoAddress(InfoAddress obj)

    {
        this.id=obj.id;
        this.firstname=obj.firstname;
        this.lastname=obj.lastname;
        this.street=obj.street;
        this.region=obj.region;
        this.city=obj.city;
        this.fax=obj.fax;

        this.postcode=obj.postcode;
        this.telephone=obj.telephone;
        this.address_type=obj.address_type;


    }
}
