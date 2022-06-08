package com.example.userservice.db;

import com.example.userservice.dto.*;
import com.example.userservice.exsception.PhotoProcessingException;
import com.example.userservice.exsception.UserNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SubsribeRepository subsribeRepository;
    @Value("${DATA_SERVICE_URL}")
    private String dataServiceUrl;
    @Value("${FEED_SERVICE_URL}")
    private String feedServiceUrl;
    private final OkHttpClient httpClient = new OkHttpClient();

    public UserServiceImpl(UserRepository userRepository, SubsribeRepository subsribeRepository) {
        this.userRepository = userRepository;
        this.subsribeRepository = subsribeRepository;
    }

    @Override
    public void addUser(UserDTO userInfo) {
        UserEntity user = new UserEntity();
        user.setBirthDate(userInfo.getBirthDate());
        user.setStatus(userInfo.getStatus());
        user.setLogin(userInfo.getUsername());
        user.setFirstName(userInfo.getFirstName());
        user.setLastname(userInfo.getLastName());
        user.setGender(userInfo.getGender());
        userRepository.saveAndFlush(user);
    }

    @Override
    public void addPhoto(String username, MultipartFile file) {
        var user = userRepository.findById(username).orElseThrow(() -> new UserNotFoundException(""));
        var requestBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        try {
            requestBuilder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("img/jpeg"), file.getBytes()));

            Request request = new Request.Builder()
                    .url(dataServiceUrl + "/content/save")
                    .addHeader("Content-Type", "multipart/form-data")
                    .post(requestBuilder.build())
                    .build();
            Call call = httpClient.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful()){
                ObjectMapper mapper = new ObjectMapper();
                var files = mapper.readValue(Objects.requireNonNull(response.body()).string(), new TypeReference<List<String>>() {});
                if (Objects.isNull(files.get(0))){
                    throw new PhotoProcessingException("");
                }
                user.setPhotoUrl(files.get(0));
                userRepository.saveAndFlush(user);
            }
            else{
                throw new PhotoProcessingException("");
            }
        } catch (Exception e) {
            throw new PhotoProcessingException("");
        }
    }

    @Override
    public List<ShortUserInfoDTO> findUsers(FindDTO find) {
        return userRepository.findByLoginLike("%" + find.getUsernamePart() + "%").stream().map(use -> {
            ShortUserInfoDTO userDTO = new ShortUserInfoDTO();
            userDTO.setLastName(use.getLastname());
            userDTO.setUsername(use.getLogin());
            userDTO.setPhoto(use.getPhotoUrl());
            userDTO.setFirstName(use.getFirstName());
            return userDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public void subscribe(SubscribeDTO publisher, String subscriber) {
        var subEntity = userRepository.findById(subscriber).orElseThrow(() -> new UserNotFoundException("user"));
        var pubEntity = userRepository.findById(publisher.getUsername()).orElseThrow(() -> new UserNotFoundException("publisher"));
        var exist = subsribeRepository.findBySubscriberIdAndPublisherId(subEntity, pubEntity);
        if (Objects.isNull(exist)) {
            SubscribesEntity subscribes = new SubscribesEntity();
            subscribes.setSubscriberId(subEntity);
            subscribes.setPublisherId(pubEntity);
            subsribeRepository.saveAndFlush(subscribes);
        }
    }

    @Override
    public void unsubscribe(SubscribeDTO publisher, String subscriber) {
        var subEntity = userRepository.findById(subscriber).orElseThrow(() -> new UserNotFoundException("user"));
        var pubEntity = userRepository.findById(publisher.getUsername()).orElseThrow(() -> new UserNotFoundException("publisher"));
        var exist = subsribeRepository.findBySubscriberIdAndPublisherId(subEntity, pubEntity);
        if (Objects.isNull(exist)) {
            subsribeRepository.delete(exist);
        }
    }

    @Override
    public UserDTO getUserInfo(String user) throws IOException {
        UserDTO userDTO = new UserDTO();
        var userEntity = userRepository.findById(user).orElseThrow(() -> new UserNotFoundException("user"));
        userDTO.setGender(userEntity.getGender());
        userDTO.setStatus(userEntity.getStatus());
        userDTO.setBirthDate(userEntity.getBirthDate());
        userDTO.setUsername(userEntity.getLogin());
        userDTO.setFirstName(userEntity.getFirstName());
        userDTO.setLastName(userEntity.getLastname());
        userDTO.setPhoto(userEntity.getPhotoUrl());

        Request request = new Request.Builder()
                .url(feedServiceUrl + "/feed/"+user)
                .get()
                .build();
        Call call = httpClient.newCall(request);
        Response response = call.execute();
        ObjectMapper mapper = new ObjectMapper();
        userDTO.setPosts(mapper.readValue(Objects.requireNonNull(response.body()).string(), new TypeReference<>(){}));
        return userDTO;
    }

    @Override
    public UserandSubStatusDTO getUserAndSubStatus(String publisher, String user) throws IOException {
        UserandSubStatusDTO userandSubStatusDTO = new UserandSubStatusDTO();
        UserDTO userDTO = new UserDTO();
        var publisherEntity = userRepository.findById(publisher).orElseThrow(() -> new UserNotFoundException("publisher"));
        userDTO.setGender(publisherEntity.getGender());
        userDTO.setStatus(publisherEntity.getStatus());
        userDTO.setBirthDate(publisherEntity.getBirthDate());
        userDTO.setUsername(publisherEntity.getLogin());
        userDTO.setFirstName(publisherEntity.getFirstName());
        userDTO.setLastName(publisherEntity.getLastname());
        userDTO.setPhoto(publisherEntity.getPhotoUrl());
        userandSubStatusDTO.setUser(userDTO);
        Request request = new Request.Builder()
                .url(feedServiceUrl + "/feed/"+user)
                .get()
                .build();
        Call call = httpClient.newCall(request);
        Response response = call.execute();
        ObjectMapper mapper = new ObjectMapper();
        userDTO.setPosts(mapper.readValue(Objects.requireNonNull(response.body()).string(), new TypeReference<>(){}));
        var userEntity = userRepository.findById(user).orElseThrow(() -> new UserNotFoundException("user"));
        var exist = subsribeRepository.findBySubscriberIdAndPublisherId(userEntity, publisherEntity);
        if (Objects.isNull(exist)) {
            userandSubStatusDTO.setSubscibe(SubscribeStatus.SUBSCRIBE);
        } else {
            userandSubStatusDTO.setSubscibe(SubscribeStatus.UNSUBSCRIBE);
        }
        return userandSubStatusDTO;
    }

    @Override
    public List<ShortUserInfoDTO> getAllSubscribes(String subscriber) {
        var userEntity = userRepository.findById(subscriber).orElseThrow(() -> new UserNotFoundException("user"));
        return subsribeRepository.findAllBySubscriberId(userEntity).stream().map(user -> {
            ShortUserInfoDTO subescibes = new ShortUserInfoDTO();
            subescibes.setFirstName(user.getPublisherId().getFirstName());
            subescibes.setLastName(user.getPublisherId().getLastname());
            subescibes.setUsername(user.getPublisherId().getLogin());
            subescibes.setPhoto(user.getPublisherId().getPhotoUrl());
            return subescibes;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ShortUserInfoDTO> getAllSubscribers(String subscriber) {
        var userEntity = userRepository.findById(subscriber).orElseThrow(() -> new UserNotFoundException("user"));
        return subsribeRepository.findAllByPublisherId(userEntity).stream().map(user -> {
            ShortUserInfoDTO subescibers = new ShortUserInfoDTO();
            subescibers.setFirstName(user.getSubscriberId().getFirstName());
            subescibers.setLastName(user.getSubscriberId().getLastname());
            subescibers.setUsername(user.getSubscriberId().getLogin());
            subescibers.setPhoto(user.getSubscriberId().getPhotoUrl());
            return subescibers;
        }).collect(Collectors.toList());
    }
}
