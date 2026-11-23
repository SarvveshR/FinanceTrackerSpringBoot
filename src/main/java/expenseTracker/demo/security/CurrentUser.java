package expenseTracker.demo.security;

// Who is making the request. Controllers get it with @AuthenticationPrincipal CurrentUser user.
public record CurrentUser(long userId, long sessionId) {
}