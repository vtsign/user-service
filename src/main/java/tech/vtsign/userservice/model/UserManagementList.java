package tech.vtsign.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementList extends PageInfo {
    private List<UserResponseDto> users;
}
