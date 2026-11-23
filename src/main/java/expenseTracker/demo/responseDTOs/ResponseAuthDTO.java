package expenseTracker.demo.responseDTOs;

public class ResponseAuthDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn; // seconds until accessToken expires
    private ResponseUserDTO user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public ResponseUserDTO getUser() {
        return user;
    }

    public void setUser(ResponseUserDTO user) {
        this.user = user;
    }
}
