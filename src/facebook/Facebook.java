package facebook;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import restfb.BinaryAttachment;
import restfb.DefaultFacebookClient;
import restfb.FacebookClient;
import restfb.Parameter;
import restfb.types.FacebookType;
import scribe.builder.ServiceBuilder;
import scribe.builder.api.FacebookApi;
import scribe.model.Token;
import scribe.model.Verifier;
import scribe.oauth.OAuthService;

/**
 * Obtains access token to user's facebook. Use this to post images.
 */
public class Facebook {
    private final FacebookClient facebookClient;
    private static final String APP_ID = "262882760531568";
    private static final String APP_SECRET = "7b3f745440588e1be6220e17597b2707";
    private static final Token EMPTY_TOKEN = null;

    /**
     * Gets access to the user's facebook. User must enter the auth redirect url
     * to get the auth token, but then Scribe takes it from there.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    public Facebook() throws IOException, URISyntaxException {
        // url to get verification token to get access token (gotta love oauth)
        String url = "https://graph.facebook.com/oauth/authorize?client_id=" + APP_ID
                + "&redirect_uri=http://www.facebook.com/connect/login_success.html&scope=publish_stream";

        // crazy scribe magic!
        OAuthService service = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(APP_ID)
                .apiSecret(APP_SECRET)
                .callback("http://www.facebook.com/connect/login_success.html")
                .build();

        // Because OAuth is a bitch, just open a browser to get the verification
        // token
        if (Desktop.isDesktopSupported()) {
            // Windows
            Desktop.getDesktop().browse(new URI(url));
        } else {
            // Ubuntu
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("/usr/bin/firefox -new-window " + url);
        }

        // i'm so sorry about this
        String redirect = JOptionPane.showInputDialog("Enter the redirect URL (be fast!)", "");

        // get the verification id from the URL
        String verification;
        if (redirect.endsWith("&_")) { // i think this is an ubuntu thing
            verification = redirect.substring(56, redirect.length() - 2);
        } else { // ends in #=_=
            verification = redirect.substring(56, redirect.length() - 2);
        }
        Verifier verifier = new Verifier(verification);

        // get the token and make a fb client!
        Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
        facebookClient = new DefaultFacebookClient(accessToken.getToken());
    }

    /**
     * Publishes an image to user's facebook.
     * 
     * @param name
     *            full path and name of image, including extension (root is
     *            whiteboard, not src)
     */
    public void publishImage(String name) {
        try {
            // get the input stream!
            File file = new File(name);
            InputStream data;
            data = new FileInputStream(file);

            // send to facebook!
            facebookClient.publish("me/photos", FacebookType.class,
                    BinaryAttachment.with(name, data),
                    Parameter.with("message", "such whiteboard. much concurrency. wow."));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
