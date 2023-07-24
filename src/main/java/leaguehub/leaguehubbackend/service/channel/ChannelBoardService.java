package leaguehub.leaguehubbackend.service.channel;

import leaguehub.leaguehubbackend.dto.channel.ChannelBoardDto;
import leaguehub.leaguehubbackend.dto.channel.ChannelBoardLoadDto;
import leaguehub.leaguehubbackend.entity.channel.Channel;
import leaguehub.leaguehubbackend.entity.channel.ChannelBoard;
import leaguehub.leaguehubbackend.entity.member.Member;
import leaguehub.leaguehubbackend.entity.participant.Participant;
import leaguehub.leaguehubbackend.entity.participant.Role;
import leaguehub.leaguehubbackend.exception.channel.exception.ChannelBoardNotFoundException;
import leaguehub.leaguehubbackend.exception.participant.exception.InvalidParticipantAuthException;
import leaguehub.leaguehubbackend.repository.channel.ChannelBoardRepository;
import leaguehub.leaguehubbackend.repository.particiapnt.ParticipantRepository;
import leaguehub.leaguehubbackend.service.member.MemberService;
import leaguehub.leaguehubbackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ChannelBoardService {

    private final ChannelService channelService;
    private final ChannelBoardRepository channelBoardRepository;
    private final MemberService memberService;
    private final ParticipantRepository participantRepository;

    @Transactional
    public void createChannelBoard(String channelLink, ChannelBoardDto request) {

        Member member = getMember();

        List<Participant> findByMemberId = participantRepository.findAllByMemberId(member.getId());
        checkAuth(channelLink, findByMemberId);

        findByMemberId.stream()
                .filter(getParticipantPredicate(channelLink))
                .forEach(participant -> {
                    ChannelBoard channelBoard = ChannelBoard.createChannelBoard(participant.getChannel(),
                            request.getTitle(), request.getContent());
                    channelBoardRepository.save(channelBoard);
                });


    }


    /**
     * 채널 로딩 시점에서 불러오는 채널 게시판(내용은 반환하지 않음.)
     *
     * @param channelId
     * @return List
     */
    @Transactional
    public List<ChannelBoardLoadDto> loadChannelBoards(String channelLink) {
        Channel channel = channelService.validateChannel(channelLink);

        List<ChannelBoard> channelBoards = channelBoardRepository.findAllByChannel(channel);

        List<ChannelBoardLoadDto> channelBoardLoadDtoList = channelBoards.stream()
                .map(channelBoard -> new ChannelBoardLoadDto(channelBoard.getId(), channelBoard.getTitle()))
                .collect(Collectors.toList());

        return channelBoardLoadDtoList;
    }

    @Transactional
    public ChannelBoardDto getChannelBoard(String channelLink, Long boardId) {
        Channel channel = channelService.validateChannel(channelLink);
        ChannelBoard channelBoard = validateChannelBoard(boardId);

        if (channelBoard.getChannel() != channel) {
            throw new ChannelBoardNotFoundException();
        }

        return new ChannelBoardDto(channelBoard.getTitle(), channelBoard.getContent());
    }

    @Transactional
    public void updateChannelBoard(String channelLink, Long boardId, ChannelBoardDto update) {
        Member member = getMember();

        ChannelBoard channelBoard = validateChannelBoard(boardId);

        List<Participant> findByMemberId = participantRepository.findAllByMemberId(member.getId());
        checkAuth(channelLink, findByMemberId);

        findByMemberId.stream()
                .filter(participant -> participant.getChannel() == channelBoard.getChannel()
                        && (participant.getRole() == Role.HOST || participant.getRole() == Role.MANAGER))
                .forEach(participant ->
                        channelBoardRepository.findById(boardId).get().updateChannelBoard(update.getTitle(), update.getContent()));

    }

    @Transactional
    public void deleteChannelBoard(String channelLink, Long boardId) {
        Member member = getMember();

        ChannelBoard channelBoard = validateChannelBoard(boardId);

        List<Participant> findByMemberId = participantRepository.findAllByMemberId(member.getId());
        checkAuth(channelLink, findByMemberId);

        findByMemberId.stream()
                .filter(participant -> participant.getChannel() == channelBoard.getChannel()
                        && (participant.getRole() == Role.HOST || participant.getRole() == Role.MANAGER))
                .forEach(participant -> channelBoardRepository.deleteById(boardId));

    }

    private Member getMember() {
        UserDetails userDetails = SecurityUtils.getAuthenticatedUser();
        String personalId = userDetails.getUsername();

        Member member = memberService.validateMember(personalId);
        return member;
    }

    private void checkAuth(String channelLink, List<Participant> findByMemberId) {
        boolean hasValidParticipant = findByMemberId.stream()
                .anyMatch(getParticipantPredicate(channelLink));

        if (!hasValidParticipant) {
            throw new InvalidParticipantAuthException();
        }
    }

    @NotNull
    private static Predicate<Participant> getParticipantPredicate(String channelLink) {
        return participant -> (participant.getRole() == Role.HOST || participant.getRole() == Role.MANAGER)
                && participant.getChannel().getChannelLink().equals(channelLink);
    }


    public ChannelBoard validateChannelBoard(Long channelBoardId) {
        return channelBoardRepository.findById(channelBoardId)
                .orElseThrow(ChannelBoardNotFoundException::new);
    }


}
