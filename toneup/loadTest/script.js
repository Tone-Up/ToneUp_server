import http from 'k6/http';
import { check, sleep } from 'k6';

// open()은 반드시 글로벌 스코프에서 호출
const imageFile = open('./test-image.jpg', 'b');

export const options = {
  vus: 20,
  duration: '100s',
};

const API_URL = 'http://host.docker.internal:8080/api/personalcolortest';
const TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsIm5pY2tuYW1lIjoi7Jqp7JqpXzExNzE3ODM5MTM0NTU4MjA0NjcyMyIsInBlcnNvbmFsQ29sb3IiOiJudWxsIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc0OTAzOTgwMSwiZXhwIjoxNzQ5MDQxMDEwfQ.mp5uOlIbWtQdfffOIUbHCrxjPGtp2UykdZG45TspkNk';

export default function () {
  const payload = {
    image: http.file(imageFile, 'test-image.jpg'),
  };

  const params = {
    headers: {
      Authorization: `Bearer ${TOKEN}`,
    },
  };

  const res = http.post(API_URL, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}


