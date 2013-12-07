package facebook;

import restfb.DefaultFacebookClient;
import restfb.FacebookClient;
import restfb.Parameter;
import restfb.types.FacebookType;

/**
 * Finds Olga's facebook. Use this to post images.
 * 
 * TODO: we should really not be posting to my facebook....figure out this OAUTH
 * shit
 * 
 * access token:
 * CAADvFymPHnABAAeFmmKZBckkarf0Sc0BHZCaMpRcrgpScnzdWFtZCOYEcqkBrOg6fhgyQieTU8WrcPcKGquJcXEOtIaA5AlAZAG9t4V1IdeetqNZCJbhBd1fR731sbYQDoBXpgGl6lAZA1KDf6yWHIFZBtJrohvUV6Wyb3LYezAsN5PFAqSaPar
 * 
 */
public class Facebook {
    private static final String MY_ACCESS_TOKEN = "CAADvFymPHnABAAeFmmKZBckkarf0Sc0BHZCaMpRcrgpScnzdWFtZCOYEcqkBrOg6fhgyQieTU8WrcPcKGquJcXEOtIaA5AlAZAG9t4V1IdeetqNZCJbhBd1fR731sbYQDoBXpgGl6lAZA1KDf6yWHIFZBtJrohvUV6Wyb3LYezAsN5PFAqSaPar";

    FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN);

    /**
     * Publishes an image to Olga's facebook. Use wisely. Image must be in
     * images folder.
     * 
     * @param name
     *            name of image, including extension
     */
    public void publishImage(String name) {
        // FacebookType publishPhotoResponse = facebookClient.publish("images/",
        // FacebookType.class,
        // BinaryAttachment.with(name, getClass().getResourceAsStream("/" +
        // name)),
        // Parameter.with("message", "005 proj test!"));
        //
        // System.out.println("Published photo ID: " +
        // publishPhotoResponse.getId());

        FacebookType publishMessageResponse =
                facebookClient.publish("me/feed", FacebookType.class,
                        Parameter.with("message", "RestFB test"));

    }
}
