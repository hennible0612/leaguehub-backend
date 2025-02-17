package leaguehub.leaguehubbackend.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import leaguehub.leaguehubbackend.domain.member.dto.member.MypageResponseDto;
import leaguehub.leaguehubbackend.domain.member.dto.member.NicknameRequestDto;
import leaguehub.leaguehubbackend.domain.member.dto.member.ProfileDto;
import leaguehub.leaguehubbackend.domain.member.service.MemberProfileService;
import leaguehub.leaguehubbackend.domain.member.service.MemberService;
import leaguehub.leaguehubbackend.global.exception.global.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Member-Controller", description = "사용자 API")
public class MemberInfoController {


    private final MemberProfileService memberProfileService;

    @Operation(summary = "사용자 프로필 조회", description = "사용자의 이미지 URL과 닉네임을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 프로필 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "MB-C-001 존재하지 않는 회원입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    @GetMapping("/member/profile")
    public ProfileDto getProfile() {
        return memberProfileService.getProfile();
    }

    @Operation(summary = "사용자 마이페이지 조회", description = "사용자의 이미지 URL, 닉네임, 이메일, 이메일 인증 상태를 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 마이페이지 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MypageResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "MB-C-001 존재하지 않는 회원입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    @GetMapping("/member/mypage")
    public MypageResponseDto getMypage() {
        return memberProfileService.getMypageProfile();
    }


    @Operation(summary = "사용자 닉네임 변경", description = "사용자 닉네임 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 변경 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "MB-C-001 PA-C-015 멤버 또는 참가자를 찾을 수 없음", content = @Content(mediaType = "application/json")),
    })
    @PostMapping("/member/profile/nickname")
    public ProfileDto changeNickName(@RequestBody @Valid NicknameRequestDto nicknameRequestDto) {

        return memberProfileService.changeMemberParticipantNickname(nicknameRequestDto);
    }

}
