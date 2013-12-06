package facebook;

import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;

/**
 * Finds Olga's facebook. Use this to post images.
 * 
 * TODO: we should really not be posting to my facebook....figure out this OAUTH
 * shit
 * 
 */
public class Facebook {
    private static final String MY_ACCESS_TOKEN = "CAACEdEose0cBANYtS1nkNJMH8wei0OW1ixro4nXI7FPbXzoOMIm3Q38BB1mGDduKzdhqOssq7zzP4Q35RdvZAMK27codbd3ALd9sgfOKslRZB0xPCRpRMKRV3F0desSZB5sePgUcbMP9XbZAxaZCtQJhGOHAXoooxF8VP7AuKAhj5ofaGc7sCrxLsRtgka8kZD";

    FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN);

    /**
     * Publishes an image to Olga's facebook. Use wisely. Image must be in
     * images folder.
     * 
     * @param name
     *            name of image, including extension
     */
    public void publishImage(String name) {
        FacebookType publishPhotoResponse = facebookClient.publish("images/", FacebookType.class,
                BinaryAttachment.with(name, getClass().getResourceAsStream("/" + name)),
                Parameter.with("message", "005 proj test!"));

        System.out.println("Published photo ID: " + publishPhotoResponse.getId());
    }
}
