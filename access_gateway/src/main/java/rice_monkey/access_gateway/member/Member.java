package rice_monkey.access_gateway.member;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import rice_monkey.access_gateway.member.role.Role;

@Entity
@NoArgsConstructor
@Data
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String name;

    private Role role;

    private String loginId;

    private String password;

    private Integer img_id;

    private String status;




}
