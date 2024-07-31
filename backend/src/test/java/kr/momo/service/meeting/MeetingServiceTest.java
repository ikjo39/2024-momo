package kr.momo.service.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import kr.momo.domain.attendee.Attendee;
import kr.momo.domain.attendee.AttendeeRepository;
import kr.momo.domain.availabledate.AvailableDate;
import kr.momo.domain.availabledate.AvailableDateRepository;
import kr.momo.domain.meeting.Meeting;
import kr.momo.domain.meeting.MeetingRepository;
import kr.momo.exception.MomoException;
import kr.momo.exception.code.AvailableDateErrorCode;
import kr.momo.exception.code.MeetingErrorCode;
import kr.momo.fixture.AttendeeFixture;
import kr.momo.fixture.MeetingFixture;
import kr.momo.service.meeting.dto.MeetingCreateRequest;
import kr.momo.service.meeting.dto.MeetingResponse;
import kr.momo.service.meeting.dto.MeetingSharingResponse;
import kr.momo.support.IsolateDatabase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@IsolateDatabase
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MeetingServiceTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private AvailableDateRepository availableDateRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @DisplayName("UUID로 약속 정보를 조회한다.")
    @Test
    void findByUUID() {
        Meeting meeting = meetingRepository.save(MeetingFixture.MOVIE.create());
        Attendee attendee = attendeeRepository.save(AttendeeFixture.HOST_JAZZ.create(meeting));
        List<AvailableDate> availableDates = List.of(
                availableDateRepository.save(new AvailableDate(LocalDate.now(), meeting)),
                availableDateRepository.save(new AvailableDate(LocalDate.now().plusDays(1), meeting))
        );

        MeetingResponse response = meetingService.findByUUID(meeting.getUuid());

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.firstTime()).isEqualTo(meeting.startTimeslotTime());
            softAssertions.assertThat(response.lastTime()).isEqualTo(meeting.endTimeslotTime());
            softAssertions.assertThat(response.meetingName()).isEqualTo(meeting.getName());
            softAssertions.assertThat(response.availableDates().size()).isEqualTo(availableDates.size());
            softAssertions.assertThat(response.attendeeNames()).isEqualTo(List.of(attendee.name()));
        });
    }

    @DisplayName("생성 완료된 약속의 정보를 조회한다.")
    @Test
    void findMeetingSharing() {
        Meeting meeting = meetingRepository.save(MeetingFixture.GAME.create());

        MeetingSharingResponse result = meetingService.findMeetingSharing(meeting.getUuid());

        assertThat(result.uuid()).isEqualTo(meeting.getUuid());
    }

    @DisplayName("생성 완료된 약속의 정보를 조회시 uuid가 일치하지 않으면 예외가 발생한다.")
    @Test
    void doesNotFindMeetingSharingMeetingIfUUIDNotExist() {
        String invalidUUID = "1234";
        Meeting meeting = meetingRepository.save(MeetingFixture.GAME.create());
        attendeeRepository.save(AttendeeFixture.HOST_JAZZ.create(meeting));

        assertThatThrownBy(() -> meetingService.findMeetingSharing(invalidUUID))
                .isInstanceOf(MomoException.class)
                .hasMessage(MeetingErrorCode.INVALID_UUID.message());
    }

    @DisplayName("약속을 생성할 때 같은 약속일을 2번 이상 보내면 예외가 발생합니다.")
    @Test
    void throwExceptionWhenDuplicatedDates() {
        //given
        LocalDate date = LocalDate.of(2024, 7, 24);
        MeetingCreateRequest request = new MeetingCreateRequest(
                "momoHost",
                "momo",
                "momoMeeting",
                List.of(date, date),
                LocalTime.of(8, 0),
                LocalTime.of(22, 0));

        //when //then
        assertThatThrownBy(() -> meetingService.create(request))
                .isInstanceOf(MomoException.class)
                .hasMessage(AvailableDateErrorCode.DUPLICATED_DATE.message());
    }
}