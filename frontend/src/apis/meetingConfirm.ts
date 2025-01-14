import { fetchClient } from './_common/fetchClient';

export interface ConfirmDates {
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
}

interface PostMeetingConfirmRequest {
  uuid: string;

  requests: ConfirmDates;
}

export interface GetConfirmedMeetingInfoResponse extends ConfirmDates {
  meetingName: string;
  availableAttendeeNames: string[];
  startDayOfWeek: string;
  endDayOfWeek: string;
}

export const postMeetingConfirm = async ({ uuid, requests }: PostMeetingConfirmRequest) => {
  const data = await fetchClient({
    path: `/${uuid}/confirm`,
    method: 'POST',
    errorMessage: '약속 시간 확정을 요청하는데 실패했어요 :(',
    body: requests,
    isAuthRequire: true,
  });

  return data;
};

export const getConfirmedMeetingInfo = async (uuid: string) => {
  const data = await fetchClient<Promise<GetConfirmedMeetingInfoResponse>>({
    path: `/${uuid}/confirm`,
    method: 'GET',
    errorMessage: '확정된 약속 정보 조회애 실패했어요 :(',
  });

  return data;
};
