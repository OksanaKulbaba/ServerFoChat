package client;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by o_kulbaba on 17.08.2017.
 */
public class ClientGuiModel {
    private final Set<String> allUserNames = new HashSet<String>();
    private String newMessage;

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public String getNewMessage() {

        return newMessage;
    }

    public Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }
    public void addUser(String newUserName) {
        allUserNames.add(newUserName);
    }
    public void deleteUser(String userName){
        if(userName != null && !userName.isEmpty()) allUserNames.remove(userName);
    }

}
