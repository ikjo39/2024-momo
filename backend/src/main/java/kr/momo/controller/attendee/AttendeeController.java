package kr.momo.controller.attendee;

import jakarta.validation.Valid;
import kr.momo.controller.MomoApiResponse;
import kr.momo.service.attendee.AttendeeService;
import kr.momo.service.attendee.dto.AttendeeLoginRequest;
import kr.momo.service.attendee.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttendeeController {

    private final AttendeeService attendeeService;

    @PostMapping("/api/v1/meetings/{uuid}/login")
    public MomoApiResponse<TokenResponse> login(
            @PathVariable String uuid, @RequestBody @Valid AttendeeLoginRequest request
    ) {
        return new MomoApiResponse<>(attendeeService.login(uuid, request));
    }
}