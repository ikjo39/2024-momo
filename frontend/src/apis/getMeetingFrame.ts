import { API_URL } from '@constants/api';

import { fetchClient } from './fetchClient';

export interface Schedules {
  date: string;
  times: string[];
}

interface MeetingFrameSeverResponse {
  data: {
    meeting: {
      name: string;
      firstTime: string;
      lastTime: string;
      availableDates: string[];
    };
    attendees: string[];
  };
}

interface MeetingFrame {
  meetingName: string;
  firstTime: string;
  lastTime: string;
  availableDates: string[];
  attendees: string[];
}

const getMeetingFrame = async (uuid: string): Promise<MeetingFrame | undefined> => {
  const url = `${API_URL}/api/v1/meeting/${uuid}`;

  const response = await fetchClient<MeetingFrameSeverResponse>({
    url,
    method: 'GET',
    errorMessage: '약속 정보를 조회하는 중 문제가 발생했어요 :(',
  });

  if (response) {
    const { name, firstTime, lastTime, availableDates } = response.data.meeting;
    return {
      meetingName: name,
      firstTime,
      lastTime,
      availableDates,
      attendees: response.data.attendees,
    };
  }
};

export default getMeetingFrame;