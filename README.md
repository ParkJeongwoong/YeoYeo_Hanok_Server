[Build]
- ./gradlew clean build -x test (빌드파일 지웠다가 다시 시작)
- docker build -t dvlprjw/yeoyeo . (도커 이미지 빌드)
- docker build --no-cache -t dvlprjw/yeoyeo .
- docker push dvlprjw/yeoyeo (도커 이미지 푸시)

[Deploy]
- docker pull dvlprjw/yeoyeo (도커 이미지 풀)
- docker run -d -p 8080:8080 -v ./log:/log --name yeoyeo dvlprjw/yeoyeo
- docker run -d -p 8091:8080 --add-host=host.docker.internal:host-gateway -e TZ=Asia/Seoul -e IDLE_PROFILE=real1 -v /home/ec2-user/app/hanok/back/log:/log --name yeoyeo dvlprjw/yeoyeo
- docker run -d -p 8081:8080 --add-host=host.docker.internal:host-gateway -e TZ=Asia/Seoul -e IDLE_PROFILE=real1 -e JAVA_AGENT=/home/ec2-user/app/pinpoint/pinpoint-agent-2.2.2/pinpoint-bootstrap-2.2.2.jar -e PINPOINT_CONFIG=/home/ec2-user/app/pinpoint/pinpoint-agent-2.2.2/pinpoint-root.config -v /home/ec2-user/app/hanok/log:/log -v /home/ec2-user/app/pinpoint/pinpoint-agent-2.2.2:/pinpoint --name yeoyeo dvlprjw/yeoyeo (도커 컨테이너 실행)
  ,C:\Users\dvlprjw\IdeaProjects\Yeoyeo_Hanok\src\main\resources\application-real1.properties
- docker-compose up -d (도커 컨테이너 실행)

[Docker]
- docker exec -it yeoyeo /bin/bash (도커 컨테이너 접속)
- docker run -d -p 6379:6379 -e TZ=Asia/Seoul -v /home/ec2-user/app/redis/data:/data -v /home/ec2-user/app/redis/conf/redis.conf:/usr/local/conf/redis.conf --name redis redis
- docker exec -it redis /bin/bash (redis 컨테이너 접속)

[Dockerfile]
```dockerfile
FROM openjdk:17-jdk-slim

VOLUME /log

ARG JAR_FILE=./build/libs/*.jar
ENV IDLE_PROFILE local
ENV JAVA_AGENT /home/ec2-user/app/pinpoint/pinpoint-agent-2.2.2/pinpoint-bootstrap-2.2.2.jar
ENV PINPOINT_CONFIG /home/ec2-user/app/pinpoint/pinpoint-agent-2.2.2/pinpoint-root.config
ENV SPRING_CONFIG /home/ec2-user/app/hanok/config/application-real-db.properties,/home/ec2-user/app/hanok/config/application-env.properties,/home/ec2-user/app/hanok/config/application-${IDLE_PROFILE}.properties

COPY ${JAR_FILE} yeoyeo.jar

# 실행 명령

ENTRYPOINT ["nohup", "java","-jar",\
"-javaagent:${JAVA_AGENT}",\
"-Dpinpoint.agentId=${IDLE_PROFILE}",\
"-Dpinpoint.applicationName=yeoyeo",\
"-Dpinpoint.config=${PINPOINT_CONFIG}",\
"-Dspring.config.location=classpath:${SPRING_CONFIG}", \
"-Dspring.profiles.active=${IDLE_PROFILE}",\
"yeoyeo.jar", "2>&1", "&"]
```

[동기화 테스트]

1. yeoyeo가 맞으면 무시 v
2. yeoyeo가 아니면 비교 후 여기에 없으면 추가, 여기에 있는데 없으면 삭제 v
3. 충돌 시 예약 취소 v
4. 자동 동기화 v
   1. 주기적 예약 v
   2. 예약 직전~결제 전 확인 v
5. **재동기화시 데이터 지워지는 이슈** v
6. **5월24일 [여행] 예약이 안 되고 [여유]가 예약됨** v

에어비앤비 업데이트 주기 : 2~3시간

1. (v) 에어비앤비 예약 -> 동기화 수신
    - 예약금지 결과 : "동기화"
2. (v) 에어비앤비 예약 -> 동기화 수신 -> 같은 UID 동기화 송신 결과
    - 결과 : "무시"
3. (v) 에어비앤비 예약 -> 동기화 수신 -> 날짜 충돌 상황
   - 결과 : "무시"
4. (v) 홈페이지 예약 -> 동기화 송신
    - 결과 : "동기화"
5. (v) 홈페이지 예약 -> 동기화 송신 -> 날짜 충돌 상황
    - 결과 : "무시"
6. (v) 홈페이지 예약 -> 동기화 송신 -> 송신 데이터 삭제 -> 동기화 송신
    - 결과 : "동기화 - 에어비앤비 일정 삭제"

