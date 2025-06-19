package rice_monkey.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rice_monkey.member.Oauth.OauthProvider;
import rice_monkey.member.domain.Member;
import rice_monkey.member.domain.MemberRole;
import rice_monkey.member.dto.SignupRequest;
import rice_monkey.member.exception.DuplicateMemberException;
import rice_monkey.member.fegin.ImageServiceClient;
import rice_monkey.member.repository.MemberRepository;
import rice_monkey.member.util.PasswordEncoderUtil;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ImageServiceClient imageServiceClient;

    @Transactional
    public void signup(SignupRequest request){
        if(memberRepository.findByLoginId(request.getLoginId()).isPresent()){
            throw new DuplicateMemberException("이미 존재하는 로그인 ID입니다.");
        }

        Long imageId = null;
        if(request.getImage() != null || !request.getImage().isEmpty()){
            imageId = imageServiceClient.uploadImage(request.getImage()).imageId();
        }

        String salt = PasswordEncoderUtil.generateSalt();
        String hashedPassword = PasswordEncoderUtil.hashPassword(request.getPassword(), salt);

        Member member = Member.builder()
                .loginId(request.getLoginId())
                .nickname(request.getNickname())
                .passwordSalt(salt)
                .passwordHash(hashedPassword)
                .role(MemberRole.USER)
                .oauthProvider(OauthProvider.LOCAL)
                .oauthId("local_" + request.getLoginId())
                .imgId(imageId)
                .status(true)
                .build();

        memberRepository.save(member);
    }
}
