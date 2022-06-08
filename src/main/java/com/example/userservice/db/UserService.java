package com.example.userservice.db;

import com.example.userservice.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    void addUser(UserDTO userInfo);
    void addPhoto(String username, MultipartFile file);
    List<ShortUserInfoDTO> findUsers(FindDTO find);
    void subscribe(SubscribeDTO publisher,String subscriber);
    void unsubscribe(SubscribeDTO publisher,String subscriber);
    UserDTO getUserInfo(String user) throws IOException;
    UserandSubStatusDTO getUserAndSubStatus(String publisher,String user) throws IOException;
    List<ShortUserInfoDTO> getAllSubscribes(String subscriber);
    List<ShortUserInfoDTO> getAllSubscribers(String subscriber);
}
