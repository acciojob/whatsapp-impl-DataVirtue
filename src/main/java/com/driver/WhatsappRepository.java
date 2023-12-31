package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {


    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashMap<String,User> userMobileMap; // number to user object map,

    private Map<String,Message> idMessageMap;

    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobileMap = new HashMap<String,User>();
        this.idMessageMap = new HashMap<String,Message>();

        this.customGroupCount = 0;
        this.messageId = 1;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobileMap.containsKey(mobile))
            throw new Exception("User already exists");

        userMobileMap.put(mobile,new User(name,mobile));
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        Group group;
        if(users.size()>2){
            customGroupCount++; // starting at from group 1
            group = new Group("Group " + customGroupCount, users.size());

        }else{ // name is of the participatant 2  for personal chat
            group = new Group(users.get(1).getName(),users.size());
        }

        groupUserMap.put(group,users);
        adminMap.put(group,users.get(0));
        groupMessageMap.put(group,new ArrayList<>());
        return group;


    }

    public int createMessage(String content) {

        Message message = new Message(this.messageId,content);

        idMessageMap.put(this.messageId+"", message);

        this.messageId++;

        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");

        if(!groupUserMap.get(group).contains(sender))
            throw new Exception("You are not allowed to send message");

        List<Message> messageList = groupMessageMap.get(group);
        messageList.add(message);

        idMessageMap.put(message.getId()+"",message);


        return messageList.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(adminMap.get(group)!=approver)
            throw new Exception("Approver does not have rights");

        if(!groupUserMap.get(group).contains(user))
            throw  new Exception("User is not a participant");

        adminMap.put(group,user);

        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        boolean found= false;
        for(Group group: groupUserMap.keySet()){
            for(User u: groupUserMap.get(group)){
                if(u ==user)
                    found = true;
            }
        }
        if(!found)
            throw new Exception("User not found");
        for(Group group: adminMap.keySet()){
            if(adminMap.get(group)==user)
                throw new Exception("Cannot remove admin");
        }
        userMobileMap.remove(user.getMobile());

        for(Group group: groupUserMap.keySet()){
            for(User u: groupUserMap.get(group)){
                if(u ==user){
                    groupUserMap.get(group).remove(user);
                }
            }
        }
        int count = 0;
        for(Group group: groupMessageMap.keySet()){
            for(Message m: groupMessageMap.get(group)){
                if(senderMap.get(m)==user) {
                    groupMessageMap.get(group).remove(m);
                    senderMap.remove(m);
                    count++;
                }
            }
        }

        return 1 + count;
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        int count = 0;
        List<Message> messages = new ArrayList<>();
        for(Message m: idMessageMap.values()){
            if(m.getTimestamp().after(start) && m.getTimestamp().before(end)) {
                messages.add(m);
            }
        }
        if(messages.size()<k)
            throw new Exception("K is greater than the number of messages");

        Collections.sort(messages,(a,b)-> {
            if(a.getTimestamp().before(b.getTimestamp()))
                return -1;
            return 1;
        });

        return messages.get(k).getId()+"";


    }
}
