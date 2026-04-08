package com.example.sayy.DTO;


import com.example.sayy.Entity.UserEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDTO {

    private String username;
    private String pwd;
    private String pwd2;

 public static UserDTO toUserDTO(UserEntity userEntity){

     UserDTO userDTO = new UserDTO();

     userDTO.setUsername(userEntity.getUsername());
     userDTO.setPwd(userEntity.getPwd());


     return userDTO;

 }

}
