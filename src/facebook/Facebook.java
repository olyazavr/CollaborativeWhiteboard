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

    public Facebook() throws IOException, URISyntaxException {
        // try to get the access token
        String verificationURI = "https://graph.facebook.com/oauth/authorize?client_id=" + APP_ID
                + "&redirect_uri=http://www.facebook.com/connect/login_success.html&scope=publish_stream";

        OAuthService service = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(APP_ID)
                .apiSecret(APP_SECRET)
                .callback("http://www.facebook.com/connect/login_success.html")
                .build();

        // Because OAuth is a bitch, just open a browser to get the verification
        // code
        if (Desktop.isDesktopSupported()) {
            // Windows
            Desktop.getDesktop().browse(new URI(verificationURI));
        } else {
            // Ubuntu
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("/usr/bin/firefox -new-window " + verificationURI);
        }

        String redirect = JOptionPane.showInputDialog("Enter the redirect URL (be fast!)", "");
        // get the verification id from the URL
        String verification = redirect.substring(56, redirect.length() - 4);
        Verifier verifier = new Verifier(verification);

        Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
        facebookClient = new DefaultFacebookClient(accessToken.getToken());
    }

    /**
     * Publishes an image to user's facebook. Image must be in images folder.
     * 
     * @param name
     *            name of image, including extension
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