7. 에어비앤비 예약 -> 동기화 수신 -> 에어비앤비 삭제 -> 홈페이지 삭제 확인
8. 에어비앤비 예약 -> 동기화 수신 -> 에어비앤비 수정 -> 홈페이지 수정 확인
9. 예약 생성으로 인한 충돌 시 취소가 되는지 확인
10. 예약 변경으로 인한 충돌 시 취소가 되는지 확인

- **실예약 & 예약금지 비교**
- 모바일환경 테스트

- 날짜 충돌 처리
- 홈페이지 예약 -> 동기화 송신 -> 홈페이지 예약 취소 -> 동기화 수신 -> 홈페이지 예약의 경우, 예약 반영 X

[ics 포맷]

Event Name = VEVENT

예약 있으면
BEGIN:VEVENT
DTSTART;VALUE=DATE:20230430
DTEND;VALUE=DATE:20230501
DTSTAMP:20230419T145008Z
UID:1418fb94e984-07373f188dd17ce3c697756c0414e0df@airbnb.com
CREATED:19000101T120000Z
DESCRIPTION:Reservation URL: https://www.airbnb.com/hosting/reservations/details/HM3ZF2RT52\nPhone Number (Last 4 Digits): 3717
LAST-MODIFIED:20230409T110043Z
LOCATION:
SEQUENCE:0
STATUS:CONFIRMED
SUMMARY:Reserved
TRANSP:OPAQUE
END:VEVENT

예약 없으면
DTEND;VALUE=DATE:20230507
DTSTART;VALUE=DATE:20230504
UID:6fec1092d3fa-ee31dc011b23b48933be7054c7f95051@airbnb.com
SUMMARY:Airbnb (Not available)

[Todo]

- SMS 인증 구현 v
- Redis 사용 (번호만 있으면 됨) v
- 예약 완료 SMS 보내기 구현 v
- **여러 일 예약** v
- 리디렉션 처리 고민 v
    - 예약정보 선 생성 -> 결제정보 후 생성 (이미 있는 결제면 생성 불가) v
- **AWS  보안** v
- 대기큐 DB 저장 -> 서버 재가동 시 환불 처리 x
- **연박 할인** v
- busy waiting 개선 (reactor pattern) x
- confirm Process 신청 후 등록 (https://portone.gitbook.io/docs/tip/confirm-process) v
- 로그인 v
- 스케쥴 작업 체크 v
    - 날짜 지난 Reservation 제거 작업 추가 여부 v
- 관리자 페이지 v
    - dateroom 예약 상태 변경 (multiple) v
    - room 가격 변경 v
    - dateroom 가격 변경 (multiple) v
    - 환불 v

[3월17일 개발완료 예정]
- 인증번호 ip 제한 (10회) (1주일 블랙리스트 ip 구현)
- isReservable을 이용해 1년치 미리 생성

[관리자 페이지]

==============================================================

변동사항 신규 예약 / 환불

- 휴대폰 인증 검증
- 문자 사이트

[Webhook 경우의 수]
1. 예약 -> Webhook
   - Reservation이 있는지 확인
   - Payment가 있는지 확인

2. Webhook -> 예약


3. Webhook only


[결제 경우의 수]
예약이 우선 / WEBHOOK은 후순위
Q. WEBHOOK 도착 후 1분이 지나서 환불된 이후 예약이 되는 경우는?
A. validate 함수에 status check가 있어서 괜찮음
**통과 조건 필요** (예약된 건에 대해 환불이 안 되도록)

- 1예약 1webhook 2예약 2webhook => 1예약, 1webhook 검증완료(통과조건), 2예약 검증실패 환불(조건2), 2webhook 검증실패(조건1)
- 1예약 2예약 1webhook 2webhook => 1예약, 2예약 검증실패 환불(조건2), 1webhook 검증완료(통과조건), 2webhook 검증실패(조건1)
- 1예약 2예약 2webhook 1webhook => 1예약, 2예약 검증실패 환불(조건2), 2webhook 검증실패(조건1), 1webhook 검증완료(통과조건)
- 1예약 2webhook 2예약 1webhook => 1예약, 2webhook 검증실패 환불(조건1), 2예약 검증실패 환불(조건2), 1webhook 검증완료(통과조건)
- 1webhook 1예약 2webhook 2예약 => 1분 Lock, 1예약, 2webhook 검증실패 환불(조건1), 2예약 검증실패 환불(조건2)
- 1webhook 2webhook 1예약 2예약 => 1분 Lock, 1예약, 2예약 검증실패 환불(조건2)
- 1webhook 2webhook 2예약 1예약 => 1분 Lock, 2예약, 1예약 검증실패 환불(조건2)
- 1webhook 2예약 2webhook 1예약 => 1분 Lock, 2예약, 2webhook 검증완료(통과조건), 1예약 검증실패 환불(조건2)

[각 작업]
예약 : DATEROOM BOOKED, RESV. COUNT+1 / RESERVATION CREATE / PAYMENT CREATE / GUEST CREATE 
webhook : DATEROOM (IF NOT BOOKED) BOOKED FOR 1 MINUTE => (WHEN REFUND) RESV. COUNT+1