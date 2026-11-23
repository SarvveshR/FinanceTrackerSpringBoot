package expenseTracker.demo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import expenseTracker.demo.exception.ApiException;
import expenseTracker.demo.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierService(@Value("${google.client-id}") String clientId) {
        // Checks signature (against Google's cached public keys), issuer, expiry and that the token was issued for our app
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idToken) {
        GoogleIdToken token;
        try {
            token = verifier.verify(idToken);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_GOOGLE_TOKEN, "Malformed Google ID token");
        } catch (GeneralSecurityException e) {
            throw new ApiException(ErrorCode.INVALID_GOOGLE_TOKEN, "Could not verify Google ID token: " + e.getMessage());
        } catch (IOException e) {
            // Google's public keys couldn't be downloaded: our network problem, not a bad token
            throw new ApiException(ErrorCode.GOOGLE_UNAVAILABLE);
        }
        if (token == null) {
            throw new ApiException(ErrorCode.INVALID_GOOGLE_TOKEN, "Invalid or expired Google ID token");
        }
        GoogleIdToken.Payload payload = token.getPayload();
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new ApiException(ErrorCode.INVALID_GOOGLE_TOKEN, "Google account email is not verified");
        }
        return payload;
    }
}
