package lv.lumii.qrng;

/**
 * Implements a pair of (User, 1KiB random block).
 * @param <User> the type used to represent users
 */
public class BlockForUser<User> {
    private User user;
    private byte[] block;

    public BlockForUser(User user, byte[] block) {
        this.user = user;
        this.block = block;
    }

    public User user() {
        return user;
    }
}
