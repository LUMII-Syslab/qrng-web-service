package lv.lumii.qrng;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.nio.BufferOverflowException;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class UsersQueue<User> {
    private int maxUsersToTrail = 1000;
    private long time = 0;
    // ^^^ will be incremented when registering each next user;
    //     non-OOP analog of MutableInteger
    private PriorityBlockingQueue<Registration> prioritizedRegistrations;
    private Map<User, Registration> registeredUsers;

    public UsersQueue(int maxUsersToTrail) {
        if (maxUsersToTrail > 0)
            this.maxUsersToTrail = maxUsersToTrail;
        this.prioritizedRegistrations = new PriorityBlockingQueue<>(this.maxUsersToTrail);
        this.registeredUsers = new HashMap<>();
    }

    /**
     * Registers the given user as waiting to be served. If the user has been registered
     * but has not been served yet (by invoking earliestUser),
     * the user is not registered for the second time and KeyAlreadyExistsException is thrown
     *
     * @param user the user object
     * @throws BufferOverflowException   if there are too many waiting users registered
     * @throws KeyAlreadyExistsException if the user has been already registered
     */
    public synchronized void enqueue(User user) throws BufferOverflowException, KeyAlreadyExistsException {
        time++;
        if (prioritizedRegistrations.size() >= maxUsersToTrail)
            throw new BufferOverflowException();
        if (registeredUsers.containsKey(user))
            throw new KeyAlreadyExistsException();

        Registration registration = new Registration(user, time);
        prioritizedRegistrations.add(registration);
        registeredUsers.put(user, registration);
    }

    /**
     * Takes out the user who waited for the longest period of time.
     * This is a blocking call (it will wait for the user, if there is none).
     * However, the call will not interfere with other synchronized methods of UsersQueue.
     *
     * @return the user who waited the longest
     * @throws InterruptedException if there are no waiting users in the registry,
     *                              and we were interrupted when waiting for one
     */
    public User takeUser() throws InterruptedException { // !!! non-synchronized but blocking
        Registration u1 = prioritizedRegistrations.take();
        synchronized (this) {
            Registration u2 = registeredUsers.get(u1.user());
            if (u1 == u2)
                registeredUsers.remove(u1.user());
            // ^^^ remove only our registration;
            //     otherwise, that is a new registration, and the user is waiting again...
        }
        return u1.user();
    }

    /**
     * Forcefully removes the user from the queue. Useful, when the connection is lost.
     *
     * @param user
     */
    public synchronized void kick(User user) {
        Registration registration = registeredUsers.remove(user);
        if (registration != null)
            prioritizedRegistrations.remove(registration);
    }

    /**
     * Implements the pair (user, time) with the time comparator.
     */
    private class Registration implements Comparable<Registration> {
        private User user;
        private long time;

        public Registration(User user, long time) {
            this.user = user;
            this.time = time;
        }

        @Override
        public int compareTo(Registration other) {
            long diff = this.time - other.time;
            if (diff < 0)
                return -1;
            else if (diff == 0)
                return 0; // should never happen, since time is increased with each new user
            else
                return +1;
        }

        public User user() {
            return this.user;
        }

        public long time() {
            return this.time;
        }
    }

}
