package kr.momo.service.attendee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.momo.domain.attendee.Attendee;
import kr.momo.domain.attendee.AttendeeRepository;
import kr.momo.domain.meeting.Meeting;
import kr.momo.domain.meeting.MeetingRepository;
import kr.momo.exception.MomoException;
import kr.momo.exception.code.MeetingErrorCode;
import kr.momo.fixture.AttendeeFixture;
import kr.momo.fixture.MeetingFixture;
import kr.momo.service.attendee.dto.AttendeeLoginRequest;
import kr.momo.support.IsolateDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@IsolateDatabase
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class AttendeeServiceTest {

    @Autowired
    private AttendeeService attendeeService;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    private Meeting meeting;

    @BeforeEach
    void setUp() {
        meeting = meetingRepository.save(MeetingFixture.COFFEE.create());
    }

    @DisplayName("로그인 시 올바르지 않은 uuid로 접근할 경우 예외를 발생시킨다.")
    @Test
    void loginThrowsExceptionForInvalidUuid() {
        Attendee attendee = attendeeRepository.save(AttendeeFixture.HOST_JAZZ.create(meeting));
        AttendeeLoginRequest request = new AttendeeLoginRequest(attendee.name(), attendee.password());

        assertThatThrownBy(() -> attendeeService.login("invalidUUID", request))
                .isInstanceOf(MomoException.class)
                .hasMessage(MeetingErrorCode.INVALID_UUID.message());
    }

    @DisplayName("로그인 시 동일한 이름이 저장되어있지 않으면 새로 참가자를 생성한다.")
    @Test
    void createsNewAttendeeIfNameIsNotAlreadyExists() {
        AttendeeLoginRequest request = new AttendeeLoginRequest("harry", "1234");

        long initialCount = attendeeRepository.count();
        attendeeService.login(meeting.getUuid(), request);
        long finalCount = attendeeRepository.count();

        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    @DisplayName("로그인 시 동일한 이름이 저장되어 있으면 새로 참가자를 생성하지 않는다.")
    @Test
    void doesNotCreateAttendeeIfNameAlreadyExists() {
        Attendee attendee = attendeeRepository.save(AttendeeFixture.HOST_JAZZ.create(meeting));
        AttendeeLoginRequest request = new AttendeeLoginRequest(attendee.name(), attendee.password());
        attendeeService.login(meeting.getUuid(), request);

        long initialCount = attendeeRepository.count();
        attendeeService.login(meeting.getUuid(), request);
        long finalCount = attendeeRepository.count();

        assertThat(finalCount).isEqualTo(initialCount);
    }

    @DisplayName("미팅에 해당하는 모든 참여자의 이름 리스트를 반환한다.")
    @Test
    void findAllAttendeeNames() {
        Attendee jazz = attendeeRepository.save(AttendeeFixture.HOST_JAZZ.create(meeting));
        Attendee pero = attendeeRepository.save(AttendeeFixture.GUEST_PEDRO.create(meeting));
        Attendee mark = attendeeRepository.save(AttendeeFixture.GUEST_MARK.create(meeting));

        List<String> attendeeNames = attendeeService.findAll(meeting.getUuid());

        assertThat(attendeeNames).containsExactlyInAnyOrder(jazz.name(), pero.name(), mark.name());
    }
}
