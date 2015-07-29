package in.vipplaza.info;

/**
 * Created by manish on 02-07-2015.
 */
public class InfoHomeEvents {

    public String event_id;
    public String event_name;
    public String event_logo;
    public String event_image;
    public String disc_amount;
    public String category_id;
    public String event_start;
    public String event_end;
    public String last_generate;
    public String event_menu;
    public String disc_info;
    public String event_promo;
    public String event_category;




    public InfoHomeEvents()
    {

    }


    public InfoHomeEvents(InfoHomeEvents obj)

    {
        this.event_id=obj.event_id;
        this.event_name=obj.event_name;
        this.event_logo=obj.event_logo;
        this.event_image=obj.event_image;
        this.disc_amount=obj.disc_amount;
        this.category_id=obj.category_id;
        this.event_start=obj.event_start;
        this.event_end=obj.event_end;
        this.last_generate=obj.last_generate;
        this.event_menu=obj.event_menu;
        this.disc_info=obj.disc_info;
        this.event_promo=obj.event_promo;
        this.event_category=obj.event_category;



    }
}
