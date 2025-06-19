package rice_monkey.member.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rice_monkey.member.dto.SignupRequest;
import rice_monkey.member.service.MemberService;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public void signup(@ModelAttribute SignupRequest request) {
        memberService.signup(request);
    }
}
