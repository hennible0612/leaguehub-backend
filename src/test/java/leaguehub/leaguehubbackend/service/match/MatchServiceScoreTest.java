package leaguehub.leaguehubbackend.service.match;

import jakarta.transaction.Transactional;
import leaguehub.leaguehubbackend.domain.match.service.MatchQueryService;
import leaguehub.leaguehubbackend.domain.match.service.MatchService;
import leaguehub.leaguehubbackend.domain.channel.dto.CreateChannelDto;
import leaguehub.leaguehubbackend.domain.match.dto.MatchPlayerInfo;
import leaguehub.leaguehubbackend.domain.match.dto.MatchScoreInfoDto;
import leaguehub.leaguehubbackend.domain.channel.entity.Channel;
import leaguehub.leaguehubbackend.domain.channel.entity.ChannelRule;
import leaguehub.leaguehubbackend.domain.match.entity.Match;
import leaguehub.leaguehubbackend.domain.match.entity.MatchPlayer;
import leaguehub.leaguehubbackend.domain.member.entity.Member;
import leaguehub.leaguehubbackend.domain.participant.entity.Participant;
import leaguehub.leaguehubbackend.domain.match.exception.exception.MatchNotFoundException;
import leaguehub.leaguehubbackend.fixture.ChannelFixture;
import leaguehub.leaguehubbackend.fixture.UserFixture;
import leaguehub.leaguehubbackend.domain.channel.repository.ChannelRepository;
import leaguehub.leaguehubbackend.domain.channel.repository.ChannelRuleRepository;
import leaguehub.leaguehubbackend.domain.match.repository.MatchPlayerRepository;
import leaguehub.leaguehubbackend.domain.match.repository.MatchRepository;
import leaguehub.leaguehubbackend.domain.member.repository.MemberRepository;
import leaguehub.leaguehubbackend.domain.participant.repository.ParticipantRepository;
import leaguehub.leaguehubbackend.domain.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.properties")
public class MatchServiceScoreTest {

    @Autowired
    private MatchService matchService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    ChannelRuleRepository channelRuleRepository;
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    MatchPlayerRepository matchPlayerRepository;
    @Autowired
    MatchRepository matchRepository;
    @Autowired
    MatchQueryService matchQueryService;
    @Mock
    MemberService memberService;

    private Member member1;

    private Match savedMatch;
    private Channel channel;

    @BeforeEach
    public void setUp() {
        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username("member1")
                .password("member1")
                .roles("USER")
                .build();

        GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsUser, null
                , authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        member1 = memberRepository.save(UserFixture.createCustomeMember("member1"));
        Member member2 = memberRepository.save(UserFixture.createCustomeMember("member2"));
        Member member3 = memberRepository.save(UserFixture.createCustomeMember("member3"));
        Member member4 = memberRepository.save(UserFixture.createCustomeMember("member4"));

        CreateChannelDto channelDto = ChannelFixture.createAllPropertiesCustomChannelDto(false, false, 2400, null, 20);
        channel = Channel.createChannel(channelDto.getTitle(),
                channelDto.getGameCategory(), channelDto.getMaxPlayer(),
                channelDto.getMatchFormat(), channelDto.getChannelImageUrl());
        ChannelRule channelRule = ChannelRule.createChannelRule(channel, channelDto.getTier(), channelDto.getTierMax(), channelDto.getTierMin(),
                channelDto.getPlayCount(),
                channelDto.getPlayCountMin());
        channelRepository.save(channel);
        channelRuleRepository.save(channelRule);

        // Participant 생성 및 저장
        Participant participant1 = participantRepository.save(Participant.createHostChannel(member1, channel));
        Participant participant2 = participantRepository.save(Participant.createHostChannel(member2, channel));
        Participant participant3 = participantRepository.save(Participant.createHostChannel(member3, channel));
        Participant participant4 = participantRepository.save(Participant.createHostChannel(member4, channel));

        // Match 생성 및 저장
        Integer matchRound = 1;
        String matchName = "Sample Match";
        Match match = Match.createMatch(matchRound, channel, matchName);
        savedMatch = matchRepository.save(match);

        // MatchPlayer 생성
        MatchPlayer matchPlayer1 = MatchPlayer.createMatchPlayer(participant1, savedMatch);
        MatchPlayer matchPlayer2 = MatchPlayer.createMatchPlayer(participant2, savedMatch);
        MatchPlayer matchPlayer3 = MatchPlayer.createMatchPlayer(participant3, savedMatch);
        MatchPlayer matchPlayer4 = MatchPlayer.createMatchPlayer(participant4, savedMatch);
        matchPlayer1.updateMatchPlayerScore(1);
        matchPlayer2.updateMatchPlayerScore(2);
        matchPlayer3.updateMatchPlayerScore(2);
        matchPlayer4.updateMatchPlayerScore(3);

        matchPlayerRepository.save(matchPlayer1);
        matchPlayerRepository.save(matchPlayer2);
        matchPlayerRepository.save(matchPlayer3);
        matchPlayerRepository.save(matchPlayer4);
    }

    @AfterEach
    public void tearDown() {
        matchPlayerRepository.deleteAll();
        participantRepository.deleteAll();
        matchRepository.deleteAll();
        channelRuleRepository.deleteAll();
        channelRepository.deleteAll();
        memberRepository.deleteAll();

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getMatchScoreInfo 테스트 - 성공")
    public void getMatchScoreInfoSuccessTest() throws Exception {

        MatchScoreInfoDto result = matchQueryService.getMatchScoreInfo(channel.getChannelLink(), savedMatch.getId());
        assertNotNull(result);
        assertEquals(-1, result.getRequestMatchPlayerId());

        List<MatchPlayerInfo> scoreInfos = result.getMatchPlayerInfos();
        assertNotNull(scoreInfos);
        assertEquals(4, scoreInfos.size());

    }

    @Test
    @DisplayName("순위 정렬 테스트")
    public void test() throws Exception {

        Long matchId = 1L;

        MatchScoreInfoDto testDto = matchQueryService.getMatchScoreInfo(channel.getChannelLink(), savedMatch.getId());

        assertNotNull(testDto);
        assertEquals(-1, testDto.getRequestMatchPlayerId());

        List<MatchPlayerInfo> matchPlayerInfos = testDto.getMatchPlayerInfos();
        assertNotNull(matchPlayerInfos);
        assertEquals(4, matchPlayerInfos.size());

        assertEquals(1, matchPlayerInfos.get(0).getMatchRank());
        assertEquals(2, matchPlayerInfos.get(1).getMatchRank());
        assertEquals(2, matchPlayerInfos.get(2).getMatchRank());
        assertEquals(4, matchPlayerInfos.get(3).getMatchRank());

    }


    @Test
    @DisplayName("getMatchScoreInfo 테스트 - 유효하지 않은 matchId")
    public void getMatchScoreInfoInvalidMatchIdTest() {

        Long invalidMatchId = 1234L;

        assertThrows(MatchNotFoundException.class, () -> {
            matchQueryService.getMatchScoreInfo("1234", invalidMatchId);
        });
    }

}